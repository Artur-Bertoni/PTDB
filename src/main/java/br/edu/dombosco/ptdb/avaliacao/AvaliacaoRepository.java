package br.edu.dombosco.ptdb.avaliacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByVideoIdOrderByDataDesc(Long videoId);

    Avaliacao findFirstByVideoIdOrderByDataDesc(Long videoId);

    List<Avaliacao> findAllByOrderByDataDesc();

    List<Avaliacao> findTop5ByOrderByDataDesc();
}
