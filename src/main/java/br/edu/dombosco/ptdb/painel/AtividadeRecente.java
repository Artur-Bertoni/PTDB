package br.edu.dombosco.ptdb.painel;

import java.time.LocalDateTime;

public class AtividadeRecente {

    private String titulo;
    private TipoAtividade tipo;
    private LocalDateTime momento;
    private String quando;

    public AtividadeRecente(String titulo, TipoAtividade tipo, LocalDateTime momento) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.momento = momento;
    }

    public String getTitulo() {
        return titulo;
    }

    public TipoAtividade getTipo() {
        return tipo;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public String getQuando() {
        return quando;
    }

    public void setQuando(String quando) {
        this.quando = quando;
    }
}
