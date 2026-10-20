package br.edu.dombosco.ptdb.painel;

import br.edu.dombosco.ptdb.avaliacao.Avaliacao;
import br.edu.dombosco.ptdb.avaliacao.AvaliacaoRepository;
import br.edu.dombosco.ptdb.avaliacao.DecisaoAvaliacao;
import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.faq.ItemFaqRepository;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoRepository;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PainelService {

    public static final int LIMITE_LISTAS = 5;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    private final VideoRepository videoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ItemFaqRepository itemFaqRepository;

    public PainelService(VideoRepository videoRepository,
                         CategoriaRepository categoriaRepository,
                         AvaliacaoRepository avaliacaoRepository,
                         ItemFaqRepository itemFaqRepository) {
        this.videoRepository = videoRepository;
        this.categoriaRepository = categoriaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.itemFaqRepository = itemFaqRepository;
    }

    public IndicadoresPainel carregarIndicadores() {
        List<Video> ativos = videoRepository.findByAtivoTrueOrderByDataPublicacaoDescIdDesc();
        LocalDateTime seteDiasAtras = LocalDateTime.now().minusDays(7);

        long novosNaSemana = 0;
        for (Video video : ativos) {
            if (!video.getDataCadastro().isBefore(seteDiasAtras)) {
                novosNaSemana++;
            }
        }

        List<Video> aprovados = videoRepository.findByAtivoTrueAndStatus(VideoStatus.APROVADO);
        long categoriasSemAprovados = 0;
        for (Categoria categoria : categoriaRepository.findByAtivaTrueOrderByNomeAsc()) {
            boolean temAprovado = false;
            for (Video video : aprovados) {
                if (video.getCategoria().getId().equals(categoria.getId())) {
                    temAprovado = true;
                }
            }
            if (!temAprovado) {
                categoriasSemAprovados++;
            }
        }

        IndicadoresPainel indicadores = new IndicadoresPainel();
        indicadores.setVideosCadastrados(ativos.size());
        indicadores.setVideosNovosNaSemana(novosNaSemana);
        indicadores.setVideosAguardando(videoRepository.countByAtivoTrueAndStatus(VideoStatus.AGUARDANDO_AVALIACAO));
        indicadores.setCategorias(categoriaRepository.countByAtivaTrue());
        indicadores.setCategoriasSemAprovados(categoriasSemAprovados);
        indicadores.setItensFaq(itemFaqRepository.count());
        return indicadores;
    }

    public List<AtividadeRecente> atividadesRecentes() {
        List<AtividadeRecente> todas = new ArrayList<>();
        for (Video video : videoRepository.findAll()) {
            todas.add(new AtividadeRecente(video.getTitulo(), tipoDoVideo(video), ultimaAlteracao(video)));
        }
        for (Avaliacao avaliacao : avaliacaoRepository.findTop5ByOrderByDataDesc()) {
            todas.add(new AtividadeRecente(avaliacao.getVideo().getTitulo(), tipoDaAvaliacao(avaliacao), avaliacao.getData()));
        }

        LocalDate hoje = LocalDate.now();
        List<AtividadeRecente> recentes = new ArrayList<>();
        while (!todas.isEmpty() && recentes.size() < LIMITE_LISTAS) {
            AtividadeRecente maisRecente = todas.get(0);
            for (AtividadeRecente atividade : todas) {
                if (atividade.getMomento().isAfter(maisRecente.getMomento())) {
                    maisRecente = atividade;
                }
            }
            todas.remove(maisRecente);
            maisRecente.setQuando(formatarMomento(maisRecente.getMomento(), hoje));
            recentes.add(maisRecente);
        }
        return recentes;
    }

    public List<ItemFilaAvaliacao> filaDeAvaliacao() {
        LocalDate hoje = LocalDate.now();
        List<ItemFilaAvaliacao> itens = new ArrayList<>();
        List<Video> fila = videoRepository.findByAtivoTrueAndStatusOrderByDataCadastroAsc(VideoStatus.AGUARDANDO_AVALIACAO);
        for (Video video : fila) {
            if (itens.size() == LIMITE_LISTAS) {
                break;
            }
            String enviado = formatarEnvio(ultimaAlteracao(video).toLocalDate(), hoje);
            itens.add(new ItemFilaAvaliacao(video.getId(), video.getTitulo(), video.getCategoria().getNome(), enviado));
        }
        return itens;
    }

    public static String formatarMomento(LocalDateTime momento, LocalDate hoje) {
        long dias = ChronoUnit.DAYS.between(momento.toLocalDate(), hoje);
        if (dias == 0) {
            return "Hoje, " + momento.format(HORA);
        }
        if (dias == 1) {
            return "Ontem, " + momento.format(HORA);
        }
        return momento.format(DATA_HORA);
    }

    public static String formatarEnvio(LocalDate envio, LocalDate hoje) {
        long dias = ChronoUnit.DAYS.between(envio, hoje);
        if (dias <= 0) {
            return "enviado hoje";
        }
        if (dias == 1) {
            return "enviado ontem";
        }
        return "enviado há " + dias + " dias";
    }

    private TipoAtividade tipoDoVideo(Video video) {
        if (!video.isAtivo()) {
            return TipoAtividade.EXCLUSAO;
        }
        if (video.getDataAtualizacao() != null) {
            return TipoAtividade.EDICAO;
        }
        return TipoAtividade.CADASTRO;
    }

    private TipoAtividade tipoDaAvaliacao(Avaliacao avaliacao) {
        if (avaliacao.getDecisao() == DecisaoAvaliacao.APROVADO) {
            return TipoAtividade.APROVACAO;
        }
        if (avaliacao.getDecisao() == DecisaoAvaliacao.REPROVADO) {
            return TipoAtividade.REPROVACAO;
        }
        return TipoAtividade.AJUSTES;
    }

    private LocalDateTime ultimaAlteracao(Video video) {
        if (video.getDataAtualizacao() != null) {
            return video.getDataAtualizacao();
        }
        return video.getDataCadastro();
    }
}
