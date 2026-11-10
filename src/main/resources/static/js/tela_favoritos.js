let usuario = null;

const listaVideos = document.getElementById("listaVideos");
const semVideos = document.getElementById("semVideos");

async function carregarFavoritos() {
    const videos = await buscarJson("/api/favoritos");
    let html = "";
    for (let i = 0; i < videos.length; i++) {
        html += montarCardVideo(videos[i], usuario);
    }
    listaVideos.innerHTML = html;
    if (videos.length === 0) {
        semVideos.innerHTML = "Você ainda não favoritou nenhum vídeo. Use o botão ♡ Favoritar na "
            + "<a href=\"/tela_inicial.html\">tela inicial</a>.";
    } else {
        semVideos.textContent = "";
    }
}

async function iniciar() {
    usuario = await iniciarPagina();
    await carregarFavoritos();
}

ligarFavoritos(listaVideos, carregarFavoritos);

iniciar();
