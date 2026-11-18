package br.com.sebrae.atividade_produtos.form;

import br.com.sebrae.atividade_produtos.model.TipoMovimentacao;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MovimentacaoForm {

    @NotNull(message = "Selecione o produto")
    private Long produtoId;

    @NotNull(message = "Selecione o tipo de movimentação")
    private TipoMovimentacao tipo = TipoMovimentacao.ENTRADA;

    @NotNull(message = "Informe a quantidade")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    private Integer quantidade;

    @Size(max = 255, message = "Use no máximo 255 caracteres")
    private String motivo;

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public TipoMovimentacao getTipo() { return tipo; }
    public void setTipo(TipoMovimentacao tipo) { this.tipo = tipo; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
