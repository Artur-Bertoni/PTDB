package br.edu.dombosco.ptdb.painel;

import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoRepository;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PainelService {

    static final int LIMITE_LISTAS = 5;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    private final VideoRepository videoRepository;
    private final CategoriaRepository categoriaRepository;
    private final Clock clock;

    public PainelService(VideoRepository videoRepository, CategoriaRepository categoriaRepository, Clock clock) {
        this.videoRepository = videoRepository;
        this.categoriaRepository = categoriaRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public IndicadoresPainel carregarIndicadores() {
        return new IndicadoresPainel(
                videoRepository.countByAtivoTrue(),
                videoRepository.countByAtivoTrueAndDataCadastroGreaterThanEqual(LocalDateTime.now(clock).minusDays(7)),
                videoRepository.countByAtivoTrueAndStatus(VideoStatus.AGUARDANDO_AVALIACAO),
                categoriaRepository.count(),
                categoriaRepository.contarSemVideosNoStatus(VideoStatus.APROVADO));
    }

    @Transactional(readOnly = true)
    public List<AtividadeRecente> atividadesRecentes() {
        LocalDate hoje = LocalDate.now(clock);
        return videoRepository.buscarMaisRecentes(PageRequest.of(0, LIMITE_LISTAS)).stream()
                .map(video -> paraAtividade(video, hoje))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemFilaAvaliacao> filaDeAvaliacao() {
        LocalDate hoje = LocalDate.now(clock);
        return videoRepository
                .buscarAtivosPorStatusEmOrdemDeEnvio(VideoStatus.AGUARDANDO_AVALIACAO, PageRequest.of(0, LIMITE_LISTAS))
                .stream()
                .map(video -> new ItemFilaAvaliacao(
                        video.getId(),
                        video.getTitulo(),
                        video.getCategoria().getNome(),
                        formatarEnvio(ultimaAlteracao(video).toLocalDate(), hoje)))
                .toList();
    }

    static String formatarMomento(LocalDateTime momento, LocalDate hoje) {
        long dias = ChronoUnit.DAYS.between(momento.toLocalDate(), hoje);
        if (dias == 0) {
            return "Hoje, " + momento.format(HORA);
        }
        if (dias == 1) {
            return "Ontem, " + momento.format(HORA);
        }
        return momento.format(DATA_HORA);
    }

    static String formatarEnvio(LocalDate envio, LocalDate hoje) {
        long dias = ChronoUnit.DAYS.between(envio, hoje);
        if (dias <= 0) {
            return "enviado hoje";
        }
        if (dias == 1) {
            return "enviado ontem";
        }
        return "enviado há " + dias + " dias";
    }

    private static AtividadeRecente paraAtividade(Video video, LocalDate hoje) {
        TipoAtividade tipo;
        if (!video.isAtivo()) {
            tipo = TipoAtividade.EXCLUSAO;
        } else if (video.getDataAtualizacao() != null) {
            tipo = TipoAtividade.EDICAO;
        } else {
            tipo = TipoAtividade.CADASTRO;
        }
        return new AtividadeRecente(video.getTitulo(), tipo, formatarMomento(ultimaAlteracao(video), hoje));
    }

    private static LocalDateTime ultimaAlteracao(Video video) {
        return video.getDataAtualizacao() != null ? video.getDataAtualizacao() : video.getDataCadastro();
    }
}
