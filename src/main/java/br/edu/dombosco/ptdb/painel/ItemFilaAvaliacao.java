package br.edu.dombosco.ptdb.painel;

public class ItemFilaAvaliacao {

    private Long id;
    private String titulo;
    private String categoria;
    private String enviado;

    public ItemFilaAvaliacao(Long id, String titulo, String categoria, String enviado) {
        this.id = id;
        this.titulo = titulo;
        this.categoria = categoria;
        this.enviado = enviado;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getEnviado() {
        return enviado;
    }
}
