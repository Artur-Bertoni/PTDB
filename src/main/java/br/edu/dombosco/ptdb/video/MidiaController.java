package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.storage.FileStorageService;
import br.edu.dombosco.ptdb.storage.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/midia")
public class MidiaController {

    private final FileStorageService storage;
    private final VideoService videoService;
    private final UsuarioSessao usuarioSessao;

    public MidiaController(FileStorageService storage, VideoService videoService, UsuarioSessao usuarioSessao) {
        this.storage = storage;
        this.videoService = videoService;
        this.usuarioSessao = usuarioSessao;
    }

    @GetMapping("/videos/{nome}")
    public ResponseEntity<Resource> video(@PathVariable String nome, HttpServletRequest request) {
        PerfilUsuario perfil = usuarioSessao.perfil(request).orElse(null);
        return responder(videoService.buscarPorArquivo(nome, perfil), FileStorageService.PASTA_VIDEOS, nome);
    }

    @GetMapping("/capas/{nome}")
    public ResponseEntity<Resource> capa(@PathVariable String nome, HttpServletRequest request) {
        PerfilUsuario perfil = usuarioSessao.perfil(request).orElse(null);
        return responder(videoService.buscarPorImagemCapa(nome, perfil), FileStorageService.PASTA_CAPAS, nome);
    }

    private ResponseEntity<Resource> responder(Optional<Video> video, String pasta, String nome) {
        if (video.isEmpty()) {
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
