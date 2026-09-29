package br.edu.dombosco.ptdb.categoria;

import br.edu.dombosco.ptdb.video.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    @Query("""
            select count(c) from Categoria c
            where not exists (
                select 1 from Video v
                where v.categoria = c and v.ativo = true and v.status = :status)
            """)
    long contarSemVideosNoStatus(@Param("status") VideoStatus status);
}
