package br.edu.dombosco.ptdb.favorito;

import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final VideoRepository videoRepository;
    private final UsuarioRepository usuarioRepository;

    public FavoritoService(FavoritoRepository favoritoRepository,
                           VideoRepository videoRepository,
                           UsuarioRepository usuarioRepository) {
        this.favoritoRepository = favoritoRepository;
        this.videoRepository = videoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean alternar(Long usuarioId, Long videoId) {
        Favorito existente = favoritoRepository.findByUsuarioIdAndVideoId(usuarioId, videoId);
        if (existente != null) {
            favoritoRepository.delete(existente);
            return false;
        }
        Video video = videoRepository.findByIdAndAtivoTrue(videoId);
        if (video == null || !video.isPublicado()) {
            throw new RegraNegocioException("Somente vídeos publicados podem ser favoritados.");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        favoritoRepository.save(new Favorito(usuario, video));
        return true;
    }

    public List<Video> listar(Long usuarioId) {
        List<Video> videos = new ArrayList<>();
        List<Favorito> favoritos = favoritoRepository.findByUsuarioIdOrderByDataRegistroDesc(usuarioId);
        for (Favorito favorito : favoritos) {
            if (favorito.getVideo().isPublicado()) {
                videos.add(favorito.getVideo());
            }
        }
        return videos;
    }

    public Set<Long> idsFavoritados(UsuarioLogado usuario) {
        Set<Long> ids = new HashSet<>();
        if (usuario == null || !usuario.isAluno()) {
            return ids;
        }
        List<Favorito> favoritos = favoritoRepository.findByUsuarioIdOrderByDataRegistroDesc(usuario.getId());
        for (Favorito favorito : favoritos) {
            ids.add(favorito.getVideo().getId());
        }
        return ids;
    }
}
