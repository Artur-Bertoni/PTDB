package br.edu.dombosco.ptdb.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public class VideoForm {

    private Long id;

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 4000, message = "A descrição deve ter no máximo 4000 caracteres.")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória.")
    private Long categoriaId;

    @NotNull(message = "A data de publicação é obrigatória.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPublicacao;

    @NotNull(message = "Informe se o vídeo será um link ou um arquivo.")
    private OrigemVideo origem = OrigemVideo.LINK;

    @Size(max = 500, message = "O link deve ter no máximo 500 caracteres.")
    private String link;

    private MultipartFile arquivoVideo;

    private MultipartFile imagemCapa;

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

    public OrigemVideo getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemVideo origem) {
        this.origem = origem;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public MultipartFile getArquivoVideo() {
        return arquivoVideo;
    }

    public void setArquivoVideo(MultipartFile arquivoVideo) {
        this.arquivoVideo = arquivoVideo;
    }

    public MultipartFile getImagemCapa() {
        return imagemCapa;
    }

    public void setImagemCapa(MultipartFile imagemCapa) {
        this.imagemCapa = imagemCapa;
    }
}
