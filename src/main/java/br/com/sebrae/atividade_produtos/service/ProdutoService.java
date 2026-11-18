package br.com.sebrae.atividade_produtos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.form.ProdutoForm;
import br.com.sebrae.atividade_produtos.model.Categoria;
import br.com.sebrae.atividade_produtos.model.Produto;
import br.com.sebrae.atividade_produtos.model.TipoMovimentacao;
import br.com.sebrae.atividade_produtos.repository.CategoriaRepository;
import br.com.sebrae.atividade_produtos.repository.MovimentacaoRepository;
import br.com.sebrae.atividade_produtos.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final EstoqueService estoqueService;

    public ProdutoService(ProdutoRepository produtoRepository,
                          CategoriaRepository categoriaRepository,
                          MovimentacaoRepository movimentacaoRepository,
                          EstoqueService estoqueService) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.estoqueService = estoqueService;
    }

    public List<Produto> buscar(String busca, Long categoriaId, boolean somenteBaixo, boolean incluirInativos) {
        String termo = busca == null ? "" : busca.trim();
        long categoria = categoriaId == null ? 0L : categoriaId;
        return produtoRepository.buscar(termo, categoria, somenteBaixo, incluirInativos);
    }

    public List<Produto> listarAtivos() {
        return produtoRepository.findByAtivoTrueOrderByNomeAsc();
    }

    public List<Produto> listarAbaixoDoMinimo() {
        return produtoRepository.findAbaixoDoMinimo();
    }

    public Produto buscar(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado."));
    }

    public boolean skuEmUso(String sku, Long idAtual) {
        if (sku == null || sku.isBlank()) {
            return false;
        }
        return produtoRepository.findBySkuIgnoreCase(sku.trim())
                .filter(p -> !p.getId().equals(idAtual))
                .isPresent();
    }

    @Transactional
    public Produto salvar(ProdutoForm form) {
        boolean novo = form.getId() == null;
        Produto produto = novo ? new Produto() : buscar(form.getId());

        produto.setSku(form.getSku().trim().toUpperCase());
        produto.setNome(form.getNome().trim());
        String descricao = form.getDescricao();
        produto.setDescricao(descricao == null || descricao.isBlank() ? null : descricao.trim());
        produto.setUnidade(form.getUnidade().trim().toUpperCase());
        produto.setPrecoCusto(form.getPrecoCusto());
        produto.setPrecoVenda(form.getPrecoVenda());
        produto.setEstoqueMinimo(form.getEstoqueMinimo());
        produto.setAtivo(form.isAtivo());

        Categoria categoria = null;
        if (form.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(form.getCategoriaId())
                    .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada."));
        }
        produto.setCategoria(categoria);

        Produto salvo = produtoRepository.save(produto);

        Integer inicial = form.getQuantidadeInicial();
        if (novo && inicial != null && inicial > 0) {
            estoqueService.registrar(salvo.getId(), TipoMovimentacao.ENTRADA, inicial, "Estoque inicial");
        }
        return salvo;
    }

    @Transactional
    public Produto alternarAtivo(Long id) {
        Produto produto = buscar(id);
        produto.setAtivo(!produto.isAtivo());
        return produtoRepository.save(produto);
    }

    /** Produtos com histórico de movimentação não podem ser excluídos (apenas inativados). */
    @Transactional
    public void excluir(Long id) {
        Produto produto = buscar(id);
        if (movimentacaoRepository.existsByProdutoId(id)) {
            throw new RegraNegocioException("O produto \"" + produto.getNome()
                    + "\" possui movimentações registradas e não pode ser excluído. Inative-o.");
        }
        produtoRepository.delete(produto);
    }
}
