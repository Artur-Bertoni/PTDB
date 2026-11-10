package br.edu.dombosco.ptdb.catalogo;

import br.edu.dombosco.ptdb.video.Video;
import java.time.LocalDate;
import java.util.List;

public class VideoDetalhe {

    private Long id;
    private String titulo;
    private String descricao;
    private String autor;
    private String categoria;
    private LocalDate dataPublicacao;
    private String capa;
    private String arquivo;
    private String embed;
    private String linkExterno;
    private boolean publicado;
    private String statusDescricao;
    private boolean favorito;
    private Integer views;
    private List<VideoResumo> relacionados;

    public VideoDetalhe(Video video, boolean favorito, boolean mostrarViews, List<VideoResumo> relacionados) {
        this.id = video.getId();
        this.titulo = video.getTitulo();
        this.descricao = video.getDescricao();
        this.autor = video.getAutor();
        this.categoria = video.getCategoria().getNome();
        this.dataPublicacao = video.getDataPublicacao();
        this.capa = video.getCapaUrl();
        this.arquivo = video.getArquivoUrl();
        this.embed = video.getEmbed();
        if (video.isLink()) {
            this.linkExterno = video.getArquivoLink();
        }
        this.publicado = video.isPublicado();
        this.statusDescricao = video.getStatus().getDescricao();
        this.favorito = favorito;
        if (mostrarViews) {
            this.views = video.getQtdViews();
        }
        this.relacionados = relacionados;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getAutor() {
        return autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public String getCapa() {
        return capa;
    }

    public String getArquivo() {
        return arquivo;
    }

    public String getEmbed() {
        return embed;
    }

    public String getLinkExterno() {
        return linkExterno;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public String getStatusDescricao() {
        return statusDescricao;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public Integer getViews() {
        return views;
    }

    public List<VideoResumo> getRelacionados() {
        return relacionados;
    }
}
