package br.edu.dombosco.ptdb.video;

public class IndicadoresVideos {

    private long total;
    private long aguardando;
    private long aprovados;
    private long reprovados;

    public IndicadoresVideos(long total, long aguardando, long aprovados, long reprovados) {
        this.total = total;
        this.aguardando = aguardando;
        this.aprovados = aprovados;
        this.reprovados = reprovados;
    }

    public long getTotal() {
        return total;
    }

    public long getAguardando() {
        return aguardando;
    }

    public long getAprovados() {
        return aprovados;
    }

    public long getReprovados() {
        return reprovados;
    }
}
