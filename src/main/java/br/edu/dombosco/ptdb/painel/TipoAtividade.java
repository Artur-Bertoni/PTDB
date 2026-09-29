package br.edu.dombosco.ptdb.painel;

public enum TipoAtividade {

    CADASTRO("foi cadastrado e enviado para avaliação"),
    EDICAO("foi editado e reenviado para avaliação"),
    EXCLUSAO("foi excluído");

    private final String descricao;

    TipoAtividade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
