package br.edu.dombosco.ptdb.video;

public enum OrigemVideo {

    LINK("Link externo"),
    ARQUIVO("Arquivo enviado");

    private final String descricao;

    OrigemVideo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
