let itens = [];

const pesquisa = document.getElementById("pesquisa");
const filtroTema = document.getElementById("filtroTema");

function listarTemas() {
    const temas = [];
    for (let i = 0; i < itens.length; i++) {
        if (!temas.includes(itens[i].tema)) {
            temas.push(itens[i].tema);
        }
    }
    return temas;
}

function mostrarPerguntas() {
    const busca = pesquisa.value.trim().toLowerCase();
    const temas = listarTemas();
    let html = "";
    let quantidade = 0;

    for (let t = 0; t < temas.length; t++) {
        const tema = temas[t];
        if (filtroTema.value !== "" && filtroTema.value !== tema) {
            continue;
        }
        let perguntas = "";
        for (let i = 0; i < itens.length; i++) {
            const item = itens[i];
            const passouBusca = busca === "" || item.pergunta.toLowerCase().includes(busca)
                || item.resposta.toLowerCase().includes(busca);
            if (item.tema === tema && passouBusca) {
                perguntas += `
                    <details>
                        <summary>${escapar(item.pergunta)}</summary>
                        <p>${escapar(item.resposta)}</p>
                    </details>
                `;
                quantidade++;
            }
        }
        if (perguntas !== "") {
            html += `<div class="grupo-faq"><h2>${escapar(tema)}</h2>${perguntas}</div>`;
        }
    }

    document.getElementById("perguntas").innerHTML = html;
    if (quantidade === 0) {
        document.getElementById("semPerguntas").textContent = "Nenhuma pergunta encontrada.";
    } else {
        document.getElementById("semPerguntas").textContent = "";
    }
}

async function iniciar() {
    await iniciarPagina();
    itens = await buscarJson("/api/faq");
    const temas = listarTemas();
    for (let i = 0; i < temas.length; i++) {
        const opcao = document.createElement("option");
        opcao.value = temas[i];
        opcao.textContent = temas[i];
        filtroTema.appendChild(opcao);
    }
    mostrarPerguntas();
}

pesquisa.addEventListener("input", mostrarPerguntas);
filtroTema.addEventListener("change", mostrarPerguntas);

iniciar();
