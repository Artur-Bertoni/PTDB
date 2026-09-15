package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Disponibiliza (stream/preview) os arquivos de video e thumbnail enviados
 * no RF06. Fica sob {@code /admin} para uso do Administrador na conferencia.
 */
@Controller
@RequestMapping("/admin/midia")
public class MidiaController {

    private final FileStorageService storage;

    public MidiaController(FileStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/videos/{nome}")
    public ResponseEntity<Resource> video(@PathVariable String nome) {
        Resource recurso = storage.carregar(FileStorageService.PASTA_VIDEOS, nome);
        return responder(recurso, MediaType.APPLICATION_OCTET_STREAM);
    }

    @GetMapping("/thumbnails/{nome}")
    public ResponseEntity<Resource> thumbnail(@PathVariable String nome) {
        Resource recurso = storage.carregar(FileStorageService.PASTA_THUMBNAILS, nome);
        return responder(recurso, MediaType.APPLICATION_OCTET_STREAM);
    }

    private ResponseEntity<Resource> responder(Resource recurso, MediaType fallback) {
        MediaType tipo = MediaTypeFactoryHelper.detectar(recurso.getFilename(), fallback);
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }

    /** Pequeno auxiliar para inferir o Content-Type pela extensao. */
    private static final class MediaTypeFactoryHelper {
        static MediaType detectar(String nomeArquivo, MediaType fallback) {
            return org.springframework.http.MediaTypeFactory
                    .getMediaType(nomeArquivo == null ? "" : nomeArquivo)
                    .orElse(fallback);
        }
    }
}
