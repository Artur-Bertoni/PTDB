package br.edu.dombosco.ptdb.painel;

public enum TipoAtividade {

    CADASTRO("foi cadastrado e enviado para avaliação"),
    EDICAO("foi editado e reenviado para avaliação"),
    EXCLUSAO("foi excluído"),
    APROVACAO("foi aprovado"),
    REPROVACAO("foi reprovado"),
    AJUSTES("teve ajustes solicitados");

    private final String descricao;

    TipoAtividade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
