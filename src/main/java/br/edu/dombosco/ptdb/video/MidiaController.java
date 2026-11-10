package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.storage.FileStorageService;
import br.edu.dombosco.ptdb.storage.StorageException;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/midia")
public class MidiaController {

    private final FileStorageService storage;
    private final VideoService videoService;

    public MidiaController(FileStorageService storage, VideoService videoService) {
        this.storage = storage;
        this.videoService = videoService;
    }

    @GetMapping("/videos/{nome}")
    public ResponseEntity<Resource> video(@PathVariable String nome, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        Video video = videoService.buscarPorArquivo(nome, usuario);
        return responder(video, FileStorageService.PASTA_VIDEOS, nome);
    }

    @GetMapping("/capas/{nome}")
    public ResponseEntity<Resource> capa(@PathVariable String nome, HttpSession session) {
        UsuarioLogado usuario = UsuarioSessao.buscar(session);
        Video video = videoService.buscarPorImagemCapa(nome, usuario);
        return responder(video, FileStorageService.PASTA_CAPAS, nome);
    }

    private ResponseEntity<Resource> responder(Video video, String pasta, String nome) {
        if (video == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Resource recurso;
        try {
            recurso = storage.carregar(pasta, nome);
        } catch (StorageException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MediaType tipo = MediaTypeFactory.getMediaType(nome).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }
}
