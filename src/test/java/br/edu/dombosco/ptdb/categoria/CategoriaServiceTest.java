package br.edu.dombosco.ptdb.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import br.edu.dombosco.ptdb.video.OrigemVideo;
import br.edu.dombosco.ptdb.video.VideoForm;
import br.edu.dombosco.ptdb.video.VideoService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@Transactional
class CategoriaServiceTest {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void criaEditaEExcluiCategoria() {
        Categoria categoria = categoriaService.criar(form("Design", "Interfaces e UX"));
        assertThat(categoriaService.listarAtivas()).contains(categoria);

        categoriaService.atualizar(categoria.getId(), form("Design de Interfaces", null));
        assertThat(categoriaService.buscarPorId(categoria.getId()).getNome()).isEqualTo("Design de Interfaces");
        assertThat(categoriaService.buscarPorId(categoria.getId()).getDescricao()).isNull();

        categoriaService.excluir(categoria.getId());
        assertThat(categoriaService.listarAtivas()).doesNotContain(categoria);
    }

    @Test
    void nomeDuplicadoERecusado() {
        categoriaService.criar(form("Matemática", null));

        Errors errors = validar(form("matemática", null));

        assertThat(errors.hasFieldErrors("nome")).isTrue();
    }

    @Test
    void nomeEObrigatorio() {
        assertThat(validar(form(" ", null)).hasFieldErrors("nome")).isTrue();
    }

    @Test
    void categoriaComVideosNaoPodeSerExcluida() {
        Categoria categoria = categoriaService.criar(form("Estatística", null));
        VideoForm video = new VideoForm();
        video.setTitulo("Aula");
        video.setDescricao("Descrição");
        video.setAutor("Prof. Teste");
        video.setCategoriaId(categoria.getId());
        video.setDataPublicacao(LocalDate.of(2026, 10, 1));
        video.setOrigem(OrigemVideo.LINK);
        video.setLink("https://vimeo.com/1");
        Long administradorId = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR).getId();
        videoService.criar(video, administradorId);

        assertThatThrownBy(() -> categoriaService.excluir(categoria.getId()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void recriarCategoriaExcluidaReativaOMesmoRegistro() {
        Categoria categoria = categoriaService.criar(form("Robótica", null));
        categoriaService.excluir(categoria.getId());

        Categoria reativada = categoriaService.criar(form("Robótica", "Volta"));

        assertThat(reativada.getId()).isEqualTo(categoria.getId());
        assertThat(reativada.isAtiva()).isTrue();
    }

    private Errors validar(CategoriaForm form) {
        Errors errors = new BeanPropertyBindingResult(form, "form");
        categoriaService.validar(form, errors);
        return errors;
    }

    private CategoriaForm form(String nome, String descricao) {
        CategoriaForm form = new CategoriaForm();
        form.setNome(nome);
        form.setDescricao(descricao);
        return form;
    }
}
