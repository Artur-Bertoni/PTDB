let itens = [];
let idEmEdicao = null;

const formulario = document.getElementById("formulario");
const linhasTabela = document.getElementById("linhasTabela");
const pesquisa = document.getElementById("pesquisa");

async function carregarItens() {
    itens = await buscarJson("/api/faq");
    mostrarItens();
}

function mostrarItens() {
    const busca = pesquisa.value.trim().toLowerCase();
    let html = "";
    let quantidade = 0;
    for (let i = 0; i < itens.length; i++) {
        const item = itens[i];
        const passouBusca = busca === "" || item.pergunta.toLowerCase().includes(busca)
            || item.resposta.toLowerCase().includes(busca) || item.tema.toLowerCase().includes(busca);
        if (!passouBusca) {
            continue;
        }
        quantidade++;
        html += `
            <tr>
                <td>
                    <strong>${escapar(item.pergunta)}</strong>
                    <span class="detalhe">${escapar(item.resposta)}</span>
                </td>
                <td>${escapar(item.tema)}</td>
                <td class="acoes">
                    <button type="button" class="botao botao-secundario botao-pequeno" data-editar="${item.id}">Editar</button>
                    <button type="button" class="botao botao-perigo botao-pequeno" data-excluir="${item.id}">Excluir</button>
                </td>
            </tr>
        `;
    }
    linhasTabela.innerHTML = html;
    if (quantidade === 0) {
        document.getElementById("semPerguntas").textContent = "Nenhuma pergunta encontrada.";
    } else {
        document.getElementById("semPerguntas").textContent = "";
    }
}

function limparFormulario() {
    idEmEdicao = null;
    formulario.reset();
    limparErros(formulario);
    document.getElementById("tituloFormulario").textContent = "Nova pergunta";
    document.getElementById("btnSalvar").textContent = "Salvar pergunta";
}

function editar(id) {
    for (let i = 0; i < itens.length; i++) {
        if (String(itens[i].id) === String(id)) {
            idEmEdicao = itens[i].id;
            document.getElementById("pergunta").value = itens[i].pergunta;
            document.getElementById("resposta").value = itens[i].resposta;
            document.getElementById("categoriaTema").value = itens[i].categoriaTema || "";
        }
    }
    limparErros(formulario);
    document.getElementById("tituloFormulario").textContent = "Editar pergunta";
    document.getElementById("btnSalvar").textContent = "Salvar alterações";
    window.scrollTo(0, 0);
}

async function salvar(evento) {
    evento.preventDefault();
    const dados = {
        pergunta: document.getElementById("pergunta").value,
        resposta: document.getElementById("resposta").value,
        categoriaTema: document.getElementById("categoriaTema").value
    };

    let resultado;
    if (idEmEdicao === null) {
        resultado = await enviar("/api/admin/faq", "POST", dados);
    } else {
        resultado = await enviar("/api/admin/faq/" + idEmEdicao, "PUT", dados);
    }

    if (!resultado.ok) {
        mostrarErros(formulario, resultado.dados);
        return;
    }
    limparFormulario();
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarItens();
}

async function excluir(id) {
    if (!confirm("Confirma a exclusão desta pergunta?")) {
        return;
    }
    const resultado = await enviar("/api/admin/faq/" + id, "DELETE");
    if (!resultado.ok) {
        mostrarMensagem(textoDoErro(resultado.dados), false);
        return;
    }
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarItens();
}

linhasTabela.addEventListener("click", function (evento) {
    const alvo = evento.target;
    if (alvo.dataset.editar) {
        esconderMensagem();
        editar(alvo.dataset.editar);
    }
    if (alvo.dataset.excluir) {
        excluir(alvo.dataset.excluir);
    }
});

formulario.addEventListener("submit", salvar);
document.getElementById("btnCancelar").addEventListener("click", limparFormulario);
pesquisa.addEventListener("input", mostrarItens);

async function iniciar() {
    await iniciarPagina();
    await carregarItens();
}

iniciar();
