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
