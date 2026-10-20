package br.edu.dombosco.ptdb.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@AutoConfigureMockMvc
@Transactional
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockHttpSession administrador;
    private Long administradorId;
    private Long categoriaId;

    @BeforeEach
    void setUp() {
        administrador = Sessoes.administrador(usuarioRepository);
        administradorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR).getId();
        categoriaId = categoriaRepository.findAll().get(0).getId();
    }

    @Test
    void areaAdministrativaExigePerfilAdministrador() throws Exception {
        mockMvc.perform(get("/admin/videos").session(Sessoes.aluno(usuarioRepository)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/videos").session(Sessoes.avaliador(usuarioRepository)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listagemExibeIndicadoresETabelaSemModal() throws Exception {
        videoService.criar(formComLink("https://vimeo.com/1"), administradorId);

        mockMvc.perform(get("/admin/videos").session(administrador))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aguardando avaliação")))
                .andExpect(content().string(containsString("class=\"tabela\"")))
                .andExpect(content().string(not(containsString("id=\"modal-video\""))));
    }

    @Test
    void novoVideoAbreOModal() throws Exception {
        mockMvc.perform(get("/admin/videos/novo").session(administrador))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"modal-video\"")))
                .andExpect(content().string(containsString("Novo vídeo")));
    }

    @Test
    void cadastroInvalidoReabreOModalComOsErros() throws Exception {
        mockMvc.perform(multipart("/admin/videos").session(administrador)
                        .param("titulo", "")
                        .param("descricao", "Descrição")
                        .param("autor", "Prof. Teste")
                        .param("categoriaId", categoriaId.toString())
                        .param("dataPublicacao", "2026-09-29")
                        .param("origem", "LINK")
                        .param("link", "nao-e-um-link"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"modal-video\"")))
                .andExpect(content().string(containsString("O título é obrigatório.")))
                .andExpect(content().string(containsString("Informe um link válido")));
    }

    @Test
    void cadastroComLinkRedirecionaParaAListagem() throws Exception {
        long antes = videoRepository.count();

        mockMvc.perform(multipart("/admin/videos").session(administrador)
                        .param("titulo", "Aula de Git")
                        .param("descricao", "Branches e pull requests")
                        .param("autor", "Prof. Teste")
                        .param("categoriaId", categoriaId.toString())
                        .param("dataPublicacao", "2026-09-29")
                        .param("duracao", "12:30")
                        .param("origem", "LINK")
                        .param("link", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/videos"));

        assertThat(videoRepository.count()).isEqualTo(antes + 1);
    }

    @Test
    void edicaoAbreOModalPreenchido() throws Exception {
        Video video = videoService.criar(formComLink("https://vimeo.com/42"), administradorId);

        mockMvc.perform(get("/admin/videos/{id}/editar", video.getId()).session(administrador))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar vídeo")))
                .andExpect(content().string(containsString("https://vimeo.com/42")));
    }

    @Test
    void arquivoDeVideoPendenteSoEServidoParaQuemPodeAvaliar() throws Exception {
        VideoForm form = formBase();
        form.setOrigem(OrigemVideo.ARQUIVO);
        form.setArquivoVideo(new MockMultipartFile("arquivoVideo", "aula.mp4", "video/mp4", new byte[] {1, 2, 3}));
        Video video = videoService.criar(form, administradorId);
        String url = "/midia/videos/" + video.getArquivoLink();
        MockHttpSession aluno = Sessoes.aluno(usuarioRepository);

        mockMvc.perform(get(url).session(aluno)).andExpect(status().isNotFound());
        mockMvc.perform(get(url).session(administrador)).andExpect(status().isOk());

        video.setStatus(VideoStatus.APROVADO);
        videoRepository.flush();
        mockMvc.perform(get(url).session(aluno)).andExpect(status().isOk());
    }

    private VideoForm formComLink(String link) {
        VideoForm form = formBase();
        form.setOrigem(OrigemVideo.LINK);
        form.setLink(link);
        return form;
    }

    private VideoForm formBase() {
        VideoForm form = new VideoForm();
        form.setTitulo("Vídeo de teste");
        form.setDescricao("Descrição de teste");
        form.setAutor("Prof. Teste");
        form.setCategoriaId(categoriaId);
        form.setDataPublicacao(LocalDate.of(2026, 9, 29));
        return form;
    }
}
