package br.com.sebrae.atividade_produtos.model;

public enum TipoMovimentacao {

    ENTRADA("Entrada", "Quantidade a adicionar"),
    SAIDA("Saída", "Quantidade a retirar"),
    AJUSTE("Ajuste (inventário)", "Contagem física (novo saldo)");

    private final String descricao;
    private final String rotuloQuantidade;

    TipoMovimentacao(String descricao, String rotuloQuantidade) {
        this.descricao = descricao;
        this.rotuloQuantidade = rotuloQuantidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getRotuloQuantidade() {
        return rotuloQuantidade;
    }

    /** Nome em minúsculas, usado como classe CSS nos templates. */
    public String getCss() {
        return name().toLowerCase();
    }
}
