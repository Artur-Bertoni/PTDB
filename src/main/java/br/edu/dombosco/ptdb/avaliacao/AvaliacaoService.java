package br.edu.dombosco.ptdb.avaliacao;

import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoRepository;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

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

    public void validar(AvaliacaoForm form, Errors errors) {
        if (form.getParecer() == null || form.getParecer().isBlank()) {
            errors.rejectValue("parecer", "obrigatorio", "O parecer é obrigatório para qualquer decisão.");
        } else if (form.getParecer().length() > 4000) {
            errors.rejectValue("parecer", "tamanho", "O parecer deve ter no máximo 4000 caracteres.");
        }
        if (form.getDecisao() == null) {
            errors.rejectValue("decisao", "obrigatorio", "Escolha uma decisão.");
        }
    }

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

    public List<Avaliacao> maisRecentes() {
        return avaliacaoRepository.findTop5ByOrderByDataDesc();
    }

    public Avaliacao ultimaDoVideo(Long videoId) {
        return avaliacaoRepository.findFirstByVideoIdOrderByDataDesc(videoId);
    }

    public Map<Long, Avaliacao> ultimaPorVideo(List<Video> videos) {
        Map<Long, Avaliacao> ultimas = new HashMap<>();
        for (Video video : videos) {
            Avaliacao ultima = ultimaDoVideo(video.getId());
            if (ultima != null) {
                ultimas.put(video.getId(), ultima);
            }
        }
        return ultimas;
    }
}
