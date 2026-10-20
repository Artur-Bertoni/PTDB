package br.edu.dombosco.ptdb.video;

import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

public class VideoForm {

    private Long id;
    private String titulo;
    private String descricao;
    private String autor;
    private Long categoriaId;
    private LocalDate dataPublicacao;
    private OrigemVideo origem = OrigemVideo.LINK;
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

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
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
