package br.edu.dombosco.ptdb.painel;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@AutoConfigureMockMvc
class PainelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void painelExigePerfilAdministrador() throws Exception {
        mockMvc.perform(get("/admin").session(Sessoes.avaliador(usuarioRepository)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin").session(Sessoes.aluno(usuarioRepository)))
                .andExpect(status().isForbidden());
    }

    @Test
    void painelExibeIndicadoresModulosEListas() throws Exception {
        mockMvc.perform(get("/admin").session(Sessoes.administrador(usuarioRepository)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Painel administrativo")))
                .andExpect(content().string(containsString("Vídeos cadastrados")))
                .andExpect(content().string(containsString("Aguardando avaliação")))
                .andExpect(content().string(containsString("/admin/categorias")))
                .andExpect(content().string(containsString("/admin/faq")))
                .andExpect(content().string(containsString("Atividade recente")))
                .andExpect(content().string(containsString("Fila de avaliação")));
    }
}
