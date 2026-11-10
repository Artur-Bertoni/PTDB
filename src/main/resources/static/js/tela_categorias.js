let categorias = [];
let idEmEdicao = null;

const formulario = document.getElementById("formulario");
const linhasTabela = document.getElementById("linhasTabela");

async function carregarCategorias() {
    categorias = await buscarJson("/api/admin/categorias");
    let html = "";
    for (let i = 0; i < categorias.length; i++) {
        const categoria = categorias[i];
        html += `
            <tr>
                <td><strong>${escapar(categoria.nome)}</strong></td>
                <td>${escapar(categoria.descricao)}</td>
                <td>${categoria.quantidade}</td>
                <td class="acoes">
                    <button type="button" class="botao botao-secundario botao-pequeno" data-editar="${categoria.id}">Editar</button>
                    <button type="button" class="botao botao-perigo botao-pequeno" data-excluir="${categoria.id}">Excluir</button>
                </td>
            </tr>
        `;
    }
    linhasTabela.innerHTML = html;
    if (categorias.length === 0) {
        document.getElementById("semCategorias").textContent = "Nenhuma categoria cadastrada.";
    } else {
        document.getElementById("semCategorias").textContent = "";
    }
}

function limparFormulario() {
    idEmEdicao = null;
    formulario.reset();
    limparErros(formulario);
    document.getElementById("tituloFormulario").textContent = "Nova categoria";
    document.getElementById("btnSalvar").textContent = "Salvar categoria";
}

function editar(id) {
    for (let i = 0; i < categorias.length; i++) {
        if (String(categorias[i].id) === String(id)) {
            idEmEdicao = categorias[i].id;
            document.getElementById("nome").value = categorias[i].nome;
            document.getElementById("descricao").value = categorias[i].descricao || "";
        }
    }
    limparErros(formulario);
    document.getElementById("tituloFormulario").textContent = "Editar categoria";
    document.getElementById("btnSalvar").textContent = "Salvar alterações";
    window.scrollTo(0, 0);
}

async function salvar(evento) {
    evento.preventDefault();
    const dados = {
        nome: document.getElementById("nome").value,
        descricao: document.getElementById("descricao").value
    };

    let resultado;
    if (idEmEdicao === null) {
        resultado = await enviar("/api/admin/categorias", "POST", dados);
    } else {
        resultado = await enviar("/api/admin/categorias/" + idEmEdicao, "PUT", dados);
    }

    if (!resultado.ok) {
        mostrarErros(formulario, resultado.dados);
        return;
    }
    limparFormulario();
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarCategorias();
}

async function excluir(id) {
    if (!confirm("Confirma a exclusão desta categoria?")) {
        return;
    }
    const resultado = await enviar("/api/admin/categorias/" + id, "DELETE");
    if (!resultado.ok) {
        mostrarMensagem(textoDoErro(resultado.dados), false);
        return;
    }
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarCategorias();
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

async function iniciar() {
    await iniciarPagina();
    await carregarCategorias();
}

iniciar();
