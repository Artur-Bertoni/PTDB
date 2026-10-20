package br.edu.dombosco.ptdb.avaliacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.OrigemVideo;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoForm;
import br.edu.dombosco.ptdb.video.VideoRepository;
import br.edu.dombosco.ptdb.video.VideoService;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@AutoConfigureMockMvc
@Transactional
class AvaliacaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockHttpSession avaliador;
    private Long avaliadorId;
    private Long administradorId;
    private Long categoriaId;

    @BeforeEach
    void setUp() {
        avaliador = Sessoes.avaliador(usuarioRepository);
        avaliadorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.AVALIADOR).getId();
        administradorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR).getId();
        categoriaId = categoriaRepository.findAll().get(0).getId();
    }

    @Test
    void aprovarPublicaOVideoEGuardaOHistorico() {
        Video video = videoService.criar(form("Aula para aprovar"), administradorId);

        avaliacaoService.avaliar(video.getId(), avaliadorId, DecisaoAvaliacao.APROVADO, "Conteúdo adequado.");

        assertThat(videoService.buscarPorId(video.getId()).getStatus()).isEqualTo(VideoStatus.APROVADO);
        assertThat(avaliacaoService.historicoDoVideo(video.getId())).hasSize(1);
        Avaliacao avaliacao = avaliacaoService.ultimaDoVideo(video.getId());
        assertThat(avaliacao.getParecer()).isEqualTo("Conteúdo adequado.");
        assertThat(avaliacao.getAvaliador().getId()).isEqualTo(avaliadorId);
        assertThat(avaliacao.getData()).isNotNull();
    }

    @Test
    void parecerEObrigatorio() {
        Video video = videoService.criar(form("Aula"), administradorId);

        assertThatThrownBy(() -> avaliacaoService.avaliar(video.getId(), avaliadorId, DecisaoAvaliacao.REPROVADO, " "))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void somenteVideoAguardandoPodeSerAvaliado() {
        Video video = videoService.criar(form("Aula"), administradorId);
        avaliacaoService.avaliar(video.getId(), avaliadorId, DecisaoAvaliacao.REPROVADO, "Áudio ruim.");

        assertThatThrownBy(() -> avaliacaoService.avaliar(video.getId(), avaliadorId, DecisaoAvaliacao.APROVADO, "Ok"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void solicitarAjustesTiraODaFilaAteOAdministradorEditar() {
        Video video = videoService.criar(form("Aula com áudio ruim"), administradorId);

        avaliacaoService.avaliar(video.getId(), avaliadorId, DecisaoAvaliacao.AJUSTES_SOLICITADOS, "Melhore o áudio.");
        assertThat(avaliacaoService.fila()).doesNotContain(video);
        assertThat(videoService.buscarPorId(video.getId()).getStatus()).isEqualTo(VideoStatus.AJUSTES_SOLICITADOS);

        videoService.atualizar(video.getId(), form("Aula com áudio corrigido"));
        assertThat(avaliacaoService.fila()).contains(video);
    }

    @Test
    void telaDoAvaliadorMostraAFilaEPermiteDecidir() throws Exception {
        Video video = videoService.criar(form("Modelagem BPMN"), administradorId);

        mockMvc.perform(get("/avaliacao").param("video", video.getId().toString()).session(avaliador))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tela do avaliador")))
                .andExpect(content().string(containsString("Modelagem BPMN")))
                .andExpect(content().string(containsString("Solicitar ajustes")));

        mockMvc.perform(post("/avaliacao/{id}", video.getId()).session(avaliador)
                        .param("decisao", "APROVADO")
                        .param("parecer", "Muito bom."))
                .andExpect(redirectedUrl("/avaliacao"));

        assertThat(videoRepository.findById(video.getId()).get().getStatus()).isEqualTo(VideoStatus.APROVADO);

        mockMvc.perform(get("/avaliacao/historico").session(avaliador))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Muito bom.")));
    }

    @Test
    void decisaoSemParecerMostraErro() throws Exception {
        Video video = videoService.criar(form("Aula sem parecer"), administradorId);

        mockMvc.perform(post("/avaliacao/{id}", video.getId()).session(avaliador)
                        .param("decisao", "REPROVADO")
                        .param("parecer", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("O parecer é obrigatório")));
    }

    private VideoForm form(String titulo) {
        VideoForm form = new VideoForm();
        form.setTitulo(titulo);
        form.setDescricao("Descrição");
        form.setAutor("Prof. Teste");
        form.setCategoriaId(categoriaId);
        form.setDataPublicacao(LocalDate.of(2026, 10, 1));
        form.setOrigem(OrigemVideo.LINK);
        form.setLink("https://vimeo.com/1");
        return form;
    }
}
