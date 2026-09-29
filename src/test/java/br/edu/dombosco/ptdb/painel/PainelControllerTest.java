package br.edu.dombosco.ptdb.painel;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "ptdb.storage.location=target/test-uploads",
        "ptdb.dev.auto-admin=false"
})
@AutoConfigureMockMvc
class PainelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void raizLevaAoPainel() throws Exception {
        mockMvc.perform(get("/")).andExpect(redirectedUrl("/admin"));
    }

    @Test
    void painelExigePerfilAdministrador() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());

        MockHttpSession avaliador = new MockHttpSession();
        avaliador.setAttribute(UsuarioSessao.ATRIBUTO_PERFIL, PerfilUsuario.AVALIADOR);
        mockMvc.perform(get("/admin").session(avaliador)).andExpect(status().isForbidden());
    }

    @Test
    void painelExibeIndicadoresModulosEListas() throws Exception {
        MockHttpSession administrador = new MockHttpSession();
        administrador.setAttribute(UsuarioSessao.ATRIBUTO_PERFIL, PerfilUsuario.ADMINISTRADOR);

        mockMvc.perform(get("/admin").session(administrador))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Painel administrativo")))
                .andExpect(content().string(containsString("Vídeos cadastrados")))
                .andExpect(content().string(containsString("Aguardando avaliação")))
                .andExpect(content().string(containsString("Gerenciar categorias")))
                .andExpect(content().string(containsString("Em breve")))
                .andExpect(content().string(containsString("Atividade recente")))
                .andExpect(content().string(containsString("Fila de avaliação")));
    }
}
