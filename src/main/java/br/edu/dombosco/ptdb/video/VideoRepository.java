package br.edu.dombosco.ptdb.video;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio da entidade {@link Video} (RF06 / RF10).
 *
 * <p>As consultas consideram apenas registros ativos, pois a exclusao do
 * RF06 e logica.
 */
public interface VideoRepository extends JpaRepository<Video, Long> {

    /** Busca um video ativo pelo id (usado em consulta, edicao e exclusao). */
    Optional<Video> findByIdAndAtivoTrue(Long id);

    /**
     * Listagem com filtro por texto (titulo/descricao), categoria e status.
     * Parametros nulos sao ignorados. Retorna somente videos ativos.
     */
    @Query("""
            select v from Video v
            where v.ativo = true
              and (:termo is null
                   or lower(v.titulo) like lower(concat('%', :termo, '%'))
                   or lower(v.descricao) like lower(concat('%', :termo, '%')))
              and (:categoriaId is null or v.categoria.id = :categoriaId)
              and (:status is null or v.status = :status)
            order by v.dataPublicacao desc, v.id desc
            """)
    List<Video> buscar(@Param("termo") String termo,
                       @Param("categoriaId") Long categoriaId,
                       @Param("status") VideoStatus status);
}
