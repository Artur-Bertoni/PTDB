package br.edu.dombosco.ptdb.faq;

public class ItemFaqForm {

    private Long id;
    private String pergunta;
    private String resposta;
    private String categoriaTema;

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
}
