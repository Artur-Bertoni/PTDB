package br.edu.dombosco.ptdb.avaliacao;

import br.edu.dombosco.ptdb.video.Video;
import java.time.LocalDateTime;
import java.util.List;

public class VideoParaAvaliar {

    private Long id;
    private String titulo;
    private String descricao;
    private String autor;
    private String categoria;
    private String origem;
    private String enviadoPor;
    private LocalDateTime enviadoEm;
    private List<AvaliacaoResumo> historico;

    public VideoParaAvaliar(Video video, List<AvaliacaoResumo> historico) {
        this.id = video.getId();
        this.titulo = video.getTitulo();
        this.descricao = video.getDescricao();
        this.autor = video.getAutor();
        this.categoria = video.getCategoria().getNome();
        this.origem = video.getOrigemVideo().getDescricao();
        if (video.getCadastradoPor() != null) {
            this.enviadoPor = video.getCadastradoPor().getNome();
        }
        if (video.getDataAtualizacao() != null) {
            this.enviadoEm = video.getDataAtualizacao();
        } else {
            this.enviadoEm = video.getDataCadastro();
        }
        this.historico = historico;
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

    public String getOrigem() {
        return origem;
    }

    public String getEnviadoPor() {
        return enviadoPor;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public List<AvaliacaoResumo> getHistorico() {
        return historico;
    }
}
