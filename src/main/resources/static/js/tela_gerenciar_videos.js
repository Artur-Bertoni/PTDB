let usuario = null;
let videos = [];
let idEmEdicao = null;

const linhasTabela = document.getElementById("linhasTabela");
const semVideos = document.getElementById("semVideos");
const pesquisa = document.getElementById("pesquisa");
const filtroCategoria = document.getElementById("filtroCategoria");
const filtroStatus = document.getElementById("filtroStatus");
const janela = document.getElementById("janela");
const formulario = document.getElementById("formulario");

function ehAdministrador() {
    return usuario.tipoUsuario === "administrador";
}

function prepararTextos() {
    if (ehAdministrador()) {
        document.getElementById("descricaoPagina").textContent =
            "Cadastre, edite e exclua os vídeos do portal. Vídeos cadastrados por administradores são publicados direto.";
        document.getElementById("avisoFormulario").textContent =
            "Ao salvar, o vídeo é publicado direto na tela inicial, sem passar pela avaliação.";
    } else {
        document.title = "Meus Vídeos - Portal Acadêmico de Vídeos";
        document.getElementById("tituloPagina").textContent = "Meus Vídeos";
        document.getElementById("descricaoPagina").textContent =
            "Envie seus vídeos e acompanhe a avaliação. Eles aparecem na tela inicial depois de aprovados.";
        document.getElementById("btnNovo").textContent = "+ Subir vídeo";
        document.getElementById("avisoFormulario").textContent =
            "Ao salvar, o vídeo fica aguardando avaliação e só aparece na tela inicial depois de aprovado.";
    }

    let colunas = "<tr><th>Vídeo</th><th>Categoria</th><th>Publicação</th><th>Status</th>";
    if (ehAdministrador()) {
        colunas += "<th>Enviado por</th><th>Views</th>";
    }
    colunas += "<th></th></tr>";
    document.getElementById("cabecalhoTabela").innerHTML = colunas;
}

async function carregarCategorias() {
    const categorias = await buscarJson("/api/categorias");
    const seletorFormulario = document.getElementById("categoriaId");
    for (let i = 0; i < categorias.length; i++) {
        const opcaoFiltro = document.createElement("option");
        opcaoFiltro.value = categorias[i].nome;
        opcaoFiltro.textContent = categorias[i].nome;
        filtroCategoria.appendChild(opcaoFiltro);

        const opcaoFormulario = document.createElement("option");
        opcaoFormulario.value = categorias[i].id;
        opcaoFormulario.textContent = categorias[i].nome;
        seletorFormulario.appendChild(opcaoFormulario);
    }
}

async function carregarVideos() {
    videos = await buscarJson("/api/gestao/videos");
    mostrarIndicadores();
    mostrarVideos();
}

function mostrarIndicadores() {
    let aguardando = 0;
    let ajustes = 0;
    let aprovados = 0;
    let reprovados = 0;
    for (let i = 0; i < videos.length; i++) {
        if (videos[i].status === "AGUARDANDO_AVALIACAO") {
            aguardando++;
        } else if (videos[i].status === "AJUSTES_SOLICITADOS") {
            ajustes++;
        } else if (videos[i].status === "APROVADO") {
            aprovados++;
        } else if (videos[i].status === "REPROVADO") {
            reprovados++;
        }
    }
    document.getElementById("totalVideos").textContent = videos.length;
    document.getElementById("totalAguardando").textContent = aguardando;
    document.getElementById("totalAjustes").textContent = ajustes;
    document.getElementById("totalAprovados").textContent = aprovados;
    document.getElementById("totalReprovados").textContent = reprovados;
}

function contem(texto, busca) {
    return texto.toLowerCase().includes(busca);
}

function mostrarVideos() {
    const busca = pesquisa.value.trim().toLowerCase();
    let html = "";
    let quantidade = 0;

    for (let i = 0; i < videos.length; i++) {
        const video = videos[i];
        const passouBusca = busca === "" || contem(video.titulo, busca)
            || contem(video.descricao, busca) || contem(video.autor, busca);
        const passouCategoria = filtroCategoria.value === "" || video.categoria === filtroCategoria.value;
        const passouStatus = filtroStatus.value === "" || video.status === filtroStatus.value;
        if (!passouBusca || !passouCategoria || !passouStatus) {
            continue;
        }
        quantidade++;

        let parecer = "";
        if (video.parecer && video.status !== "APROVADO") {
            parecer = `<span class="detalhe">Parecer: ${escapar(video.parecer)}</span>`;
        }

        let colunasAdmin = "";
        if (ehAdministrador()) {
            colunasAdmin = `<td>${escapar(video.enviadoPor)}</td><td>${video.views}</td>`;
        }

        html += `
            <tr>
                <td>
                    <strong>${escapar(video.titulo)}</strong>
                    <span class="detalhe">${escapar(video.autor)} · ${escapar(video.origemDescricao)}</span>
                </td>
                <td>${escapar(video.categoria)}</td>
                <td>${formatarData(video.dataPublicacao)}</td>
                <td>
                    <span class="selo selo-${video.status.toLowerCase()}">${escapar(video.statusDescricao)}</span>
                    ${parecer}
                </td>
                ${colunasAdmin}
                <td class="acoes">
                    <a class="botao botao-secundario botao-pequeno" href="/tela_video.html?id=${video.id}">Ver</a>
                    <button type="button" class="botao botao-secundario botao-pequeno" data-editar="${video.id}">Editar</button>
                    <button type="button" class="botao botao-perigo botao-pequeno" data-excluir="${video.id}">Excluir</button>
                </td>
            </tr>
        `;
    }

    linhasTabela.innerHTML = html;
    if (quantidade === 0) {
        semVideos.textContent = "Nenhum vídeo encontrado.";
    } else {
        semVideos.textContent = "";
    }
}

