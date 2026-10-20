package br.edu.dombosco.ptdb.painel;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.dombosco.ptdb.avaliacao.AvaliacaoService;
import br.edu.dombosco.ptdb.avaliacao.DecisaoAvaliacao;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.OrigemVideo;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoForm;
import br.edu.dombosco.ptdb.video.VideoService;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@Transactional
class PainelServiceTest {

    @Autowired
    private PainelService painelService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long categoriaId;
    private Long administradorId;
    private Long avaliadorId;

    @BeforeEach
    void setUp() {
        categoriaId = categoriaRepository.findAll().get(0).getId();
        administradorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR).getId();
        avaliadorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.AVALIADOR).getId();
    }

    @Test
    void indicadoresContamVideosAguardandoENovosNaSemana() {
        IndicadoresPainel antes = painelService.carregarIndicadores();
        Video aprovado = videoService.criar(form("Aprovado"), administradorId);
        aprovado.setStatus(VideoStatus.APROVADO);
        videoService.criar(form("Pendente"), administradorId);
        Video excluido = videoService.criar(form("Excluído"), administradorId);
        videoService.excluir(excluido.getId());

        IndicadoresPainel depois = painelService.carregarIndicadores();

        assertThat(depois.getVideosCadastrados() - antes.getVideosCadastrados()).isEqualTo(2);
        assertThat(depois.getVideosNovosNaSemana() - antes.getVideosNovosNaSemana()).isEqualTo(2);
        assertThat(depois.getVideosAguardando() - antes.getVideosAguardando()).isEqualTo(1);
        assertThat(depois.getCategorias()).isEqualTo(categoriaRepository.countByAtivaTrue());
        assertThat(depois.getItensFaq()).isGreaterThan(0);
    }

    @Test
    void categoriaComVideoAprovadoDeixaDeContarComoSemAprovados() {
        long antes = painelService.carregarIndicadores().getCategoriasSemAprovados();
        Video video = videoService.criar(form("Aula"), administradorId);
        video.setStatus(VideoStatus.APROVADO);

        long depois = painelService.carregarIndicadores().getCategoriasSemAprovados();

        assertThat(depois).isEqualTo(antes - 1);
    }

    @Test
    void atividadeRecenteIdentificaCadastroEdicaoExclusaoEAvaliacao() {
        Video cadastrado = videoService.criar(form("Cadastrado"), administradorId);
        Video editado = videoService.criar(form("Editado"), administradorId);
        videoService.atualizar(editado.getId(), form("Editado"));
        Video excluido = videoService.criar(form("Excluído"), administradorId);
        videoService.excluir(excluido.getId());
        Video avaliado = videoService.criar(form("Avaliado"), administradorId);
        avaliacaoService.avaliar(avaliado.getId(), avaliadorId, DecisaoAvaliacao.APROVADO, "Conteúdo adequado.");

        List<AtividadeRecente> atividades = painelService.atividadesRecentes();

        assertThat(atividades).hasSizeLessThanOrEqualTo(PainelService.LIMITE_LISTAS);
        List<String> descricoes = new ArrayList<>();
        for (AtividadeRecente atividade : atividades) {
            descricoes.add(atividade.getTitulo() + " " + atividade.getTipo());
        }
        assertThat(descricoes).contains(
                cadastrado.getTitulo() + " " + TipoAtividade.CADASTRO,
                "Editado " + TipoAtividade.EDICAO,
                "Excluído " + TipoAtividade.EXCLUSAO,
                "Avaliado " + TipoAtividade.APROVACAO);
    }

    @Test
    void filaDeAvaliacaoTrazSomentePendentesAtivos() {
        videoService.criar(form("Na fila"), administradorId);
        Video aprovado = videoService.criar(form("Aprovado"), administradorId);
        aprovado.setStatus(VideoStatus.APROVADO);
        Video excluido = videoService.criar(form("Excluído"), administradorId);
        videoService.excluir(excluido.getId());

        List<String> titulos = new ArrayList<>();
        for (ItemFilaAvaliacao item : painelService.filaDeAvaliacao()) {
            titulos.add(item.getTitulo());
        }

        assertThat(titulos).contains("Na fila").doesNotContain("Aprovado", "Excluído");
    }


    @Test
    void formataMomentosRelativosAoDiaAtual() {
        LocalDate hoje = LocalDate.of(2026, 9, 29);

        assertThat(PainelService.formatarMomento(LocalDateTime.of(2026, 9, 29, 9, 42), hoje)).isEqualTo("Hoje, 09:42");
        assertThat(PainelService.formatarMomento(LocalDateTime.of(2026, 9, 28, 17, 15), hoje)).isEqualTo("Ontem, 17:15");
        assertThat(PainelService.formatarMomento(LocalDateTime.of(2026, 9, 5, 14, 20), hoje)).isEqualTo("05/09/2026, 14:20");

        assertThat(PainelService.formatarEnvio(hoje, hoje)).isEqualTo("enviado hoje");
        assertThat(PainelService.formatarEnvio(hoje.minusDays(1), hoje)).isEqualTo("enviado ontem");
        assertThat(PainelService.formatarEnvio(hoje.minusDays(3), hoje)).isEqualTo("enviado há 3 dias");
    }

    private VideoForm form(String titulo) {
        VideoForm form = new VideoForm();
        form.setTitulo(titulo);
        form.setDescricao("Descrição");
        form.setAutor("Prof. Teste");
        form.setCategoriaId(categoriaId);
        form.setDataPublicacao(LocalDate.of(2026, 9, 29));
        form.setOrigem(OrigemVideo.LINK);
        form.setLink("https://vimeo.com/1");
        return form;
    }
}
