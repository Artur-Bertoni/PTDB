package br.edu.dombosco.ptdb.video;

public enum VideoStatus {

    AGUARDANDO_AVALIACAO("Aguardando avaliação"),
    AJUSTES_SOLICITADOS("Ajustes solicitados"),
    APROVADO("Aprovado"),
    REPROVADO("Reprovado");

    private final String descricao;

    VideoStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