function escolherOrigem(origem) {
    const radios = formulario.querySelectorAll("input[name=origem]");
    for (let i = 0; i < radios.length; i++) {
        radios[i].checked = radios[i].value === origem;
    }
    atualizarCamposOrigem();
}

function origemEscolhida() {
    return formulario.querySelector("input[name=origem]:checked").value;
}

function atualizarCamposOrigem() {
    if (origemEscolhida() === "LINK") {
        document.getElementById("campoLink").style.display = "flex";
        document.getElementById("campoArquivo").style.display = "none";
    } else {
        document.getElementById("campoLink").style.display = "none";
        document.getElementById("campoArquivo").style.display = "flex";
    }
}

function abrirFormulario(video) {
    formulario.reset();
    limparErros(formulario);
    document.getElementById("arquivoAtual").textContent = "";

    if (video) {
        idEmEdicao = video.id;
        document.getElementById("tituloFormulario").textContent = "Editar vídeo";
        document.getElementById("btnSalvar").textContent = "Salvar alterações";
        document.getElementById("titulo").value = video.titulo;
        document.getElementById("descricao").value = video.descricao;
        document.getElementById("autor").value = video.autor;
        document.getElementById("categoriaId").value = video.categoriaId;
        document.getElementById("dataPublicacao").value = video.dataPublicacao;
        escolherOrigem(video.origem);
        if (video.origem === "LINK") {
            document.getElementById("link").value = video.link;
        } else {
            document.getElementById("arquivoAtual").textContent =
                "Arquivo atual: " + video.nomeArquivo + ". Deixe em branco para mantê-lo.";
        }
    } else {
        idEmEdicao = null;
        document.getElementById("tituloFormulario").textContent = "Novo vídeo";
        document.getElementById("btnSalvar").textContent = "Salvar vídeo";
        escolherOrigem("LINK");
    }
    janela.classList.add("aberta");
}

function fecharFormulario() {
    janela.classList.remove("aberta");
}

async function salvar(evento) {
    evento.preventDefault();
    const dados = new FormData(formulario);
    if (origemEscolhida() === "LINK") {
        dados.delete("arquivoVideo");
    } else {
        dados.delete("link");
    }

    let endereco = "/api/gestao/videos";
    if (idEmEdicao !== null) {
        endereco = "/api/gestao/videos/" + idEmEdicao;
    }

    const botao = document.getElementById("btnSalvar");
    botao.disabled = true;
    const resultado = await enviar(endereco, "POST", dados);
    botao.disabled = false;

    if (!resultado.ok) {
        mostrarErros(formulario, resultado.dados);
        return;
    }
    fecharFormulario();
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarVideos();
}

async function editar(id) {
    const video = await buscarJson("/api/gestao/videos/" + id);
    abrirFormulario(video);
}

async function excluir(id) {
    if (!confirm("Confirma a exclusão deste vídeo?")) {
        return;
    }
    const resultado = await enviar("/api/gestao/videos/" + id, "DELETE");
    if (!resultado.ok) {
        mostrarMensagem(textoDoErro(resultado.dados), false);
        return;
    }
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarVideos();
}

linhasTabela.addEventListener("click", function (evento) {
    const alvo = evento.target;
    if (alvo.dataset.editar) {
        editar(alvo.dataset.editar);
    }
    if (alvo.dataset.excluir) {
        excluir(alvo.dataset.excluir);
    }
});

document.getElementById("btnNovo").addEventListener("click", function () {
    esconderMensagem();
    abrirFormulario(null);
});
document.getElementById("btnCancelar").addEventListener("click", fecharFormulario);
formulario.addEventListener("submit", salvar);

const radiosOrigem = formulario.querySelectorAll("input[name=origem]");
for (let i = 0; i < radiosOrigem.length; i++) {
    radiosOrigem[i].addEventListener("change", atualizarCamposOrigem);
}

pesquisa.addEventListener("input", mostrarVideos);
filtroCategoria.addEventListener("change", mostrarVideos);
filtroStatus.addEventListener("change", mostrarVideos);

async function iniciar() {
    usuario = await iniciarPagina();
    prepararTextos();
    await carregarCategorias();
    await carregarVideos();
}

iniciar();
