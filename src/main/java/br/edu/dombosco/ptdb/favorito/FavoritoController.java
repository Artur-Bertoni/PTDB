package br.edu.dombosco.ptdb.favorito;

import br.edu.dombosco.ptdb.catalogo.VideoResumo;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.video.Video;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    public List<VideoResumo> listar(HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        List<VideoResumo> lista = new ArrayList<>();
        for (Video video : favoritoService.listar(usuario.getId())) {
            lista.add(new VideoResumo(video, true));
        }
        return lista;
    }

    @PostMapping("/{videoId}")
    public Map<String, Boolean> alternar(@PathVariable Long videoId, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        boolean favorito = favoritoService.alternar(usuario.getId(), videoId);
        return Map.of("favorito", favorito);
    }
}
