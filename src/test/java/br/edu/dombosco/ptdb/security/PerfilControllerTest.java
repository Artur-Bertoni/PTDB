package br.edu.dombosco.ptdb.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@AutoConfigureMockMvc
class PerfilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void semPerfilEscolhidoComecaComoAdministrador() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/").session(session)).andExpect(redirectedUrl("/admin"));

        assertThat(UsuarioSessao.buscar(session).isAdministrador()).isTrue();
    }

    @Test
    void trocarPerfilLevaParaATelaDoPerfil() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/perfil").param("perfil", "ALUNO").session(session))
                .andExpect(redirectedUrl("/catalogo"));
        assertThat(UsuarioSessao.buscar(session).getNome()).isEqualTo("Bruno Alves");

        mockMvc.perform(post("/perfil").param("perfil", "AVALIADOR").session(session))
                .andExpect(redirectedUrl("/avaliacao"));
        assertThat(UsuarioSessao.buscar(session).isAvaliador()).isTrue();
    }

    @Test
    void cadaPerfilAcessaSomenteSuasAreas() throws Exception {
        MockHttpSession aluno = Sessoes.aluno(usuarioRepository);
        MockHttpSession avaliador = Sessoes.avaliador(usuarioRepository);
        MockHttpSession administrador = Sessoes.administrador(usuarioRepository);

        mockMvc.perform(get("/catalogo").session(aluno)).andExpect(status().isOk());
        mockMvc.perform(get("/favoritos").session(aluno)).andExpect(status().isOk());
        mockMvc.perform(get("/avaliacao").session(aluno)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin").session(aluno)).andExpect(status().isForbidden());

        mockMvc.perform(get("/avaliacao").session(avaliador)).andExpect(status().isOk());
        mockMvc.perform(get("/favoritos").session(avaliador)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/faq").session(avaliador)).andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/categorias").session(administrador)).andExpect(status().isOk());
        mockMvc.perform(get("/avaliacao").session(administrador)).andExpect(status().isForbidden());
        mockMvc.perform(get("/faq").session(administrador)).andExpect(status().isOk());
    }
}
