package br.edu.dombosco.ptdb.favorito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.OrigemVideo;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoForm;
import br.edu.dombosco.ptdb.video.VideoService;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@Transactional
class FavoritoServiceTest {

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long alunoId;
    private Long administradorId;
    private Long categoriaId;

    @BeforeEach
    void setUp() {
        alunoId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ALUNO).getId();
        administradorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR).getId();
        categoriaId = categoriaRepository.findAll().get(0).getId();
    }

    @Test
    void alternarMarcaEDesmarcaOFavorito() {
        Video video = criarAprovado();

        assertThat(favoritoService.alternar(alunoId, video.getId())).isTrue();
        assertThat(favoritoService.listar(alunoId)).contains(video);

        assertThat(favoritoService.alternar(alunoId, video.getId())).isFalse();
        assertThat(favoritoService.listar(alunoId)).doesNotContain(video);
    }

    @Test
    void videoNaoPublicadoNaoPodeSerFavoritado() {
        Video pendente = videoService.criar(form(), administradorId);

        assertThatThrownBy(() -> favoritoService.alternar(alunoId, pendente.getId()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void excluirVideoRemoveOsFavoritos() {
        Video video = criarAprovado();
        favoritoService.alternar(alunoId, video.getId());

        videoService.excluir(video.getId());

        assertThat(favoritoService.favoritado(alunoId, video.getId())).isFalse();
    }

    @Test
    void somenteAlunoTemIdsDeFavoritos() {
        Video video = criarAprovado();
        favoritoService.alternar(alunoId, video.getId());

        assertThat(favoritoService.idsFavoritados(Sessoes.usuario(usuarioRepository, PerfilUsuario.ALUNO)))
                .contains(video.getId());
        assertThat(favoritoService.idsFavoritados(Sessoes.usuario(usuarioRepository, PerfilUsuario.AVALIADOR)))
                .isEmpty();
    }

    private Video criarAprovado() {
        Video video = videoService.criar(form(), administradorId);
        video.setStatus(VideoStatus.APROVADO);
        return video;
    }

    private VideoForm form() {
        VideoForm form = new VideoForm();
        form.setTitulo("Aula");
        form.setDescricao("Descrição");
        form.setAutor("Prof. Teste");
        form.setCategoriaId(categoriaId);
        form.setDataPublicacao(LocalDate.of(2026, 10, 1));
        form.setOrigem(OrigemVideo.LINK);
        form.setLink("https://vimeo.com/1");
        return form;
    }
}
