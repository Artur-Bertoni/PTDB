package br.edu.dombosco.ptdb.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "ptdb.storage.location=target/test-uploads",
                "spring.datasource.url=jdbc:h2:mem:ptdb-erro;DB_CLOSE_DELAY=-1"
        })
class PaginaErroTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void acessoNegadoUsaAPaginaDoPortal() {
        String cookie = escolherPerfil("ALUNO");

        ResponseEntity<String> resposta = abrir("/admin/videos", cookie);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resposta.getBody()).contains("Acesso restrito", "ptdb.css");
    }

    @Test
    void paginaInexistenteUsaAPaginaDoPortal() {
        String cookie = escolherPerfil("ALUNO");

        ResponseEntity<String> resposta = abrir("/nao-existe", cookie);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).contains("Página não encontrada", "ptdb.css", "Bruno Alves");
    }

    @Test
    void trocaDePerfilNaoColocaASessaoNaUrl() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> dados = new LinkedMultiValueMap<>();
        dados.add("perfil", "ALUNO");

        ResponseEntity<String> resposta = rest.postForEntity("/perfil", new HttpEntity<>(dados, headers), String.class);

        assertThat(resposta.getHeaders().getLocation().toString()).doesNotContain("jsessionid");
    }

    private String escolherPerfil(String perfil) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> dados = new LinkedMultiValueMap<>();
        dados.add("perfil", perfil);
        ResponseEntity<String> resposta = rest.postForEntity("/perfil", new HttpEntity<>(dados, headers), String.class);
        String cookie = resposta.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        return cookie.split(";")[0];
    }

    private ResponseEntity<String> abrir(String caminho, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.TEXT_HTML));
        headers.add(HttpHeaders.COOKIE, cookie);
        return rest.exchange(caminho, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
