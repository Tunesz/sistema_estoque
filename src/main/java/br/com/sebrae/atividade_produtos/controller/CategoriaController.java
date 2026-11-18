package br.com.sebrae.atividade_produtos.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.model.Categoria;
import br.com.sebrae.atividade_produtos.service.CategoriaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        List<Categoria> categorias = categoriaService.listar();
        Map<Long, Long> qtdProdutos = new HashMap<>();
        for (Categoria c : categorias) {
            qtdProdutos.put(c.getId(), categoriaService.contarProdutos(c.getId()));
        }
        model.addAttribute("categorias", categorias);
        model.addAttribute("qtdProdutos", qtdProdutos);
        return "categorias/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("categoria", categoriaService.buscar(id));
        return "categorias/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("categoria") Categoria categoria, BindingResult result,
                         RedirectAttributes ra) {
        if (categoriaService.nomeEmUso(categoria.getNome(), categoria.getId())) {
            result.rejectValue("nome", "duplicado", "Já existe uma categoria com este nome");
        }
        if (result.hasErrors()) {
            return "categorias/form";
        }
        Categoria salva = categoriaService.salvar(categoria);
        ra.addFlashAttribute("sucesso", "Categoria \"" + salva.getNome() + "\" salva com sucesso.");
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            categoriaService.excluir(id);
            ra.addFlashAttribute("sucesso", "Categoria excluída.");
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/categorias";
    }
}
