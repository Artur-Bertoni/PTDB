package br.edu.dombosco.ptdb.video;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@Transactional
class VideoServiceTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long categoriaId;
    private UsuarioLogado administrador;
    private UsuarioLogado avaliador;
    private UsuarioLogado aluno;

    @BeforeEach
    void setUp() {
        categoriaId = categoriaRepository.findAll().get(0).getId();
        administrador = Sessoes.usuario(usuarioRepository, PerfilUsuario.ADMINISTRADOR);
        avaliador = Sessoes.usuario(usuarioRepository, PerfilUsuario.AVALIADOR);
        aluno = Sessoes.usuario(usuarioRepository, PerfilUsuario.ALUNO);
    }

    @Test
    void cadastroComLinkFicaAguardandoAvaliacao() {
        Video video = videoService.criar(formComLink("https://youtu.be/dQw4w9WgXcQ"), administrador.getId());

        assertThat(video.getOrigemVideo()).isEqualTo(OrigemVideo.LINK);
        assertThat(video.getArquivoLink()).isEqualTo("https://youtu.be/dQw4w9WgXcQ");
        assertThat(video.getStatus()).isEqualTo(VideoStatus.AGUARDANDO_AVALIACAO);
        assertThat(video.getDataCadastro()).isNotNull();
        assertThat(video.getCapaExterna()).contains("dQw4w9WgXcQ");
        assertThat(video.getCadastradoPor().getId()).isEqualTo(administrador.getId());
    }

    @Test
    void cadastroComArquivoGuardaNomeGeradoENomeOriginal() {
        VideoForm form = formBase();
        form.setOrigem(OrigemVideo.ARQUIVO);
        form.setArquivoVideo(arquivoDeVideo());

        Video video = videoService.criar(form, administrador.getId());

        assertThat(video.getOrigemVideo()).isEqualTo(OrigemVideo.ARQUIVO);
        assertThat(video.getArquivoLink()).endsWith(".mp4").isNotEqualTo("aula.mp4");
        assertThat(video.getNomeArquivoOriginal()).isEqualTo("aula.mp4");
    }

    @Test
    void camposObrigatoriosSaoValidados() {
        VideoForm form = new VideoForm();
        form.setOrigem(OrigemVideo.LINK);

        Errors errors = validar(form, null);

        assertThat(errors.hasFieldErrors("titulo")).isTrue();
        assertThat(errors.hasFieldErrors("descricao")).isTrue();
        assertThat(errors.hasFieldErrors("autor")).isTrue();
        assertThat(errors.hasFieldErrors("categoriaId")).isTrue();
        assertThat(errors.hasFieldErrors("dataPublicacao")).isTrue();
        assertThat(errors.hasFieldErrors("link")).isTrue();
    }

    @Test
    void linkInvalidoERejeitado() {
        Errors errors = validar(formComLink("javascript:alert(1)"), null);

        assertThat(errors.hasFieldErrors("link")).isTrue();
    }

    @Test
    void arquivoEObrigatorioNoCadastro() {
        VideoForm form = formBase();
        form.setOrigem(OrigemVideo.ARQUIVO);

        assertThat(validar(form, null).hasFieldErrors("arquivoVideo")).isTrue();
    }

    @Test
    void arquivoQueNaoEVideoERejeitado() {
        VideoForm form = formBase();
        form.setOrigem(OrigemVideo.ARQUIVO);
        form.setArquivoVideo(new MockMultipartFile("arquivoVideo", "foto.png", "image/png", new byte[] {1}));

        assertThat(validar(form, null).hasFieldErrors("arquivoVideo")).isTrue();
    }

    @Test
    void edicaoPodeManterOArquivoAtual() {
        VideoForm cadastro = formBase();
        cadastro.setOrigem(OrigemVideo.ARQUIVO);
        cadastro.setArquivoVideo(arquivoDeVideo());
        Video existente = videoService.criar(cadastro, administrador.getId());

        VideoForm edicao = formBase();
        edicao.setOrigem(OrigemVideo.ARQUIVO);

        assertThat(validar(edicao, existente).hasErrors()).isFalse();
    }

    @Test
    void edicaoTrocaArquivoPorLinkEVoltaParaAvaliacao() {
        VideoForm cadastro = formBase();
        cadastro.setOrigem(OrigemVideo.ARQUIVO);
        cadastro.setArquivoVideo(arquivoDeVideo());
        Video video = videoService.criar(cadastro, administrador.getId());
        video.setStatus(VideoStatus.APROVADO);

        Video atualizado = videoService.atualizar(video.getId(), formComLink("https://vimeo.com/123456"));

        assertThat(atualizado.getOrigemVideo()).isEqualTo(OrigemVideo.LINK);
        assertThat(atualizado.getNomeArquivoOriginal()).isNull();
        assertThat(atualizado.getStatus()).isEqualTo(VideoStatus.AGUARDANDO_AVALIACAO);
        assertThat(atualizado.getDataAtualizacao()).isNotNull();
    }

    @Test
    void somentePublicadosSaoListadosNoCatalogo() {
        Video aprovado = videoService.criar(formComLink("https://vimeo.com/1"), administrador.getId());
        aprovado.setStatus(VideoStatus.APROVADO);
        Video pendente = videoService.criar(formComLink("https://vimeo.com/2"), administrador.getId());
        Video excluido = videoService.criar(formComLink("https://vimeo.com/3"), administrador.getId());
        excluido.setStatus(VideoStatus.APROVADO);
        videoService.excluir(excluido.getId());

        assertThat(videoService.listarPublicados(null, null))
                .contains(aprovado)
                .doesNotContain(pendente, excluido);
    }

    @Test
    void buscaFiltraPorTermoECategoria() {
        VideoForm form = formComLink("https://vimeo.com/1");
        form.setTitulo("Introdução ao Kanban");
        Video kanban = videoService.criar(form, administrador.getId());
        kanban.setStatus(VideoStatus.APROVADO);
        Long outraCategoria = categoriaRepository.findAll().get(1).getId();

        assertThat(videoService.listarPublicados("kanban", null)).contains(kanban);
        assertThat(videoService.listarPublicados("inexistente", null)).doesNotContain(kanban);
        assertThat(videoService.listarPublicados(null, outraCategoria)).doesNotContain(kanban);
        assertThat(videoService.contarPublicadosPorCategoria().get(categoriaId)).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void indicadoresContamSomenteVideosAtivos() {
        IndicadoresVideos antes = videoService.indicadores();
        Video aprovado = videoService.criar(formComLink("https://vimeo.com/1"), administrador.getId());
        aprovado.setStatus(VideoStatus.APROVADO);
        videoService.criar(formComLink("https://vimeo.com/2"), administrador.getId());
        Video excluido = videoService.criar(formComLink("https://vimeo.com/3"), administrador.getId());
        videoService.excluir(excluido.getId());

        IndicadoresVideos depois = videoService.indicadores();

        assertThat(depois.getTotal() - antes.getTotal()).isEqualTo(2);
        assertThat(depois.getAprovados() - antes.getAprovados()).isEqualTo(1);
        assertThat(depois.getAguardando() - antes.getAguardando()).isEqualTo(1);
    }

    @Test
    void videoPendenteSoEVisivelParaAdministradorEAvaliador() {
        Video pendente = videoService.criar(formComLink("https://vimeo.com/1"), administrador.getId());

        assertThat(videoService.podeVisualizar(pendente, null)).isFalse();
        assertThat(videoService.podeVisualizar(pendente, aluno)).isFalse();
        assertThat(videoService.podeVisualizar(pendente, avaliador)).isTrue();
        assertThat(videoService.podeVisualizar(pendente, administrador)).isTrue();

        pendente.setStatus(VideoStatus.APROVADO);
        assertThat(videoService.podeVisualizar(pendente, aluno)).isTrue();
    }

    private Errors validar(VideoForm form, Video atual) {
        Errors errors = new BeanPropertyBindingResult(form, "form");
        videoService.validar(form, atual, errors);
        return errors;
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

    private static MockMultipartFile arquivoDeVideo() {
        return new MockMultipartFile("arquivoVideo", "aula.mp4", "video/mp4", new byte[] {1, 2, 3});
    }
}
