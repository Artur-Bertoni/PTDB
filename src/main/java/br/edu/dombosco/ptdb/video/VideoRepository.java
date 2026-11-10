package br.edu.dombosco.ptdb.video;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Video findByIdAndAtivoTrue(Long id);

    Video findFirstByOrigemVideoAndArquivoLink(OrigemVideo origemVideo, String arquivoLink);

    Video findFirstByImagemCapa(String imagemCapa);

    List<Video> findByAtivoTrueOrderByDataPublicacaoDescIdDesc();

    List<Video> findByAtivoTrueAndCadastradoPorIdOrderByDataPublicacaoDescIdDesc(Long usuarioId);

    List<Video> findByAtivoTrueAndStatusOrderByDataPublicacaoDescIdDesc(VideoStatus status);

    List<Video> findByAtivoTrueAndStatus(VideoStatus status);

    List<Video> findByAtivoTrueAndStatusOrderByDataCadastroAsc(VideoStatus status);

    boolean existsByCategoriaIdAndAtivoTrue(Long categoriaId);

    long countByAtivoTrueAndStatus(VideoStatus status);
}
