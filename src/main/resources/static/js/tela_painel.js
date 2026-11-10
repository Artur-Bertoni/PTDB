function mostrarIndicadores(indicadores) {
    document.getElementById("videosCadastrados").textContent = indicadores.videosCadastrados;
    document.getElementById("videosNovosNaSemana").textContent = indicadores.videosNovosNaSemana;
    document.getElementById("videosAguardando").textContent = indicadores.videosAguardando;
    document.getElementById("categorias").textContent = indicadores.categorias;
    document.getElementById("categoriasSemAprovados").textContent = indicadores.categoriasSemAprovados;
    document.getElementById("itensFaq").textContent = indicadores.itensFaq;
}

function mostrarAtividades(atividades) {
    let html = "";
    for (let i = 0; i < atividades.length; i++) {
        const atividade = atividades[i];
        html += `<li>
            <strong>${escapar(atividade.titulo)}</strong> ${escapar(atividade.descricao)}
            <span class="detalhe">${escapar(atividade.quando)}</span>
        </li>`;
    }
    document.getElementById("atividades").innerHTML = html;
    if (atividades.length === 0) {
        document.getElementById("semAtividades").textContent = "Nenhuma atividade registrada.";
    }
}

function mostrarFila(fila) {
    let html = "";
    for (let i = 0; i < fila.length; i++) {
        const item = fila[i];
        html += `<li>
            <a href="/tela_video.html?id=${item.id}"><strong>${escapar(item.titulo)}</strong></a>
            <span class="detalhe">${escapar(item.categoria)} · ${escapar(item.enviado)}</span>
        </li>`;
    }
    document.getElementById("fila").innerHTML = html;
    if (fila.length === 0) {
        document.getElementById("semFila").textContent = "Nenhum vídeo aguardando avaliação.";
    }
}

async function iniciar() {
    await iniciarPagina();
    const dados = await buscarJson("/api/admin/painel");
    mostrarIndicadores(dados.indicadores);
    mostrarAtividades(dados.atividades);
    mostrarFila(dados.fila);
}

iniciar();
