package br.com.sebrae.atividade_produtos.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.sebrae.atividade_produtos.form.ProdutoForm;
import br.com.sebrae.atividade_produtos.model.Categoria;
import br.com.sebrae.atividade_produtos.model.Produto;
import br.com.sebrae.atividade_produtos.model.TipoMovimentacao;
import br.com.sebrae.atividade_produtos.repository.CategoriaRepository;
import br.com.sebrae.atividade_produtos.service.EstoqueService;
import br.com.sebrae.atividade_produtos.service.ProdutoService;

/** Popula um banco vazio com dados de exemplo (desligue com estoque.dados-demo=false). */
@Component
public class DadosDemoLoader implements CommandLineRunner {

    private final boolean ativo;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoService produtoService;
    private final EstoqueService estoqueService;

    public DadosDemoLoader(@Value("${estoque.dados-demo:false}") boolean ativo,
                           CategoriaRepository categoriaRepository,
                           ProdutoService produtoService,
                           EstoqueService estoqueService) {
        this.ativo = ativo;
        this.categoriaRepository = categoriaRepository;
        this.produtoService = produtoService;
        this.estoqueService = estoqueService;
    }

    @Override
    public void run(String... args) {
        if (!ativo || categoriaRepository.count() > 0) {
            return;
        }

        Categoria papelaria = categoriaRepository.save(new Categoria("Papelaria", "Papel, canetas e organização"));
        Categoria informatica = categoriaRepository.save(new Categoria("Informática", "Periféricos e acessórios"));
        Categoria limpeza = categoriaRepository.save(new Categoria("Limpeza", "Produtos de limpeza e higiene"));
        Categoria copa = categoriaRepository.save(new Categoria("Copa e cozinha", "Itens de consumo do escritório"));

        Produto sulfite = criar("PAP-001", "Papel sulfite A4 (resma 500 fl.)", papelaria, "CX", "22.50", "34.90", 40, 15);
        criar("PAP-002", "Caneta esferográfica azul", papelaria, "UN", "0.90", "2.50", 300, 100);
        criar("PAP-003", "Caderno universitário 96 fls.", papelaria, "UN", "9.80", "18.90", 8, 12);
        criar("INF-001", "Mouse óptico USB", informatica, "UN", "18.00", "39.90", 25, 10);
        criar("INF-002", "Teclado ABNT2 USB", informatica, "UN", "35.00", "79.90", 4, 8);
        criar("INF-003", "Cabo HDMI 2 m", informatica, "UN", "12.50", "29.90", 0, 5);
        criar("LMP-001", "Detergente neutro 500 ml", limpeza, "UN", "1.80", "3.99", 60, 24);
        criar("LMP-002", "Álcool 70% 1 L", limpeza, "UN", "6.50", "11.90", 18, 20);
        criar("CPA-001", "Café torrado 500 g", copa, "UN", "14.90", "24.90", 30, 10);
        criar("CPA-002", "Copo descartável 200 ml (c/100)", copa, "PCT", "5.40", "9.90", 22, 10);

        // Algumas movimentações para o histórico não nascer vazio.
        estoqueService.registrar(sulfite.getId(), TipoMovimentacao.SAIDA, 6, "Requisição do setor financeiro");
        estoqueService.registrar(sulfite.getId(), TipoMovimentacao.ENTRADA, 10, "Compra - NF 1042");
    }

    private Produto criar(String sku, String nome, Categoria categoria, String unidade,
                       String custo, String venda, int quantidade, int minimo) {
        ProdutoForm f = new ProdutoForm();
        f.setSku(sku);
        f.setNome(nome);
        f.setCategoriaId(categoria.getId());
        f.setUnidade(unidade);
        f.setPrecoCusto(new BigDecimal(custo));
        f.setPrecoVenda(new BigDecimal(venda));
        f.setEstoqueMinimo(minimo);
        f.setQuantidadeInicial(quantidade);
        return produtoService.salvar(f);
    }
}
