package br.edu.dombosco.ptdb.painel;

public class IndicadoresPainel {

    private long videosCadastrados;
    private long videosNovosNaSemana;
    private long videosAguardando;
    private long categorias;
    private long categoriasSemAprovados;
    private long itensFaq;

    public long getVideosCadastrados() {
        return videosCadastrados;
    }

    public void setVideosCadastrados(long videosCadastrados) {
        this.videosCadastrados = videosCadastrados;
    }

    public long getVideosNovosNaSemana() {
        return videosNovosNaSemana;
    }

    public void setVideosNovosNaSemana(long videosNovosNaSemana) {
        this.videosNovosNaSemana = videosNovosNaSemana;
    }

    public long getVideosAguardando() {
        return videosAguardando;
    }

    public void setVideosAguardando(long videosAguardando) {
        this.videosAguardando = videosAguardando;
    }

    public long getCategorias() {
        return categorias;
    }

    public void setCategorias(long categorias) {
        this.categorias = categorias;
    }

    public long getCategoriasSemAprovados() {
        return categoriasSemAprovados;
    }

    public void setCategoriasSemAprovados(long categoriasSemAprovados) {
        this.categoriasSemAprovados = categoriasSemAprovados;
    }

    public long getItensFaq() {
        return itensFaq;
    }

    public void setItensFaq(long itensFaq) {
        this.itensFaq = itensFaq;
    }
}
