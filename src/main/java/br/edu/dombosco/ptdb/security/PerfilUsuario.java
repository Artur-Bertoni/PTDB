package br.edu.dombosco.ptdb.security;

public enum PerfilUsuario {

    ADMINISTRADOR("Administrador"),
    AVALIADOR("Avaliador"),
    ALUNO("Aluno");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
