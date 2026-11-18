package br.com.sebrae.atividade_produtos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sebrae.atividade_produtos.exception.RegraNegocioException;
import br.com.sebrae.atividade_produtos.model.Categoria;
import br.com.sebrae.atividade_produtos.repository.CategoriaRepository;
import br.com.sebrae.atividade_produtos.repository.ProdutoRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByNomeAsc();
    }

    public Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada."));
    }

    public long contarProdutos(Long categoriaId) {
        return produtoRepository.countByCategoriaId(categoriaId);
    }

    public boolean nomeEmUso(String nome, Long idAtual) {
        if (nome == null || nome.isBlank()) {
            return false;
        }
        return categoriaRepository.findByNomeIgnoreCase(nome.trim())
                .filter(c -> !c.getId().equals(idAtual))
                .isPresent();
    }

    @Transactional
    public Categoria salvar(Categoria form) {
        Categoria categoria = form.getId() == null ? new Categoria() : buscar(form.getId());
        categoria.setNome(form.getNome().trim());
        String descricao = form.getDescricao();
        categoria.setDescricao(descricao == null || descricao.isBlank() ? null : descricao.trim());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscar(id);
        long qtd = produtoRepository.countByCategoriaId(id);
        if (qtd > 0) {
            throw new RegraNegocioException("A categoria \"" + categoria.getNome() + "\" possui " + qtd
                    + " produto(s). Mova-os para outra categoria antes de excluir.");
        }
        categoriaRepository.delete(categoria);
    }
}
