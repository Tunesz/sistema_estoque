package br.com.sebrae.atividade_produtos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.sebrae.atividade_produtos.exception.EstoqueInsuficienteException;
import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.form.ProdutoForm;
import br.com.sebrae.atividade_produtos.model.Movimentacao;
import br.com.sebrae.atividade_produtos.model.Produto;
import br.com.sebrae.atividade_produtos.model.TipoMovimentacao;
import br.com.sebrae.atividade_produtos.repository.MovimentacaoRepository;

@SpringBootTest
@Transactional
class EstoqueServiceTest {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    private Produto novoProduto(int quantidadeInicial, int minimo) {
        ProdutoForm form = new ProdutoForm();
        form.setSku("t-" + System.nanoTime());
        form.setNome("Produto de teste");
        form.setUnidade("un");
        form.setPrecoCusto(new BigDecimal("10.00"));
        form.setPrecoVenda(new BigDecimal("15.00"));
        form.setEstoqueMinimo(minimo);
        form.setQuantidadeInicial(quantidadeInicial);
        return produtoService.salvar(form);
    }

    private int saldo(Produto p) {
        return produtoService.buscar(p.getId()).getQuantidade();
    }

    @Test
    void cadastroComQuantidadeInicialGeraEntrada() {
        Produto p = novoProduto(25, 5);

        assertEquals(25, saldo(p));
        Movimentacao m = movimentacaoRepository.findTop30ByProdutoIdOrderByDataHoraDescIdDesc(p.getId()).get(0);
        assertEquals(TipoMovimentacao.ENTRADA, m.getTipo());
        assertEquals(0, m.getSaldoAnterior());
        assertEquals(25, m.getSaldoPosterior());
    }

    @Test
    void skuEhNormalizadoParaMaiusculas() {
        Produto p = novoProduto(0, 0);
        assertEquals(p.getSku().toUpperCase(), p.getSku());
        assertTrue(produtoService.skuEmUso(p.getSku().toLowerCase(), null));
        assertFalse(produtoService.skuEmUso(p.getSku(), p.getId()));
    }

    @Test
    void entradaSomaESaidaSubtrai() {
        Produto p = novoProduto(10, 2);

        estoqueService.registrar(p.getId(), TipoMovimentacao.ENTRADA, 5, "compra");
        assertEquals(15, saldo(p));

        Movimentacao saida = estoqueService.registrar(p.getId(), TipoMovimentacao.SAIDA, 4, "  requisição  ");
        assertEquals(11, saldo(p));
        assertEquals(15, saida.getSaldoAnterior());
        assertEquals(11, saida.getSaldoPosterior());
        assertEquals(-4, saida.getVariacao());
        assertEquals("requisição", saida.getMotivo());
    }

    @Test
    void saidaMaiorQueSaldoEhRejeitadaESaldoNaoMuda() {
        Produto p = novoProduto(3, 1);

        assertThrows(EstoqueInsuficienteException.class,
                () -> estoqueService.registrar(p.getId(), TipoMovimentacao.SAIDA, 4, null));
        assertEquals(3, saldo(p));
    }

    @Test
    void saidaDoSaldoExatoZeraOEstoque() {
        Produto p = novoProduto(3, 1);

        estoqueService.registrar(p.getId(), TipoMovimentacao.SAIDA, 3, null);

        assertEquals(0, saldo(p));
        assertTrue(produtoService.buscar(p.getId()).isSemEstoque());
    }

    @Test
    void ajusteDefineOSaldoComoContagemFisica() {
        Produto p = novoProduto(10, 2);

        Movimentacao m = estoqueService.registrar(p.getId(), TipoMovimentacao.AJUSTE, 7, "inventário");

        assertEquals(7, saldo(p));
        assertEquals(-3, m.getVariacao());
    }

    @Test
    void entradaESaidaExigemQuantidadePositiva() {
        Produto p = novoProduto(10, 2);

        assertThrows(RegraNegocioException.class,
                () -> estoqueService.registrar(p.getId(), TipoMovimentacao.ENTRADA, 0, null));
        assertThrows(RegraNegocioException.class,
                () -> estoqueService.registrar(p.getId(), TipoMovimentacao.SAIDA, -1, null));
    }

    @Test
    void produtoInativoNaoPodeSerMovimentado() {
        Produto p = novoProduto(10, 2);
        produtoService.alternarAtivo(p.getId());

        assertThrows(RegraNegocioException.class,
                () -> estoqueService.registrar(p.getId(), TipoMovimentacao.ENTRADA, 1, null));
    }

    @Test
    void alertaDeEstoqueMinimo() {
        Produto p = novoProduto(5, 5);
        assertTrue(produtoService.listarAbaixoDoMinimo().stream().anyMatch(x -> x.getId().equals(p.getId())));

        estoqueService.registrar(p.getId(), TipoMovimentacao.ENTRADA, 1, null);
        assertFalse(produtoService.listarAbaixoDoMinimo().stream().anyMatch(x -> x.getId().equals(p.getId())));
    }

    @Test
    void produtoComHistoricoNaoPodeSerExcluidoMasSemHistoricoPode() {
        Produto comHistorico = novoProduto(10, 2);
        assertThrows(RegraNegocioException.class, () -> produtoService.excluir(comHistorico.getId()));

        Produto semHistorico = novoProduto(0, 0);
        produtoService.excluir(semHistorico.getId());
        assertThrows(RegraNegocioException.class, () -> produtoService.buscar(semHistorico.getId()));
    }
}
