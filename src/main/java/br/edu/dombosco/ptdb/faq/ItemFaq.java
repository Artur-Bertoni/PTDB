package br.edu.dombosco.ptdb.faq;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "faq")
public class ItemFaq {

    public static final String TEMA_PADRAO = "Geral";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String pergunta;

    @Column(nullable = false, length = 4000)
    private String resposta;

    @Column(name = "categoria_tema", length = 100)
    private String categoriaTema;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public ItemFaq() {
    }

    public ItemFaq(String pergunta, String resposta, String categoriaTema) {
        this.pergunta = pergunta;
        this.resposta = resposta;
        this.categoriaTema = categoriaTema;
        this.dataCadastro = LocalDateTime.now();
    }

    public String getTema() {
        return categoriaTema == null || categoriaTema.isBlank() ? TEMA_PADRAO : categoriaTema;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public String getCategoriaTema() {
        return categoriaTema;
    }

    public void setCategoriaTema(String categoriaTema) {
        this.categoriaTema = categoriaTema;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
