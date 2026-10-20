package br.edu.dombosco.ptdb.avaliacao;

import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.video.Video;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "video_id", nullable = false, updatable = false)
    private Video video;

    @ManyToOne(optional = false)
    @JoinColumn(name = "avaliador_id", nullable = false, updatable = false)
    private Usuario avaliador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private DecisaoAvaliacao decisao;

    @Column(nullable = false, length = 4000, updatable = false)
    private String parecer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime data;

    public Avaliacao() {
    }

    public Avaliacao(Video video, Usuario avaliador, DecisaoAvaliacao decisao, String parecer, LocalDateTime data) {
        this.video = video;
        this.avaliador = avaliador;
        this.decisao = decisao;
        this.parecer = parecer;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public Video getVideo() {
        return video;
    }

    public Usuario getAvaliador() {
        return avaliador;
    }

    public DecisaoAvaliacao getDecisao() {
        return decisao;
    }

    public String getParecer() {
        return parecer;
    }

    public LocalDateTime getData() {
        return data;
    }
}
