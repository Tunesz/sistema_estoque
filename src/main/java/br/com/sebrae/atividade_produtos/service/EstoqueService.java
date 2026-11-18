package br.com.sebrae.atividade_produtos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sebrae.atividade_produtos.exception.EstoqueInsuficienteException;
import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.model.Movimentacao;
import br.com.sebrae.atividade_produtos.model.Produto;
import br.com.sebrae.atividade_produtos.model.TipoMovimentacao;
import br.com.sebrae.atividade_produtos.repository.MovimentacaoRepository;
import br.com.sebrae.atividade_produtos.repository.ProdutoRepository;

/**
 * Único ponto do sistema que altera o saldo de um produto. Cada alteração gera uma
 * {@link Movimentacao}, mantendo o histórico completo (quem olha o histórico consegue
 * reconstituir o saldo).
 */
@Service
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public EstoqueService(ProdutoRepository produtoRepository, MovimentacaoRepository movimentacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    /**
     * Registra uma movimentação.
     * <ul>
     *   <li>ENTRADA: soma {@code quantidade} ao saldo;</li>
     *   <li>SAIDA: subtrai {@code quantidade} (não permite saldo negativo);</li>
     *   <li>AJUSTE: define o saldo como a contagem física informada.</li>
     * </ul>
     */
    @Transactional
    public Movimentacao registrar(Long produtoId, TipoMovimentacao tipo, int quantidade, String motivo) {
        if (produtoId == null || tipo == null) {
            throw new RegraNegocioException("Produto e tipo de movimentação são obrigatórios.");
        }
        if (quantidade < 0 || (tipo != TipoMovimentacao.AJUSTE && quantidade == 0)) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero.");
        }

        Produto produto = produtoRepository.findByIdForUpdate(produtoId)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado."));
        if (!produto.isAtivo()) {
            throw new RegraNegocioException("O produto \"" + produto.getNome()
                    + "\" está inativo e não pode ser movimentado.");
        }

        int anterior = produto.getQuantidade();
        int posterior;
        switch (tipo) {
            case ENTRADA -> posterior = anterior + quantidade;
            case SAIDA -> {
                if (quantidade > anterior) {
                    throw new EstoqueInsuficienteException(produto.getNome(), anterior, quantidade);
                }
                posterior = anterior - quantidade;
            }
            case AJUSTE -> posterior = quantidade;
            default -> throw new RegraNegocioException("Tipo de movimentação inválido.");
        }

        produto.setQuantidade(posterior);
        produtoRepository.save(produto);

        String motivoLimpo = (motivo == null || motivo.isBlank()) ? null : motivo.trim();
        return movimentacaoRepository.save(
                new Movimentacao(produto, tipo, quantidade, anterior, posterior, motivoLimpo));
    }
}
