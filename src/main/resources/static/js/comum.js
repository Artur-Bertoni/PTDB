const LINKS_MENU = [
    { texto: "Início", endereco: "/tela_inicial.html", perfis: ["aluno", "avaliador", "administrador"] },
    { texto: "Meus Favoritos", endereco: "/tela_favoritos.html", perfis: ["aluno"] },
    { texto: "Meus Vídeos", endereco: "/tela_gerenciar_videos.html", perfis: ["aluno"] },
    { texto: "Liberar Vídeos", endereco: "/tela_avaliacao.html", perfis: ["avaliador"] },
    { texto: "Painel", endereco: "/tela_painel.html", perfis: ["administrador"] },
    { texto: "Gerenciar Vídeos", endereco: "/tela_gerenciar_videos.html", perfis: ["administrador"] },
    { texto: "Categorias", endereco: "/tela_categorias.html", perfis: ["administrador"] },
    { texto: "Gerenciar FAQ", endereco: "/tela_gerenciar_faq.html", perfis: ["administrador"] },
    { texto: "Usuários", endereco: "/tela_usuarios.html", perfis: ["administrador"] }
];

function escapar(texto) {
    if (texto === null || texto === undefined) {
        return "";
    }
    return String(texto)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

function formatarData(texto) {
    if (!texto) {
        return "";
    }
    const partes = texto.substring(0, 10).split("-");
    return partes[2] + "/" + partes[1] + "/" + partes[0];
}

function formatarDataHora(texto) {
    if (!texto) {
        return "";
    }
    return formatarData(texto) + " " + texto.substring(11, 16);
}

async function chamarApi(endereco, opcoes) {
    const resposta = await fetch(endereco, opcoes);
    if (resposta.status === 401) {
        window.location.href = "/tela_login.html";
        throw new Error("Sessão encerrada");
    }
    return resposta;
}

async function buscarJson(endereco) {
    const resposta = await chamarApi(endereco);
    return await resposta.json();
}

async function enviar(endereco, metodo, corpo) {
    const opcoes = { method: metodo };
    if (corpo instanceof FormData) {
        opcoes.body = corpo;
    } else if (corpo) {
        opcoes.headers = { "Content-Type": "application/json" };
        opcoes.body = JSON.stringify(corpo);
    }
    const resposta = await chamarApi(endereco, opcoes);
    let dados = {};
    try {
        dados = await resposta.json();
    } catch (erro) {
        dados = {};
    }
    return { ok: resposta.ok, dados: dados };
}

function mostrarMensagem(texto, sucesso) {
    const mensagem = document.getElementById("mensagem");
    mensagem.textContent = texto;
    if (sucesso) {
        mensagem.className = "mensagem sucesso";
    } else {
        mensagem.className = "mensagem erro";
    }
    window.scrollTo(0, 0);
}

function esconderMensagem() {
    const mensagem = document.getElementById("mensagem");
    mensagem.className = "mensagem";
    mensagem.textContent = "";
}

function limparErros(formulario) {
    const campos = formulario.querySelectorAll(".erro-campo");
    for (let i = 0; i < campos.length; i++) {
        campos[i].textContent = "";
    }
}

function mostrarErros(formulario, dados) {
    limparErros(formulario);
    let encontrouCampo = false;
    for (const campo in dados) {
        const elemento = formulario.querySelector("[data-erro='" + campo + "']");
        if (elemento) {
            elemento.textContent = dados[campo];
            encontrouCampo = true;
        }
    }
    if (!encontrouCampo) {
        alert(textoDoErro(dados));
    }
}

function textoDoErro(dados) {
    if (dados.erro) {
        return dados.erro;
    }
    return "Não foi possível concluir a operação.";
}

function montarCabecalho(usuario) {
    let links = "";
    for (let i = 0; i < LINKS_MENU.length; i++) {
        const link = LINKS_MENU[i];
        if (link.perfis.includes(usuario.tipoUsuario)) {
            let classe = "";
            if (window.location.pathname === link.endereco) {
                classe = "ativo";
            }
            links += `<a class="${classe}" href="${link.endereco}">${link.texto}</a>`;
        }
    }

    document.getElementById("cabecalho").innerHTML = `
        <div class="logo">
            <a href="/tela_inicial.html">
                <img src="/img/logo-faculdade-dom-bosco.png" alt="Faculdade Dom Bosco">
            </a>
        </div>
        <nav class="menu">
            ${links}
            <div class="perfil">
                <button type="button" id="btnPerfil">Perfil ▾</button>
                <div class="perfil-menu" id="perfilMenu">
                    <p class="perfil-nome">${escapar(usuario.nome)}</p>
                    <p class="perfil-tipo">${escapar(usuario.perfil)}</p>
                    <form method="post" action="/sair">
                        <button type="submit">Sair</button>
                    </form>
                </div>
            </div>
        </nav>
    `;

    document.getElementById("btnPerfil").addEventListener("click", function () {
        document.getElementById("perfilMenu").classList.toggle("aberto");
    });
}

function montarRodape() {
    document.getElementById("rodape").innerHTML = `
        <div class="faq-area">
            <a class="botao-rodape" href="/tela_faq.html">❓ FAQ / Dúvidas Frequentes</a>
            <a class="botao-rodape" href="/tela_ajuda.html">📘 Ajuda</a>
        </div>
        <div class="footer-info">
            <p>Faculdade Dom Bosco</p>
            <p>Portal Acadêmico de Vídeos</p>
            <p>© 2026 Todos os direitos reservados</p>
        </div>
    `;
}

async function iniciarPagina() {
    const usuario = await buscarJson("/api/sessao");
    montarCabecalho(usuario);
    montarRodape();
    return usuario;
}

function textoFavorito(favorito) {
    if (favorito) {
        return "♥ Favoritado";
    }
    return "♡ Favoritar";
}

function montarCardVideo(video, usuario) {
    let capa = `
        <div class="area-vazia">
            <div class="icone-video">📹</div>
            <p>Sem imagem de capa</p>
        </div>`;
    if (video.capa) {
        capa = `<img class="capa" src="${escapar(video.capa)}" alt="">`;
    }

    let favorito = "";
    if (usuario.tipoUsuario === "aluno") {
        favorito = `<button type="button" class="botao-favorito" data-id="${video.id}">${textoFavorito(video.favorito)}</button>`;
    }

    return `
        <div class="video-card">
            <a href="/tela_video.html?id=${video.id}">${capa}</a>
            <h3>${escapar(video.titulo)}</h3>
            <p class="descricao">${escapar(video.descricao)}</p>
            <p class="categoria">Categoria: ${escapar(video.categoria)}</p>
            <p class="autor">${escapar(video.autor)} · ${formatarData(video.dataPublicacao)}</p>
            <div class="acoes-card">
                <a class="link-video" href="/tela_video.html?id=${video.id}">Abrir página do vídeo</a>
                ${favorito}
            </div>
        </div>
    `;
}

function ligarFavoritos(container, depois) {
    container.addEventListener("click", async function (evento) {
        const botao = evento.target;
        if (!botao.classList.contains("botao-favorito")) {
            return;
        }
        const resultado = await enviar("/api/favoritos/" + botao.dataset.id, "POST");
        if (!resultado.ok) {
            alert(textoDoErro(resultado.dados));
            return;
        }
        botao.textContent = textoFavorito(resultado.dados.favorito);
        if (depois) {
            depois(resultado.dados.favorito);
        }
    });
}
