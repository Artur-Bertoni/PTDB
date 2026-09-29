package br.edu.dombosco.ptdb.video;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@SpringBootTest(properties = {
        "ptdb.storage.location=target/test-uploads",
        "ptdb.dev.auto-admin=false"
})
@Transactional
class VideoServiceTest {

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
    void cadastroComLinkFicaAguardandoAvaliacao() {
        Video video = videoService.criar(formComLink("https://youtu.be/dQw4w9WgXcQ"));

        assertThat(video.getOrigemVideo()).isEqualTo(OrigemVideo.LINK);
        assertThat(video.getArquivoLink()).isEqualTo("https://youtu.be/dQw4w9WgXcQ");
        assertThat(video.getStatus()).isEqualTo(VideoStatus.AGUARDANDO_AVALIACAO);
        assertThat(video.getDataCadastro()).isNotNull();
        assertThat(video.getCapaExterna()).contains("dQw4w9WgXcQ");
    }

    @Test
    void cadastroComArquivoGuardaNomeGeradoENomeOriginal() {
        VideoForm form = formBase();
        form.setOrigem(OrigemVideo.ARQUIVO);
        form.setArquivoVideo(arquivoDeVideo());

        Video video = videoService.criar(form);

        assertThat(video.getOrigemVideo()).isEqualTo(OrigemVideo.ARQUIVO);
        assertThat(video.getArquivoLink()).endsWith(".mp4").isNotEqualTo("aula.mp4");
        assertThat(video.getNomeArquivoOriginal()).isEqualTo("aula.mp4");
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
        Video existente = videoService.criar(cadastro);

        VideoForm edicao = formBase();
        edicao.setOrigem(OrigemVideo.ARQUIVO);

        assertThat(validar(edicao, existente).hasErrors()).isFalse();
    }

    @Test
    void edicaoTrocaArquivoPorLinkEVoltaParaAvaliacao() {
        VideoForm cadastro = formBase();
        cadastro.setOrigem(OrigemVideo.ARQUIVO);
        cadastro.setArquivoVideo(arquivoDeVideo());
        Video video = videoService.criar(cadastro);
        video.setStatus(VideoStatus.APROVADO);

        Video atualizado = videoService.atualizar(video.getId(), formComLink("https://vimeo.com/123456"));

        assertThat(atualizado.getOrigemVideo()).isEqualTo(OrigemVideo.LINK);
        assertThat(atualizado.getNomeArquivoOriginal()).isNull();
        assertThat(atualizado.getStatus()).isEqualTo(VideoStatus.AGUARDANDO_AVALIACAO);
        assertThat(atualizado.getDataAtualizacao()).isNotNull();
    }

    @Test
    void somenteAprovadosAtivosSaoListadosParaAlunos() {
        Video aprovado = videoService.criar(formComLink("https://vimeo.com/1"));
        aprovado.setStatus(VideoStatus.APROVADO);
        Video pendente = videoService.criar(formComLink("https://vimeo.com/2"));
        Video excluido = videoService.criar(formComLink("https://vimeo.com/3"));
        excluido.setStatus(VideoStatus.APROVADO);
        videoService.excluir(excluido.getId());

        assertThat(videoService.listarAprovados(null))
                .contains(aprovado)
                .doesNotContain(pendente, excluido);
    }

    @Test
    void indicadoresContamSomenteVideosAtivos() {
        IndicadoresVideos antes = videoService.indicadores();
        Video aprovado = videoService.criar(formComLink("https://vimeo.com/1"));
        aprovado.setStatus(VideoStatus.APROVADO);
        videoService.criar(formComLink("https://vimeo.com/2"));
        Video excluido = videoService.criar(formComLink("https://vimeo.com/3"));
        videoService.excluir(excluido.getId());

        IndicadoresVideos depois = videoService.indicadores();

        assertThat(depois.total() - antes.total()).isEqualTo(2);
        assertThat(depois.aprovados() - antes.aprovados()).isEqualTo(1);
        assertThat(depois.aguardando() - antes.aguardando()).isEqualTo(1);
    }

    @Test
    void videoPendenteSoEVisivelParaAdministradorEAvaliador() {
        Video pendente = videoService.criar(formComLink("https://vimeo.com/1"));

        assertThat(videoService.podeVisualizar(pendente, null)).isFalse();
        assertThat(videoService.podeVisualizar(pendente, PerfilUsuario.ALUNO)).isFalse();
        assertThat(videoService.podeVisualizar(pendente, PerfilUsuario.AVALIADOR)).isTrue();
        assertThat(videoService.podeVisualizar(pendente, PerfilUsuario.ADMINISTRADOR)).isTrue();

        pendente.setStatus(VideoStatus.APROVADO);
        assertThat(videoService.podeVisualizar(pendente, PerfilUsuario.ALUNO)).isTrue();
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
        form.setCategoriaId(categoriaId);
        form.setDataPublicacao(LocalDate.of(2026, 9, 29));
        return form;
    }

    private static MockMultipartFile arquivoDeVideo() {
        return new MockMultipartFile("arquivoVideo", "aula.mp4", "video/mp4", new byte[] {1, 2, 3});
    }
}
