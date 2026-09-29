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

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "ptdb.storage.location=target/test-uploads",
                "ptdb.dev.auto-admin=false"
        })
class PaginaErroTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void acessoNegadoUsaAPaginaDoPortal() {
        ResponseEntity<String> resposta = abrir("/admin/videos");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resposta.getBody()).contains("Acesso restrito", "ptdb.css");
    }

    @Test
    void paginaInexistenteUsaAPaginaDoPortal() {
        ResponseEntity<String> resposta = abrir("/nao-existe");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).contains("Página não encontrada", "ptdb.css");
    }

    private ResponseEntity<String> abrir(String caminho) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.TEXT_HTML));
        return rest.exchange(caminho, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
