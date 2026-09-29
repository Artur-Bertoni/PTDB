package br.edu.dombosco.ptdb.video;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByIdAndAtivoTrue(Long id);

    Optional<Video> findFirstByOrigemVideoAndArquivoLink(OrigemVideo origemVideo, String arquivoLink);

    Optional<Video> findFirstByImagemCapa(String imagemCapa);

    long countByAtivoTrue();

    long countByAtivoTrueAndStatus(VideoStatus status);

    long countByAtivoTrueAndDataCadastroGreaterThanEqual(LocalDateTime inicio);

    @Query("""
            select v from Video v
            order by coalesce(v.dataAtualizacao, v.dataCadastro) desc, v.id desc
            """)
    List<Video> buscarMaisRecentes(Pageable pagina);

    @Query("""
            select v from Video v
            where v.ativo = true and v.status = :status
            order by coalesce(v.dataAtualizacao, v.dataCadastro) asc, v.id asc
            """)
    List<Video> buscarAtivosPorStatusEmOrdemDeEnvio(@Param("status") VideoStatus status, Pageable pagina);

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
