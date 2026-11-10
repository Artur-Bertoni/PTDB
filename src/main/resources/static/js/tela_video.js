const conteudo = document.getElementById("conteudo");
const relacionados = document.getElementById("relacionados");

function montarPlayer(video) {
    if (video.arquivo) {
        let capa = "";
        if (video.capa) {
            capa = `poster="${escapar(video.capa)}"`;
        }
        return `<video controls preload="metadata" src="${escapar(video.arquivo)}" ${capa}>
                    Seu navegador não consegue reproduzir este vídeo.
                </video>`;
    }
    if (video.embed) {
        return `<iframe src="${escapar(video.embed)}" title="${escapar(video.titulo)}"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowfullscreen></iframe>`;
    }
    let capa = "";
    if (video.capa) {
        capa = `<img src="${escapar(video.capa)}" alt="">`;
    }
    return `<div class="player-externo">
                ${capa}
                <p><a class="botao" href="${escapar(video.linkExterno)}" target="_blank" rel="noopener">Abrir vídeo no site de origem</a></p>
            </div>`;
}

function mostrarVideo(video, usuario) {
    document.title = video.titulo + " - Portal Acadêmico de Vídeos";

    let aviso = "";
    if (!video.publicado) {
        aviso = `<p class="aviso">Prévia: este vídeo está com status <strong>${escapar(video.statusDescricao)}</strong>
                 e não aparece na tela inicial.</p>`;
    }

    let favorito = "";
    if (usuario.tipoUsuario === "aluno" && video.publicado) {
        favorito = `<button type="button" class="botao-favorito" data-id="${video.id}">${textoFavorito(video.favorito)}</button>`;
    }

    let views = "";
    if (video.views !== null) {
        views = `<span class="etiqueta">👁 ${video.views} visualizações</span>`;
    }

    conteudo.innerHTML = `
        ${aviso}
        <div class="caixa">
            <div class="player">${montarPlayer(video)}</div>
            <div class="info-video">
                <div class="titulo">
                    <h1>${escapar(video.titulo)}</h1>
                    ${favorito}
                </div>
                <div class="etiquetas">
                    <span class="etiqueta">${escapar(video.categoria)}</span>
                    <span class="etiqueta">Autor: ${escapar(video.autor)}</span>
                    <span class="etiqueta">Publicado em ${formatarData(video.dataPublicacao)}</span>
                    ${views}
                </div>
                <p class="texto-longo">${escapar(video.descricao)}</p>
            </div>
        </div>
    `;

    if (video.relacionados.length > 0) {
        let cards = "";
        for (let i = 0; i < video.relacionados.length; i++) {
            cards += montarCardVideo(video.relacionados[i], usuario);
        }
        relacionados.innerHTML = `
            <h2>Mais vídeos de ${escapar(video.categoria)}</h2>
            <div class="videos-container">${cards}</div>
        `;
    }
}

async function iniciar() {
    const usuario = await iniciarPagina();
    const parametros = new URLSearchParams(window.location.search);
    const id = parametros.get("id");

    const resposta = await chamarApi("/api/videos/" + id);
    if (!resposta.ok) {
        mostrarMensagem("Vídeo não encontrado ou indisponível.", false);
        return;
    }
    const video = await resposta.json();
    mostrarVideo(video, usuario);
}

ligarFavoritos(conteudo, null);
ligarFavoritos(relacionados, null);

iniciar();
