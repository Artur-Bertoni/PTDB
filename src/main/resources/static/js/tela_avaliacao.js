let fila = [];
let selecionado = null;

const listaFila = document.getElementById("fila");
const formulario = document.getElementById("formulario");

async function carregarFila() {
    fila = await buscarJson("/api/avaliacao/fila");
    document.getElementById("totalFila").textContent = fila.length;

    let html = "";
    for (let i = 0; i < fila.length; i++) {
        const video = fila[i];
        html += `
            <li>
                <strong>${escapar(video.titulo)}</strong>
                <span class="detalhe">${escapar(video.categoria)} · enviado por ${escapar(video.enviadoPor)} em ${formatarDataHora(video.enviadoEm)}</span>
                <button type="button" class="botao botao-pequeno" style="margin-top: 8px;" data-avaliar="${video.id}">Avaliar</button>
            </li>
        `;
    }
    listaFila.innerHTML = html;

    if (fila.length === 0) {
        document.getElementById("filaVazia").textContent = "Nenhum vídeo aguardando avaliação.";
    } else {
        document.getElementById("filaVazia").textContent = "";
    }
}

function selecionar(id) {
    selecionado = null;
    for (let i = 0; i < fila.length; i++) {
        if (String(fila[i].id) === String(id)) {
            selecionado = fila[i];
        }
    }
    if (selecionado === null) {
        document.getElementById("nenhumSelecionado").style.display = "block";
        document.getElementById("videoSelecionado").style.display = "none";
        return;
    }

    document.getElementById("nenhumSelecionado").style.display = "none";
    document.getElementById("videoSelecionado").style.display = "block";
    document.getElementById("selecionadoTitulo").textContent = selecionado.titulo;
    document.getElementById("selecionadoDetalhes").textContent =
        selecionado.categoria + " · Autor: " + selecionado.autor + " · " + selecionado.origem;
    document.getElementById("selecionadoDescricao").textContent = selecionado.descricao;
    document.getElementById("linkAssistir").href = "/tela_video.html?id=" + selecionado.id;

    let historico = "";
    if (selecionado.historico.length > 0) {
        historico = "<h3 style=\"margin-top: 15px;\">Avaliações anteriores</h3><ul class=\"lista\">";
        for (let i = 0; i < selecionado.historico.length; i++) {
            const avaliacao = selecionado.historico[i];
            historico += `<li>
                <strong>${escapar(avaliacao.decisaoDescricao)}</strong> por ${escapar(avaliacao.avaliador)} em ${formatarDataHora(avaliacao.data)}
                <span class="detalhe">${escapar(avaliacao.parecer)}</span>
            </li>`;
        }
        historico += "</ul>";
    }
    document.getElementById("historicoVideo").innerHTML = historico;

    formulario.reset();
    limparErros(formulario);
}

async function carregarHistorico() {
    const avaliacoes = await buscarJson("/api/avaliacao/historico");
    let html = "";
    for (let i = 0; i < avaliacoes.length; i++) {
        const avaliacao = avaliacoes[i];
        html += `
            <tr>
                <td>${formatarDataHora(avaliacao.data)}</td>
                <td>${escapar(avaliacao.video)}</td>
                <td>${escapar(avaliacao.avaliador)}</td>
                <td><span class="selo selo-${avaliacao.decisao.toLowerCase()}">${escapar(avaliacao.decisaoDescricao)}</span></td>
                <td>${escapar(avaliacao.parecer)}</td>
            </tr>
        `;
    }
    document.getElementById("historico").innerHTML = html;
    if (avaliacoes.length === 0) {
        document.getElementById("historicoVazio").textContent = "Nenhuma avaliação registrada ainda.";
    } else {
        document.getElementById("historicoVazio").textContent = "";
    }
}

async function avaliar(evento) {
    evento.preventDefault();
    const marcada = formulario.querySelector("input[name=decisao]:checked");
    let decisao = null;
    if (marcada) {
        decisao = marcada.value;
    }
    const dados = {
        decisao: decisao,
        parecer: document.getElementById("parecer").value
    };

    const resultado = await enviar("/api/avaliacao/" + selecionado.id, "POST", dados);
    if (!resultado.ok) {
        mostrarErros(formulario, resultado.dados);
        return;
    }
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarFila();
    await carregarHistorico();
    if (fila.length > 0) {
        selecionar(fila[0].id);
    } else {
        selecionar(null);
    }
}

listaFila.addEventListener("click", function (evento) {
    if (evento.target.dataset.avaliar) {
        esconderMensagem();
        selecionar(evento.target.dataset.avaliar);
    }
});

formulario.addEventListener("submit", avaliar);

async function iniciar() {
    await iniciarPagina();
    await carregarFila();
    await carregarHistorico();
    if (fila.length > 0) {
        selecionar(fila[0].id);
    }
}

iniciar();
