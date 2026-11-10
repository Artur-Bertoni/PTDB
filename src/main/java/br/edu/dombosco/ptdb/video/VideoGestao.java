package br.edu.dombosco.ptdb.video;

import java.time.LocalDate;

public class VideoGestao {

    private Long id;
    private String titulo;
    private String descricao;
    private String autor;
    private Long categoriaId;
    private String categoria;
    private LocalDate dataPublicacao;
    private String origem;
    private String origemDescricao;
    private String link;
    private String nomeArquivo;
    private String capa;
    private String status;
    private String statusDescricao;
    private String enviadoPor;
    private Integer views;
    private String parecer;

    public VideoGestao(Video video, boolean mostrarViews, String parecer) {
        this.id = video.getId();
        this.titulo = video.getTitulo();
        this.descricao = video.getDescricao();
        this.autor = video.getAutor();
        this.categoriaId = video.getCategoria().getId();
        this.categoria = video.getCategoria().getNome();
        this.dataPublicacao = video.getDataPublicacao();
        this.origem = video.getOrigemVideo().name();
        this.origemDescricao = video.getOrigemVideo().getDescricao();
        if (video.isLink()) {
            this.link = video.getArquivoLink();
        } else {
            this.nomeArquivo = video.getNomeArquivoOriginal();
        }
        this.capa = video.getCapaUrl();
        this.status = video.getStatus().name();
        this.statusDescricao = video.getStatus().getDescricao();
        if (video.getCadastradoPor() != null) {
            this.enviadoPor = video.getCadastradoPor().getNome();
        }
        if (mostrarViews) {
            this.views = video.getQtdViews();
        }
        this.parecer = parecer;
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

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getCategoria() {
        return categoria;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public String getOrigem() {
        return origem;
    }

    public String getOrigemDescricao() {
        return origemDescricao;
    }

    public String getLink() {
        return link;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public String getCapa() {
        return capa;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDescricao() {
        return statusDescricao;
    }

    public String getEnviadoPor() {
        return enviadoPor;
    }

    public Integer getViews() {
        return views;
    }

    public String getParecer() {
        return parecer;
    }
}
