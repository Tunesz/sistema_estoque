package br.com.sebrae.atividade_produtos.exception;

/** Violação de uma regra de negócio; a mensagem pode ser exibida ao usuário. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
