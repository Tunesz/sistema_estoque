package br.com.sebrae.atividade_produtos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.sebrae.atividade_produtos.repository.MovimentacaoRepository;
import br.com.sebrae.atividade_produtos.service.DashboardService;
import br.com.sebrae.atividade_produtos.service.ProdutoService;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final ProdutoService produtoService;
    private final MovimentacaoRepository movimentacaoRepository;

    public DashboardController(DashboardService dashboardService, ProdutoService produtoService,
                               MovimentacaoRepository movimentacaoRepository) {
        this.dashboardService = dashboardService;
        this.produtoService = produtoService;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @GetMapping("/")
    public String painel(Model model) {
        model.addAttribute("resumo", dashboardService.resumo());
        model.addAttribute("alertas", produtoService.listarAbaixoDoMinimo());
        model.addAttribute("ultimas", movimentacaoRepository.findTop10ByOrderByDataHoraDescIdDesc());
        return "index";
    }
}
