package br.edu.dombosco.ptdb.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.favorito.FavoritoService;
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
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockHttpSession aluno;
    private Long alunoId;
    private Long administradorId;
    private Categoria categoria;
    private Categoria outraCategoria;

    @BeforeEach
    void setUp() {
        aluno = Sessoes.aluno(usuarioRepository);
        alunoId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ALUNO).getId();
        administradorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR).getId();
        categoria = categoriaRepository.findAll().get(0);
        outraCategoria = categoriaRepository.findAll().get(1);
    }

    @Test
    void catalogoMostraSomenteVideosAprovados() throws Exception {
        criar("Vídeo aprovado", categoria, VideoStatus.APROVADO);
        criar("Vídeo pendente", categoria, VideoStatus.AGUARDANDO_AVALIACAO);

        mockMvc.perform(get("/catalogo").session(aluno))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Catálogo de vídeos")))
                .andExpect(content().string(containsString("Vídeo aprovado")))
                .andExpect(content().string(not(containsString("Vídeo pendente"))));
    }

    @Test
    void catalogoFiltraPorCategoria() throws Exception {
        criar("Aula da primeira categoria", categoria, VideoStatus.APROVADO);
        criar("Aula da segunda categoria", outraCategoria, VideoStatus.APROVADO);

        mockMvc.perform(get("/catalogo").param("categoriaId", outraCategoria.getId().toString()).session(aluno))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aula da segunda categoria")))
                .andExpect(content().string(not(containsString("Aula da primeira categoria"))));
    }

    @Test
    void paginaDoVideoMostraOPlayer() throws Exception {
        Video video = criar("Aula assistida", categoria, VideoStatus.APROVADO);

        mockMvc.perform(get("/videos/{id}", video.getId()).session(aluno))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aula assistida")))
                .andExpect(content().string(containsString("player.vimeo.com/video/1")));
    }

    @Test
    void alunoNaoVeVideoPendente() throws Exception {
        Video video = criar("Aula pendente", categoria, VideoStatus.AGUARDANDO_AVALIACAO);

        mockMvc.perform(get("/videos/{id}", video.getId()).session(aluno)).andExpect(status().isNotFound());
    }

    @Test
    void favoritarVoltaParaAPaginaAnterior() throws Exception {
        Video video = criar("Aula favorita", categoria, VideoStatus.APROVADO);

        mockMvc.perform(post("/favoritos/{id}", video.getId()).session(aluno)
                        .header("Referer", "http://localhost/catalogo?categoriaId=1"))
                .andExpect(redirectedUrl("/catalogo?categoriaId=1"));
        assertThat(favoritoService.favoritado(alunoId, video.getId())).isTrue();

        mockMvc.perform(get("/favoritos").session(aluno))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aula favorita")))
                .andExpect(content().string(containsString("1 vídeo favoritado")));
    }

    @Test
    void refererDeOutroSiteNaoERedirecionado() throws Exception {
        Video video = criar("Aula", categoria, VideoStatus.APROVADO);

        mockMvc.perform(post("/favoritos/{id}", video.getId()).session(aluno).header("Referer", "//site-malicioso.com"))
                .andExpect(redirectedUrl("/"));
    }

    private Video criar(String titulo, Categoria categoriaDoVideo, VideoStatus status) {
        VideoForm form = new VideoForm();
        form.setTitulo(titulo);
        form.setDescricao("Descrição");
        form.setAutor("Prof. Teste");
        form.setCategoriaId(categoriaDoVideo.getId());
        form.setDataPublicacao(LocalDate.of(2026, 10, 1));
        form.setOrigem(OrigemVideo.LINK);
        form.setLink("https://vimeo.com/1");
        Video video = videoService.criar(form, administradorId);
        video.setStatus(status);
        return videoRepository.save(video);
    }
}
