package br.edu.dombosco.ptdb.favorito;

import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.web.Redirecionamento;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    public String listar(HttpSession session, Model model) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        List<Video> videos = favoritoService.listar(usuario.getId());
        model.addAttribute("videos", videos);
        model.addAttribute("favoritos", favoritoService.idsFavoritados(usuario));
        return "favoritos/lista";
    }

    @PostMapping("/{videoId}")
    public String alternar(@PathVariable Long videoId, HttpServletRequest request) {
        UsuarioLogado usuario = UsuarioSessao.buscar(request);
        favoritoService.alternar(usuario.getId(), videoId);
        return "redirect:" + Redirecionamento.paginaAnterior(request);
    }
}
