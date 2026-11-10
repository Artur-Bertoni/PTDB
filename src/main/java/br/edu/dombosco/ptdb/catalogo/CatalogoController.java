package br.edu.dombosco.ptdb.catalogo;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaResumo;
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
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
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

    @GetMapping("/videos")
    public List<VideoResumo> listarVideos(HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        Set<Long> favoritos = favoritoService.idsFavoritados(usuario);
        List<VideoResumo> resumos = new ArrayList<>();
        for (Video video : videoService.listarPublicados()) {
            resumos.add(new VideoResumo(video, favoritos.contains(video.getId())));
        }
        return resumos;
    }

    @GetMapping("/videos/{id}")
    public VideoDetalhe exibir(@PathVariable Long id, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        Video video = videoService.buscarParaExibir(id, usuario);
        videoService.registrarVisualizacao(video);
        Set<Long> favoritos = favoritoService.idsFavoritados(usuario);

        List<VideoResumo> relacionados = new ArrayList<>();
        if (video.isPublicado()) {
            for (Video outro : videoService.listarPublicados()) {
                boolean mesmaCategoria = outro.getCategoria().getId().equals(video.getCategoria().getId());
                if (mesmaCategoria && !outro.getId().equals(id) && relacionados.size() < LIMITE_RELACIONADOS) {
                    relacionados.add(new VideoResumo(outro, favoritos.contains(outro.getId())));
                }
            }
        }
        return new VideoDetalhe(video, favoritos.contains(id), usuario.isAdministrador(), relacionados);
    }

    @GetMapping("/categorias")
    public List<CategoriaResumo> listarCategorias() {
        Map<Long, Long> contagem = videoService.contarPublicadosPorCategoria();
        List<CategoriaResumo> lista = new ArrayList<>();
        for (Categoria categoria : categoriaService.listarAtivas()) {
            lista.add(new CategoriaResumo(categoria, contagem.get(categoria.getId())));
        }
        return lista;
    }
}
