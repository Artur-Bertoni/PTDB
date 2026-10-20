package br.edu.dombosco.ptdb.favorito;

import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.video.Video;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorito", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "video_id"}))
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    public Favorito() {
    }

    public Favorito(Usuario usuario, Video video) {
        this.usuario = usuario;
        this.video = video;
        this.dataRegistro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Video getVideo() {
        return video;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}
