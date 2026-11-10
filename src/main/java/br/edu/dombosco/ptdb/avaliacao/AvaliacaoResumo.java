package br.edu.dombosco.ptdb.avaliacao;

import java.time.LocalDateTime;

public class AvaliacaoResumo {

    private Long id;
    private Long videoId;
    private String video;
    private String avaliador;
    private String decisao;
    private String decisaoDescricao;
    private String parecer;
    private LocalDateTime data;

    public AvaliacaoResumo(Avaliacao avaliacao) {
        this.id = avaliacao.getId();
        this.videoId = avaliacao.getVideo().getId();
        this.video = avaliacao.getVideo().getTitulo();
        this.avaliador = avaliacao.getAvaliador().getNome();
        this.decisao = avaliacao.getDecisao().name();
        this.decisaoDescricao = avaliacao.getDecisao().getDescricao();
        this.parecer = avaliacao.getParecer();
        this.data = avaliacao.getData();
    }

    public Long getId() {
        return id;
    }

    public Long getVideoId() {
        return videoId;
    }

    public String getVideo() {
        return video;
    }

    public String getAvaliador() {
        return avaliador;
    }

    public String getDecisao() {
        return decisao;
    }

    public String getDecisaoDescricao() {
        return decisaoDescricao;
    }

    public String getParecer() {
        return parecer;
    }

    public LocalDateTime getData() {
        return data;
    }
}
