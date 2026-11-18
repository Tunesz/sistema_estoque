package br.com.sebrae.atividade_produtos.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.sebrae.atividade_produtos.model.Produto;

@Service
public class DashboardService {

    /** Indicadores gerais do estoque (considera apenas produtos ativos). */
    public record Resumo(long totalProdutos, long totalUnidades, BigDecimal valorCusto,
                         BigDecimal valorVenda, long abaixoDoMinimo, long semEstoque) {
    }

    private final ProdutoService produtoService;

    public DashboardService(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    public Resumo resumo() {
        List<Produto> ativos = produtoService.listarAtivos();
        long unidades = 0;
        long abaixo = 0;
        long zerados = 0;
        BigDecimal custo = BigDecimal.ZERO;
        BigDecimal venda = BigDecimal.ZERO;

        for (Produto p : ativos) {
            unidades += p.getQuantidade();
            custo = custo.add(p.getValorEmEstoque());
            venda = venda.add(p.getPrecoVenda().multiply(BigDecimal.valueOf(p.getQuantidade())));
            if (p.isAbaixoDoMinimo()) {
                abaixo++;
            }
            if (p.isSemEstoque()) {
                zerados++;
            }
        }
        return new Resumo(ativos.size(), unidades, custo, venda, abaixo, zerados);
    }
}
