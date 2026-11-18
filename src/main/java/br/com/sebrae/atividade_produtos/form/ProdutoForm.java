package br.com.sebrae.atividade_produtos.form;

import java.math.BigDecimal;

import br.com.sebrae.atividade_produtos.model.Produto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados do formulário de produto. O saldo nunca é editado aqui, só por movimentações. */
public class ProdutoForm {

    private Long id;

    @NotBlank(message = "Informe o SKU (código) do produto")
    @Size(max = 40, message = "Use no máximo 40 caracteres")
    private String sku;

    @NotBlank(message = "Informe o nome do produto")
    @Size(max = 120, message = "Use no máximo 120 caracteres")
    private String nome;

    @Size(max = 500, message = "Use no máximo 500 caracteres")
    private String descricao;

    private Long categoriaId;

    @NotBlank(message = "Informe a unidade (UN, KG, CX...)")
    @Size(max = 10, message = "Use no máximo 10 caracteres")
    private String unidade = "UN";

    @NotNull(message = "Informe o preço de custo")
    @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
    private BigDecimal precoCusto;

    @NotNull(message = "Informe o preço de venda")
    @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
    private BigDecimal precoVenda;

    @NotNull(message = "Informe o estoque mínimo")
    @Min(value = 0, message = "O estoque mínimo não pode ser negativo")
    private Integer estoqueMinimo = 0;

    /** Usado somente no cadastro: gera uma entrada "Estoque inicial". */
    @Min(value = 0, message = "A quantidade inicial não pode ser negativa")
    private Integer quantidadeInicial = 0;

    private boolean ativo = true;

    public static ProdutoForm de(Produto p) {
        ProdutoForm f = new ProdutoForm();
        f.id = p.getId();
        f.sku = p.getSku();
        f.nome = p.getNome();
        f.descricao = p.getDescricao();
        f.categoriaId = p.getCategoria() == null ? null : p.getCategoria().getId();
        f.unidade = p.getUnidade();
        f.precoCusto = p.getPrecoCusto();
        f.precoVenda = p.getPrecoVenda();
        f.estoqueMinimo = p.getEstoqueMinimo();
        f.ativo = p.isAtivo();
        return f;
    }

    public boolean isNovo() {
        return id == null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public BigDecimal getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(BigDecimal precoCusto) { this.precoCusto = precoCusto; }

    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) { this.precoVenda = precoVenda; }

    public Integer getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(Integer estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public Integer getQuantidadeInicial() { return quantidadeInicial; }
    public void setQuantidadeInicial(Integer quantidadeInicial) { this.quantidadeInicial = quantidadeInicial; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
