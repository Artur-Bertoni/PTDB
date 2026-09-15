package br.edu.dombosco.ptdb.video;

/**
 * Status do fluxo de avaliacao de um video (RF06 / RF08).
 *
 * <p>Um video recem-cadastrado ou editado permanece em
 * {@link #AGUARDANDO_AVALIACAO} ate ser avaliado pelo Avaliador (RF08),
 * sendo exibido aos alunos somente apos {@link #APROVADO}.
 */
public enum VideoStatus {

    AGUARDANDO_AVALIACAO("Aguardando avaliacao"),
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
