package br.edu.dombosco.ptdb.painel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.video.OrigemVideo;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoForm;
import br.edu.dombosco.ptdb.video.VideoService;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "ptdb.storage.location=target/test-uploads",
        "ptdb.dev.auto-admin=false"
})
@Transactional
class PainelServiceTest {

    @Autowired
    private PainelService painelService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Long categoriaId;

    @BeforeEach
    void setUp() {
        categoriaId = categoriaRepository.findAll().get(0).getId();
    }

    @Test
    void indicadoresContamVideosAguardandoENovosNaSemana() {
        IndicadoresPainel antes = painelService.carregarIndicadores();
        Video aprovado = videoService.criar(form("Aprovado"));
        aprovado.setStatus(VideoStatus.APROVADO);
        videoService.criar(form("Pendente"));
        Video excluido = videoService.criar(form("Excluído"));
        videoService.excluir(excluido.getId());

        IndicadoresPainel depois = painelService.carregarIndicadores();

        assertThat(depois.videosCadastrados() - antes.videosCadastrados()).isEqualTo(2);
        assertThat(depois.videosNovosNaSemana() - antes.videosNovosNaSemana()).isEqualTo(2);
        assertThat(depois.videosAguardando() - antes.videosAguardando()).isEqualTo(1);
        assertThat(depois.categorias()).isEqualTo(categoriaRepository.count());
    }

    @Test
    void categoriaComVideoAprovadoDeixaDeContarComoSemAprovados() {
        long antes = painelService.carregarIndicadores().categoriasSemAprovados();
        Video video = videoService.criar(form("Aula"));
        video.setStatus(VideoStatus.APROVADO);

        long depois = painelService.carregarIndicadores().categoriasSemAprovados();

        assertThat(depois).isEqualTo(antes - 1);
    }

    @Test
    void atividadeRecenteIdentificaCadastroEdicaoEExclusao() {
        Video cadastrado = videoService.criar(form("Cadastrado"));
        Video editado = videoService.criar(form("Editado"));
        videoService.atualizar(editado.getId(), form("Editado"));
        Video excluido = videoService.criar(form("Excluído"));
        videoService.excluir(excluido.getId());

        List<AtividadeRecente> atividades = painelService.atividadesRecentes();

        assertThat(atividades).hasSizeLessThanOrEqualTo(PainelService.LIMITE_LISTAS);
        assertThat(atividades)
                .extracting(AtividadeRecente::titulo, AtividadeRecente::tipo)
                .contains(
                        tuple(cadastrado.getTitulo(), TipoAtividade.CADASTRO),
                        tuple("Editado", TipoAtividade.EDICAO),
                        tuple("Excluído", TipoAtividade.EXCLUSAO));
    }

    @Test
    void filaDeAvaliacaoTrazSomentePendentesAtivos() {
        videoService.criar(form("Na fila"));
        Video aprovado = videoService.criar(form("Aprovado"));
        aprovado.setStatus(VideoStatus.APROVADO);
        Video excluido = videoService.criar(form("Excluído"));
        videoService.excluir(excluido.getId());

        assertThat(painelService.filaDeAvaliacao())
                .extracting(ItemFilaAvaliacao::titulo)
                .contains("Na fila")
                .doesNotContain("Aprovado", "Excluído");
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
        form.setCategoriaId(categoriaId);
        form.setDataPublicacao(LocalDate.of(2026, 9, 29));
        form.setOrigem(OrigemVideo.LINK);
        form.setLink("https://vimeo.com/1");
        return form;
    }
}
