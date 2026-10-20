package br.edu.dombosco.ptdb.faq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.dombosco.ptdb.Sessoes;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "ptdb.storage.location=target/test-uploads")
@AutoConfigureMockMvc
@Transactional
class FaqTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FaqService faqService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void perguntasSaoAgrupadasPorTemaESemTemaVaiParaGeral() {
        ItemFaqForm semTema = new ItemFaqForm();
        semTema.setPergunta("Pergunta sem tema?");
        semTema.setResposta("Resposta.");
        faqService.criar(semTema);

        Map<String, List<ItemFaq>> grupos = faqService.agruparPorTema(null, null);

        assertThat(grupos).containsKeys("Vídeos e favoritos", ItemFaq.TEMA_PADRAO);
        assertThat(grupos.get(ItemFaq.TEMA_PADRAO).get(0).getPergunta()).isEqualTo("Pergunta sem tema?");
    }

    @Test
    void buscaEncontraPelaPerguntaOuResposta() {
        assertThat(faqService.listar("favoritar")).isNotEmpty();
        assertThat(faqService.listar("texto que não existe")).isEmpty();
    }

    @Test
    void paginaDoFaqFiltraPorTema() throws Exception {
        mockMvc.perform(get("/faq").param("tema", "Categorias"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Como os vídeos são organizados por categoria?")))
                .andExpect(content().string(not(containsString("Como faço para favoritar um vídeo?"))));
    }

    @Test
    void administradorCadastraPergunta() throws Exception {
        mockMvc.perform(post("/admin/faq").session(Sessoes.administrador(usuarioRepository))
                        .param("pergunta", "Posso baixar os vídeos?")
                        .param("resposta", "Não, os vídeos são assistidos no portal.")
                        .param("categoriaTema", "Vídeos e favoritos"))
                .andExpect(redirectedUrl("/admin/faq"));

        mockMvc.perform(get("/faq").param("termo", "baixar"))
                .andExpect(content().string(containsString("Posso baixar os vídeos?")));
    }

    @Test
    void perguntaSemRespostaMostraErro() throws Exception {
        mockMvc.perform(post("/admin/faq").session(Sessoes.administrador(usuarioRepository))
                        .param("pergunta", "Pergunta?")
                        .param("resposta", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("A resposta é obrigatória.")));
    }
}
