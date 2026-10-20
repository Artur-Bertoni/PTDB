package br.edu.dombosco.ptdb.favorito;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    Favorito findByUsuarioIdAndVideoId(Long usuarioId, Long videoId);

    List<Favorito> findByUsuarioIdOrderByDataRegistroDesc(Long usuarioId);

    List<Favorito> findByVideoId(Long videoId);
}
