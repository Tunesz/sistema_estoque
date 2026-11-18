package br.com.sebrae.atividade_produtos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sebrae.atividade_produtos.model.Movimentacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findTop10ByOrderByDataHoraDescIdDesc();

    List<Movimentacao> findTop200ByOrderByDataHoraDescIdDesc();

    List<Movimentacao> findTop200ByProdutoIdOrderByDataHoraDescIdDesc(Long produtoId);

    List<Movimentacao> findTop30ByProdutoIdOrderByDataHoraDescIdDesc(Long produtoId);

    boolean existsByProdutoId(Long produtoId);
}
