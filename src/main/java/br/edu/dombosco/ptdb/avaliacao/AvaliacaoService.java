package br.edu.dombosco.ptdb.avaliacao;

import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoRepository;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final VideoRepository videoRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            VideoRepository videoRepository,
                            UsuarioRepository usuarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.videoRepository = videoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Video> fila() {
        return videoRepository.findByAtivoTrueAndStatusOrderByDataCadastroAsc(VideoStatus.AGUARDANDO_AVALIACAO);
    }

    public Map<String, String> validar(AvaliacaoForm form) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (form.getDecisao() == null) {
            erros.put("decisao", "Escolha uma decisão.");
        }
        if (form.getParecer() == null || form.getParecer().isBlank()) {
            erros.put("parecer", "O parecer é obrigatório para qualquer decisão.");
        } else if (form.getParecer().length() > 4000) {
            erros.put("parecer", "O parecer deve ter no máximo 4000 caracteres.");
        }
        return erros;
    }

    @Transactional
    public Avaliacao avaliar(Long videoId, Long avaliadorId, DecisaoAvaliacao decisao, String parecer) {
        if (decisao == null) {
            throw new RegraNegocioException("Escolha uma decisão.");
        }
        if (parecer == null || parecer.isBlank()) {
            throw new RegraNegocioException("O parecer é obrigatório para qualquer decisão.");
        }
        Video video = videoRepository.findByIdAndAtivoTrue(videoId);
        if (video == null) {
            throw new RegraNegocioException("Vídeo não encontrado.");
        }
        if (video.getStatus() != VideoStatus.AGUARDANDO_AVALIACAO) {
            throw new RegraNegocioException("Este vídeo não está aguardando avaliação.");
        }

        video.setStatus(decisao.getStatusResultante());
        videoRepository.save(video);

        Usuario avaliador = usuarioRepository.findById(avaliadorId).orElse(null);
        Avaliacao avaliacao = new Avaliacao(video, avaliador, decisao, parecer.trim(), LocalDateTime.now());
        return avaliacaoRepository.save(avaliacao);
    }

    public List<Avaliacao> historicoDoVideo(Long videoId) {
        return avaliacaoRepository.findByVideoIdOrderByDataDesc(videoId);
    }

    public List<Avaliacao> historico() {
        return avaliacaoRepository.findAllByOrderByDataDesc();
    }

    public Avaliacao ultimaDoVideo(Long videoId) {
        return avaliacaoRepository.findFirstByVideoIdOrderByDataDesc(videoId);
    }
}
