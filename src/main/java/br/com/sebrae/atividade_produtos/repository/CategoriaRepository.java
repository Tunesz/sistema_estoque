package br.com.sebrae.atividade_produtos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sebrae.atividade_produtos.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNomeAsc();

    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
