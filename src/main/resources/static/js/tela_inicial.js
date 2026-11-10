let usuario = null;
let videos = [];

const listaVideos = document.getElementById("listaVideos");
const semVideos = document.getElementById("semVideos");
const pesquisa = document.getElementById("pesquisa");
const filtroCategoria = document.getElementById("filtroCategoria");

async function carregarCategorias() {
    const categorias = await buscarJson("/api/categorias");
    for (let i = 0; i < categorias.length; i++) {
        const opcao = document.createElement("option");
        opcao.value = categorias[i].nome;
        opcao.textContent = categorias[i].nome + " (" + categorias[i].quantidade + ")";
        filtroCategoria.appendChild(opcao);
    }
}

function contem(texto, busca) {
    return texto.toLowerCase().includes(busca);
}

function mostrarVideos() {
    const busca = pesquisa.value.trim().toLowerCase();
    const categoria = filtroCategoria.value;
    let html = "";
    let quantidade = 0;

    for (let i = 0; i < videos.length; i++) {
        const video = videos[i];
        const passouBusca = busca === "" || contem(video.titulo, busca)
            || contem(video.descricao, busca) || contem(video.autor, busca);
        const passouCategoria = categoria === "" || video.categoria === categoria;
        if (passouBusca && passouCategoria) {
            html += montarCardVideo(video, usuario);
            quantidade++;
        }
    }

    listaVideos.innerHTML = html;
    if (quantidade === 0) {
        semVideos.textContent = "Nenhum vídeo encontrado.";
    } else {
        semVideos.textContent = "";
    }
}

async function iniciar() {
    usuario = await iniciarPagina();
    await carregarCategorias();
    videos = await buscarJson("/api/videos");
    mostrarVideos();
}

pesquisa.addEventListener("input", mostrarVideos);
filtroCategoria.addEventListener("change", mostrarVideos);

ligarFavoritos(listaVideos, null);

iniciar();
