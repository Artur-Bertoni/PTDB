package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.categoria.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade Video (RF06 / RF10).
 *
 * <p>Campos obrigatorios definidos na regra de negocio do RF06: titulo,
 * descricao, categoria, arquivo do video e data de publicacao.
 *
 * <p>A exclusao e sempre logica ({@link #ativo} = false), preservando a
 * integridade referencial com favoritos (RF07) e avaliacao (RF08).
 */
@Entity
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Lob
    @Column(nullable = false)
    private String descricao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Nome do arquivo de video armazenado (upload). Obrigatorio. */
    @Column(name = "arquivo_video", nullable = false)
    private String arquivoVideo;

    /** Nome original do arquivo de video, para exibicao. */
    @Column(name = "arquivo_video_original")
    private String arquivoVideoOriginal;

    /** Nome do arquivo de imagem de capa armazenado (upload). Opcional. */
    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "data_publicacao", nullable = false)
    private LocalDate dataPublicacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VideoStatus status = VideoStatus.AGUARDANDO_AVALIACAO;

    /** Flag de exclusao logica (inativacao). RF06: exclusao nunca e fisica. */
    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Video() {
    }

    // getters / setters

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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getArquivoVideo() {
        return arquivoVideo;
    }

    public void setArquivoVideo(String arquivoVideo) {
        this.arquivoVideo = arquivoVideo;
    }

    public String getArquivoVideoOriginal() {
        return arquivoVideoOriginal;
    }

    public void setArquivoVideoOriginal(String arquivoVideoOriginal) {
        this.arquivoVideoOriginal = arquivoVideoOriginal;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
