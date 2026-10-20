document.querySelectorAll('dialog[data-abrir]').forEach((dialogo) => {
    if (!dialogo.open) {
        dialogo.showModal();
    }
    const alvo = dialogo.querySelector('.campo__input--erro') || dialogo.querySelector('.campo__input');
    if (alvo) {
        alvo.focus();
    }
    dialogo.addEventListener('cancel', (evento) => {
        if (dialogo.dataset.fechar) {
            evento.preventDefault();
            window.location.href = dialogo.dataset.fechar;
        }
    });
});

document.querySelectorAll('[data-alternador]').forEach((grupo) => {
    const nome = grupo.dataset.alternador;
    const formulario = grupo.closest('form') || document;
    const atualizar = () => {
        const selecionado = formulario.querySelector(`input[name="${nome}"]:checked`);
        formulario.querySelectorAll(`[data-${nome}]`).forEach((painel) => {
            painel.hidden = !selecionado || painel.dataset[nome] !== selecionado.value;
        });
    };
    grupo.addEventListener('change', atualizar);
    atualizar();
});

document.querySelectorAll('form.alternancia').forEach((formulario) => {
    formulario.addEventListener('submit', (evento) => {
        evento.preventDefault();
        const botao = formulario.querySelector('button');
        botao.disabled = true;
        fetch(formulario.action, { method: 'POST', redirect: 'manual' })
            .then((resposta) => {
                if (resposta.type === 'opaqueredirect' || resposta.ok) {
                    alternarFavorito(formulario, botao);
                }
            })
            .finally(() => {
                botao.disabled = false;
            });
    });
});

function alternarFavorito(formulario, botao) {
    const marcado = !botao.classList.contains('favorito--marcado');
    botao.classList.toggle('favorito--marcado', marcado);
    botao.setAttribute('aria-pressed', marcado);

    const listaFavoritos = formulario.closest('[data-lista-favoritos]');
    if (listaFavoritos && !marcado) {
        formulario.closest('.card-video').remove();
    }
}
