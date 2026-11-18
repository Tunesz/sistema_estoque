package br.com.sebrae.atividade_produtos.exception;

public class EstoqueInsuficienteException extends RegraNegocioException {

    public EstoqueInsuficienteException(String produto, int disponivel, int solicitado) {
        super("Estoque insuficiente para \"" + produto + "\": disponível " + disponivel
                + ", solicitado " + solicitado + ".");
    }
}
