package br.com.sebrae.atividade_produtos.controller;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.sebrae.atividade_produtos.model.Produto;
import br.com.sebrae.atividade_produtos.service.ProdutoService;

@Controller
public class RelatorioController {

    private final ProdutoService produtoService;

    public RelatorioController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /** Posição de estoque em CSV (separador ';' e BOM UTF-8, para abrir direto no Excel em pt-BR). */
    @GetMapping("/relatorios/estoque.csv")
    public ResponseEntity<byte[]> estoqueCsv() {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("SKU;Produto;Categoria;Unidade;Quantidade;Estoque minimo;Custo unitario;Valor em estoque;Situacao\n");
        for (Produto p : produtoService.listarAtivos()) {
            String situacao = p.isSemEstoque() ? "Sem estoque" : p.isAbaixoDoMinimo() ? "Abaixo do minimo" : "OK";
            sb.append(csv(p.getSku())).append(';')
              .append(csv(p.getNome())).append(';')
              .append(csv(p.getCategoria() == null ? "" : p.getCategoria().getNome())).append(';')
              .append(csv(p.getUnidade())).append(';')
              .append(p.getQuantidade()).append(';')
              .append(p.getEstoqueMinimo()).append(';')
              .append(decimal(p.getPrecoCusto())).append(';')
              .append(decimal(p.getValorEmEstoque())).append(';')
              .append(situacao).append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"estoque.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String decimal(BigDecimal v) {
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    /** Aspas e escape de campo CSV; neutraliza texto que o Excel interpretaria como fórmula. */
    private static String csv(String valor) {
        String s = valor == null ? "" : valor;
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
