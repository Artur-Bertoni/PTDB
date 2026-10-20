package br.edu.dombosco.ptdb.avaliacao;

import br.edu.dombosco.ptdb.video.VideoStatus;

public enum DecisaoAvaliacao {

    APROVADO("Aprovado", "Vídeo aprovado e publicado no catálogo.", VideoStatus.APROVADO),
    REPROVADO("Reprovado", "Vídeo reprovado.", VideoStatus.REPROVADO),
    AJUSTES_SOLICITADOS("Ajustes solicitados", "Ajustes solicitados ao responsável pelo vídeo.",
            VideoStatus.AJUSTES_SOLICITADOS);

    private final String descricao;
    private final String mensagem;
    private final VideoStatus statusResultante;

    DecisaoAvaliacao(String descricao, String mensagem, VideoStatus statusResultante) {
        this.descricao = descricao;
        this.mensagem = mensagem;
        this.statusResultante = statusResultante;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public VideoStatus getStatusResultante() {
        return statusResultante;
    }
}
