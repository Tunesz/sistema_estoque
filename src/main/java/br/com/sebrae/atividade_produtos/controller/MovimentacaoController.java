package br.com.sebrae.atividade_produtos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.form.MovimentacaoForm;
import br.com.sebrae.atividade_produtos.model.Movimentacao;
import br.com.sebrae.atividade_produtos.model.TipoMovimentacao;
import br.com.sebrae.atividade_produtos.repository.MovimentacaoRepository;
import br.com.sebrae.atividade_produtos.service.EstoqueService;
import br.com.sebrae.atividade_produtos.service.ProdutoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    private final EstoqueService estoqueService;
    private final ProdutoService produtoService;
    private final MovimentacaoRepository movimentacaoRepository;

    public MovimentacaoController(EstoqueService estoqueService, ProdutoService produtoService,
                                  MovimentacaoRepository movimentacaoRepository) {
        this.estoqueService = estoqueService;
        this.produtoService = produtoService;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(name = "produtoId", required = false) Long produtoId, Model model) {
        model.addAttribute("movimentacoes", produtoId == null
                ? movimentacaoRepository.findTop200ByOrderByDataHoraDescIdDesc()
                : movimentacaoRepository.findTop200ByProdutoIdOrderByDataHoraDescIdDesc(produtoId));
        model.addAttribute("produtos", produtoService.buscar("", null, false, true));
        model.addAttribute("produtoId", produtoId);
        return "movimentacoes/lista";
    }

    @GetMapping("/nova")
    public String nova(@RequestParam(name = "produtoId", required = false) Long produtoId,
                       @RequestParam(name = "tipo", defaultValue = "ENTRADA") TipoMovimentacao tipo,
                       Model model) {
        MovimentacaoForm form = new MovimentacaoForm();
        form.setProdutoId(produtoId);
        form.setTipo(tipo);
        prepararFormulario(model, form);
        return "movimentacoes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") MovimentacaoForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                Movimentacao m = estoqueService.registrar(form.getProdutoId(), form.getTipo(),
                        form.getQuantidade(), form.getMotivo());
                ra.addFlashAttribute("sucesso", m.getTipo().getDescricao() + " registrada: \""
                        + m.getProduto().getNome() + "\" agora tem " + m.getSaldoPosterior() + " "
                        + m.getProduto().getUnidade() + ".");
                return "redirect:/produtos/" + m.getProduto().getId();
            } catch (RegraNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        prepararFormulario(model, form);
        return "movimentacoes/form";
    }

    private void prepararFormulario(Model model, MovimentacaoForm form) {
        model.addAttribute("form", form);
        model.addAttribute("produtos", produtoService.listarAtivos());
        model.addAttribute("tipos", TipoMovimentacao.values());
    }
}
