package br.edu.dombosco.ptdb.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

/**
 * Objeto de formulario para cadastro e edicao de video (RF06).
 *
 * <p>As validacoes cobrem os campos obrigatorios de texto/data. A
 * obrigatoriedade do arquivo de video e tratada na camada de servico, pois
 * na edicao o arquivo pode ser mantido (sem novo upload).
 */
public class VideoForm {

    private Long id;

    @NotBlank(message = "O titulo e obrigatorio.")
    @Size(max = 150, message = "O titulo deve ter no maximo 150 caracteres.")
    private String titulo;

    @NotBlank(message = "A descricao e obrigatoria.")
    @Size(max = 4000, message = "A descricao deve ter no maximo 4000 caracteres.")
    private String descricao;

    @NotNull(message = "A categoria e obrigatoria.")
    private Long categoriaId;

    @NotNull(message = "A data de publicacao e obrigatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPublicacao;

    /** Arquivo de video (upload). Obrigatorio no cadastro; opcional na edicao. */
    private MultipartFile arquivoVideo;

    /** Imagem de capa (upload). Opcional. */
    private MultipartFile thumbnail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public MultipartFile getArquivoVideo() {
        return arquivoVideo;
    }

    public void setArquivoVideo(MultipartFile arquivoVideo) {
        this.arquivoVideo = arquivoVideo;
    }

    public MultipartFile getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(MultipartFile thumbnail) {
        this.thumbnail = thumbnail;
    }
}
