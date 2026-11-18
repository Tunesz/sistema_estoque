package br.com.sebrae.atividade_produtos.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.service.ProdutoService;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final ProdutoService produtoService;

    public GlobalControllerAdvice(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /** Número exibido no selo de alertas do menu lateral. */
    @ModelAttribute("qtdAlertas")
    public int qtdAlertas() {
        return produtoService.listarAbaixoDoMinimo().size();
    }

    /** Qualquer regra de negócio violada fora de um formulário volta ao painel com a mensagem. */
    @ExceptionHandler(RegraNegocioException.class)
    public String regraNegocio(RegraNegocioException e, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/";
    }
}
