package br.edu.dombosco.ptdb.painel;

public record IndicadoresPainel(
        long videosCadastrados,
        long videosNovosNaSemana,
        long videosAguardando,
        long categorias,
        long categoriasSemAprovados) {
}
