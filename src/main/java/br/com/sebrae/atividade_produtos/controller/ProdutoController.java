package br.com.sebrae.atividade_produtos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.form.ProdutoForm;
import br.com.sebrae.atividade_produtos.model.Produto;
import br.com.sebrae.atividade_produtos.repository.MovimentacaoRepository;
import br.com.sebrae.atividade_produtos.service.CategoriaService;
import br.com.sebrae.atividade_produtos.service.ProdutoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final MovimentacaoRepository movimentacaoRepository;

    public ProdutoController(ProdutoService produtoService, CategoriaService categoriaService,
                             MovimentacaoRepository movimentacaoRepository) {
        this.produtoService = produtoService;
        this.categoriaService = categoriaService;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(name = "busca", defaultValue = "") String busca,
                         @RequestParam(name = "categoriaId", required = false) Long categoriaId,
                         @RequestParam(name = "baixo", defaultValue = "false") boolean baixo,
                         @RequestParam(name = "inativos", defaultValue = "false") boolean inativos,
                         Model model) {
        model.addAttribute("produtos", produtoService.buscar(busca, categoriaId, baixo, inativos));
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("baixo", baixo);
        model.addAttribute("inativos", inativos);
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ProdutoForm());
        model.addAttribute("categorias", categoriaService.listar());
        return "produtos/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable("id") Long id, Model model) {
        model.addAttribute("produto", produtoService.buscar(id));
        model.addAttribute("movimentacoes", movimentacaoRepository.findTop30ByProdutoIdOrderByDataHoraDescIdDesc(id));
        return "produtos/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("form", ProdutoForm.de(produtoService.buscar(id)));
        model.addAttribute("categorias", categoriaService.listar());
        return "produtos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") ProdutoForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (produtoService.skuEmUso(form.getSku(), form.getId())) {
            result.rejectValue("sku", "duplicado", "Já existe um produto com este SKU");
        }
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listar());
            return "produtos/form";
        }
        try {
            Produto salvo = produtoService.salvar(form);
            ra.addFlashAttribute("sucesso", "Produto \"" + salvo.getNome() + "\" salvo com sucesso.");
            return "redirect:/produtos/" + salvo.getId();
        } catch (RegraNegocioException e) {
            result.reject("regra", e.getMessage());
            model.addAttribute("categorias", categoriaService.listar());
            return "produtos/form";
        }
    }

    @PostMapping("/{id}/alternar")
    public String alternar(@PathVariable("id") Long id, RedirectAttributes ra) {
        Produto p = produtoService.alternarAtivo(id);
        ra.addFlashAttribute("sucesso", "Produto \"" + p.getNome() + "\" " + (p.isAtivo() ? "ativado." : "inativado."));
        return "redirect:/produtos/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            produtoService.excluir(id);
            ra.addFlashAttribute("sucesso", "Produto excluído.");
            return "redirect:/produtos";
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/produtos/" + id;
        }
    }
}
