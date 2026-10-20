package br.edu.dombosco.ptdb.catalogo;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaService;
import br.edu.dombosco.ptdb.favorito.FavoritoService;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CatalogoController {

    private static final int LIMITE_RELACIONADOS = 4;

    private final VideoService videoService;
    private final CategoriaService categoriaService;
    private final FavoritoService favoritoService;

    public CatalogoController(VideoService videoService,
                              CategoriaService categoriaService,
                              FavoritoService favoritoService) {
        this.videoService = videoService;
        this.categoriaService = categoriaService;
        this.favoritoService = favoritoService;
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String termo,
                           @RequestParam(required = false) Long categoriaId,
                           HttpSession session,
                           Model model) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        List<Categoria> categorias = categoriaService.listarAtivas();

        Categoria selecionada = null;
        for (Categoria categoria : categorias) {
            if (categoria.getId().equals(categoriaId)) {
                selecionada = categoria;
            }
        }

        Long filtro = null;
        if (selecionada != null) {
            filtro = selecionada.getId();
        }
        List<Video> videos = videoService.listarPublicados(termo, filtro);

        Map<Long, Long> contagem = videoService.contarPublicadosPorCategoria();
        long totalPublicados = 0;
        for (Long quantidade : contagem.values()) {
            totalPublicados = totalPublicados + quantidade;
        }

        model.addAttribute("categorias", categorias);
        model.addAttribute("contagemPorCategoria", contagem);
        model.addAttribute("totalPublicados", totalPublicados);
        model.addAttribute("categoriaSelecionada", selecionada);
        model.addAttribute("termo", termo);
        model.addAttribute("videos", videos);
        model.addAttribute("favoritos", favoritoService.idsFavoritados(usuario));
        return "catalogo/catalogo";
    }

    @GetMapping("/videos/{id}")
    public String exibir(@PathVariable Long id, HttpSession session, Model model) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        Video video = videoService.buscarParaExibir(id, usuario);

        List<Video> relacionados = new ArrayList<>();
        if (video.isPublicado()) {
            List<Video> mesmaCategoria = videoService.listarPublicados(null, video.getCategoria().getId());
            for (Video outro : mesmaCategoria) {
                if (!outro.getId().equals(id) && relacionados.size() < LIMITE_RELACIONADOS) {
                    relacionados.add(outro);
                }
            }
        }

        model.addAttribute("video", video);
        model.addAttribute("relacionados", relacionados);
        model.addAttribute("favoritos", favoritoService.idsFavoritados(usuario));
        return "catalogo/assistir";
    }
}
