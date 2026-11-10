package br.edu.dombosco.ptdb.catalogo;

import br.edu.dombosco.ptdb.video.Video;
import java.time.LocalDate;

public class VideoResumo {

    private Long id;
    private String titulo;
    private String descricao;
    private String autor;
    private String categoria;
    private LocalDate dataPublicacao;
    private String capa;
    private boolean favorito;

    public VideoResumo(Video video, boolean favorito) {
        this.id = video.getId();
        this.titulo = video.getTitulo();
        this.descricao = video.getDescricao();
        this.autor = video.getAutor();
        this.categoria = video.getCategoria().getNome();
        this.dataPublicacao = video.getDataPublicacao();
        this.capa = video.getCapaUrl();
        this.favorito = favorito;
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

    public boolean isFavorito() {
        return favorito;
    }
}
