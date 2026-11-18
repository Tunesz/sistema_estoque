package br.com.sebrae.atividade_produtos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sebrae.atividade_produtos.model.Produto;
import jakarta.persistence.LockModeType;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /** Carrega o produto com lock de escrita, evitando saldo errado em movimentações simultâneas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> findByIdForUpdate(@Param("id") Long id);

    Optional<Produto> findBySkuIgnoreCase(String sku);

    long countByCategoriaId(Long categoriaId);

    List<Produto> findByAtivoTrueOrderByNomeAsc();

    @Query("""
            select p from Produto p
            where p.ativo = true and p.quantidade <= p.estoqueMinimo
            order by p.quantidade asc, p.nome asc
            """)
    List<Produto> findAbaixoDoMinimo();

    /**
     * Busca com filtros. Para evitar parâmetros nulos: {@code busca} vazia = sem filtro de texto e
     * {@code categoriaId} 0 = todas as categorias.
     */
    @Query("""
            select p from Produto p left join p.categoria c
            where (lower(p.nome) like lower(concat('%', :busca, '%'))
                   or lower(p.sku) like lower(concat('%', :busca, '%')))
              and (:categoriaId = 0L or c.id = :categoriaId)
              and (:somenteBaixo = false or p.quantidade <= p.estoqueMinimo)
              and (:incluirInativos = true or p.ativo = true)
            order by p.nome asc
            """)
    List<Produto> buscar(@Param("busca") String busca,
                         @Param("categoriaId") Long categoriaId,
                         @Param("somenteBaixo") boolean somenteBaixo,
                         @Param("incluirInativos") boolean incluirInativos);
}
