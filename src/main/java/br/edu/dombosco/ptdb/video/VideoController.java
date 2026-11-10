package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.avaliacao.Avaliacao;
import br.edu.dombosco.ptdb.avaliacao.AvaliacaoService;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestao/videos")
public class VideoController {

    private final VideoService videoService;
    private final AvaliacaoService avaliacaoService;

    public VideoController(VideoService videoService, AvaliacaoService avaliacaoService) {
        this.videoService = videoService;
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public List<VideoGestao> listar(HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        List<VideoGestao> lista = new ArrayList<>();
        for (Video video : videoService.listarParaGestao(usuario)) {
            lista.add(montar(video, usuario));
        }
        return lista;
    }

    @GetMapping("/{id}")
    public VideoGestao buscar(@PathVariable Long id, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        return montar(videoService.buscarParaGestao(id, usuario), usuario);
    }

    @PostMapping
    public ResponseEntity<Object> criar(VideoForm form, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        Map<String, String> erros = videoService.validar(form, null);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros);
        }
        videoService.criar(form, usuario);
        return ResponseEntity.ok(Map.of("mensagem", mensagemSalvo(usuario, "Vídeo cadastrado")));
    }

    @PostMapping("/{id}")
    public ResponseEntity<Object> atualizar(@PathVariable Long id, VideoForm form, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        Video atual = videoService.buscarParaGestao(id, usuario);
        Map<String, String> erros = videoService.validar(form, atual);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros);
        }
        videoService.atualizar(id, form, usuario);
        return ResponseEntity.ok(Map.of("mensagem", mensagemSalvo(usuario, "Vídeo atualizado")));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> excluir(@PathVariable Long id, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        videoService.excluir(id, usuario);
        return Map.of("mensagem", "Vídeo excluído.");
    }

    private VideoGestao montar(Video video, UsuarioLogado usuario) {
        String parecer = null;
        Avaliacao ultima = avaliacaoService.ultimaDoVideo(video.getId());
        if (ultima != null) {
            parecer = ultima.getParecer();
        }
        return new VideoGestao(video, usuario.isAdministrador(), parecer);
    }

    private String mensagemSalvo(UsuarioLogado usuario, String inicio) {
        if (usuario.isAdministrador()) {
            return inicio + " e publicado.";
        }
        return inicio + ". Ele ficará aguardando avaliação.";
    }
}
