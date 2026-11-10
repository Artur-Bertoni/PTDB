package br.edu.dombosco.ptdb.avaliacao;

import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.video.Video;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avaliacao")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/fila")
    public List<VideoParaAvaliar> fila() {
        List<VideoParaAvaliar> lista = new ArrayList<>();
        for (Video video : avaliacaoService.fila()) {
            List<AvaliacaoResumo> historico = new ArrayList<>();
            for (Avaliacao avaliacao : avaliacaoService.historicoDoVideo(video.getId())) {
                historico.add(new AvaliacaoResumo(avaliacao));
            }
            lista.add(new VideoParaAvaliar(video, historico));
        }
        return lista;
    }

    @GetMapping("/historico")
    public List<AvaliacaoResumo> historico() {
        List<AvaliacaoResumo> lista = new ArrayList<>();
        for (Avaliacao avaliacao : avaliacaoService.historico()) {
            lista.add(new AvaliacaoResumo(avaliacao));
        }
        return lista;
    }

    @PostMapping("/{videoId}")
    public ResponseEntity<Object> avaliar(@PathVariable Long videoId,
                                          @RequestBody AvaliacaoForm form,
                                          HttpSession session) {
        Map<String, String> erros = avaliacaoService.validar(form);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros);
        }
        UsuarioLogado avaliador = UsuarioSessao.buscar(session);
        Avaliacao avaliacao = avaliacaoService.avaliar(videoId, avaliador.getId(), form.getDecisao(), form.getParecer());
        String mensagem = "\"" + avaliacao.getVideo().getTitulo() + "\": " + form.getDecisao().getMensagem();
        return ResponseEntity.ok(Map.of("mensagem", mensagem));
    }
}
