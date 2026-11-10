let usuarios = [];

const linhasTabela = document.getElementById("linhasTabela");
const pesquisa = document.getElementById("pesquisa");
const filtroPerfil = document.getElementById("filtroPerfil");

async function carregarUsuarios() {
    usuarios = await buscarJson("/api/admin/usuarios");
    mostrarUsuarios();
}

function mostrarUsuarios() {
    const busca = pesquisa.value.trim().toLowerCase();
    let html = "";
    let quantidade = 0;
    for (let i = 0; i < usuarios.length; i++) {
        const usuario = usuarios[i];
        const passouBusca = busca === "" || usuario.nome.toLowerCase().includes(busca)
            || usuario.matricula.toLowerCase().includes(busca) || usuario.email.toLowerCase().includes(busca);
        const passouPerfil = filtroPerfil.value === "" || usuario.perfil === filtroPerfil.value;
        if (!passouBusca || !passouPerfil) {
            continue;
        }
        quantidade++;

        let acao = "";
        if (usuario.perfil === "ALUNO") {
            acao = `<button type="button" class="botao botao-pequeno" data-promover="${usuario.id}">Promover a avaliador</button>`;
        }

        html += `
            <tr>
                <td><strong>${escapar(usuario.nome)}</strong></td>
                <td>${escapar(usuario.matricula)}</td>
                <td>${escapar(usuario.email)}</td>
                <td>${escapar(usuario.perfilDescricao)}</td>
                <td class="acoes">${acao}</td>
            </tr>
        `;
    }
    linhasTabela.innerHTML = html;
    if (quantidade === 0) {
        document.getElementById("semUsuarios").textContent = "Nenhum usuário encontrado.";
    } else {
        document.getElementById("semUsuarios").textContent = "";
    }
}

async function promover(id) {
    if (!confirm("Confirma a promoção deste aluno a avaliador?")) {
        return;
    }
    const resultado = await enviar("/api/admin/usuarios/" + id + "/promover", "POST");
    if (!resultado.ok) {
        mostrarMensagem(textoDoErro(resultado.dados), false);
        return;
    }
    mostrarMensagem(resultado.dados.mensagem, true);
    await carregarUsuarios();
}

linhasTabela.addEventListener("click", function (evento) {
    if (evento.target.dataset.promover) {
        promover(evento.target.dataset.promover);
    }
});

pesquisa.addEventListener("input", mostrarUsuarios);
filtroPerfil.addEventListener("change", mostrarUsuarios);

async function iniciar() {
    await iniciarPagina();
    await carregarUsuarios();
}

iniciar();
