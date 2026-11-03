package br.edu.dombosco.ptdb.banco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.favorito.Favorito;
import br.edu.dombosco.ptdb.favorito.FavoritoRepository;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.OrigemVideo;
import br.edu.dombosco.ptdb.video.Video;
import br.edu.dombosco.ptdb.video.VideoForm;
import br.edu.dombosco.ptdb.video.VideoRepository;
import br.edu.dombosco.ptdb.video.VideoService;
import br.edu.dombosco.ptdb.video.VideoStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@Transactional
class BancoDeDadosTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario aluno;
    private Long administradorId;
    private Long categoriaId;

    @BeforeEach
    void setUp() {
        aluno = usuarioRepository.findFirstByPerfil(PerfilUsuario.ALUNO);
        administradorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR).getId();
        categoriaId = categoriaRepository.findAll().get(0).getId();
    }

    @Test
    void scriptCriaAsTabelasDoModelo() {
        List<String> tabelas = jdbcTemplate.queryForList(
                "SELECT LOWER(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'", String.class);

        assertThat(tabelas).contains("usuario", "categoria", "video", "favorito", "avaliacao", "faq");
    }

    @Test
    void mesmoVideoNaoPodeSerFavoritadoDuasVezesPeloMesmoAluno() {
        Video video = criarAprovado();
        favoritoRepository.saveAndFlush(new Favorito(aluno, video));

        assertThatThrownBy(() -> favoritoRepository.saveAndFlush(new Favorito(aluno, video)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void videoPrecisaDeUmaCategoriaExistente() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO video (titulo, descricao, autor, categoria_id, origem_video, arquivo_link, "
                        + "data_publicacao, status, data_cadastro, ativo) "
                        + "VALUES ('T', 'D', 'A', 99999, 'LINK', 'https://vimeo.com/1', CURRENT_DATE, "
                        + "'APROVADO', CURRENT_TIMESTAMP, TRUE)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void statusDoVideoSoAceitaValoresConhecidos() {
        Video video = criarAprovado();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE video SET status = 'PUBLICADO' WHERE id = ?", video.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void exclusaoLogicaDoVideoRemoveOsFavoritos() {
        Video video = criarAprovado();
        favoritoRepository.saveAndFlush(new Favorito(aluno, video));

        videoService.excluir(video.getId());

        assertThat(favoritoRepository.findByVideoId(video.getId())).isEmpty();
        Boolean ativo = jdbcTemplate.queryForObject("SELECT ativo FROM video WHERE id = ?", Boolean.class, video.getId());
        assertThat(ativo).isFalse();
    }

    @Test
    void apagarOVideoNoBancoApagaOsFavoritosEmCascata() {
        Video video = criarAprovado();
        favoritoRepository.saveAndFlush(new Favorito(aluno, video));

        jdbcTemplate.update("DELETE FROM video WHERE id = ?", video.getId());

        Integer favoritos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM favorito WHERE video_id = ?", Integer.class, video.getId());
        assertThat(favoritos).isZero();
    }

    @Test
    void emailDoUsuarioEUnico() {
        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(
                new Usuario("Outro Aluno", aluno.getEmail(), PerfilUsuario.ALUNO)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Video criarAprovado() {
        VideoForm form = new VideoForm();
        form.setTitulo("Aula");
        form.setDescricao("Descrição");
        form.setAutor("Prof. Teste");
        form.setCategoriaId(categoriaId);
        form.setDataPublicacao(LocalDate.of(2026, 10, 1));
        form.setOrigem(OrigemVideo.LINK);
        form.setLink("https://vimeo.com/1");
        Video video = videoService.criar(form, administradorId);
        video.setStatus(VideoStatus.APROVADO);
        return videoRepository.saveAndFlush(video);
    }
}
