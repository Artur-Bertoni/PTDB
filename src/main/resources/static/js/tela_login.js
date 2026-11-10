const loginForm =
document.getElementById("loginForm");

const usuarioInput =
document.getElementById("usuario");

const senhaInput =
document.getElementById("senha");

const message =
document.getElementById("message");

const mostrarSenha =
document.getElementById("mostrarSenha");

/* LOGIN */
loginForm.addEventListener(
    "submit",
    async function(event){

        event.preventDefault();

        const usuario =
        usuarioInput.value.trim();

        const senha =
        senhaInput.value.trim();

        try {

            const resposta =
            await fetch(
                "/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({

                        usuario,
                        senha

                    })
                }
            );

            if(!resposta.ok){

                message.textContent =
                "Usuário ou senha inválidos.";

                message.style.color =
                "red";

                return;

            }

            message.textContent =
            "Login realizado com sucesso!";

            message.style.color =
            "green";

            setTimeout(() => {

                window.location.href =
                "/tela_inicial.html";

            },1000);

        }
        catch(erro){

            message.textContent =
            "Erro ao conectar com a API.";

            message.style.color =
            "red";

            console.log(erro);

        }

    }
);

/* MOSTRAR SENHA */

mostrarSenha.addEventListener(
    "click",
    () => {

        if (
            senhaInput.type === "password"
        ) {

            senhaInput.type =
            "text";

            mostrarSenha.textContent =
            "🙈";

        } else {

            senhaInput.type =
            "password";

            mostrarSenha.textContent =
            "👁";

        }

    }
);
