package br.edu.dombosco.ptdb.storage;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    public static final String PASTA_VIDEOS = "videos";
    public static final String PASTA_CAPAS = "capas";

    private final Path raiz;

    public FileStorageService(@Value("${ptdb.storage.location:uploads}") String location) {
        this.raiz = Paths.get(location).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(raiz.resolve(PASTA_VIDEOS));
            Files.createDirectories(raiz.resolve(PASTA_CAPAS));
        } catch (IOException e) {
            throw new StorageException("Não foi possível inicializar o diretório de uploads.", e);
        }
    }

    public String armazenar(MultipartFile arquivo, String subpasta, String prefixoTipoEsperado) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new StorageException("Arquivo vazio ou não informado.");
        }
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith(prefixoTipoEsperado)) {
            throw new StorageException("Tipo de arquivo inválido. Esperado: " + prefixoTipoEsperado + "*");
        }

        String nomeOriginal = StringUtils.cleanPath(
                arquivo.getOriginalFilename() == null ? "" : arquivo.getOriginalFilename());
        String extensao = StringUtils.getFilenameExtension(nomeOriginal);
        String nomeGerado = UUID.randomUUID().toString()
                + (extensao != null && !extensao.isBlank() ? "." + extensao : "");

        Path destino = raiz.resolve(subpasta).resolve(nomeGerado).normalize();

        if (!destino.getParent().equals(raiz.resolve(subpasta))) {
            throw new StorageException("Caminho de destino inválido.");
        }

        try (InputStream in = arquivo.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Falha ao gravar o arquivo enviado.", e);
        }
        return nomeGerado;
    }

    public Resource carregar(String subpasta, String nomeArquivo) {
        try {
            Path caminho = raiz.resolve(subpasta).resolve(nomeArquivo).normalize();
            if (!caminho.getParent().equals(raiz.resolve(subpasta))) {
                throw new StorageException("Caminho de arquivo inválido.");
            }
            Resource resource = new UrlResource(caminho.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new StorageException("Arquivo não encontrado: " + nomeArquivo);
            }
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new StorageException("Arquivo não encontrado: " + nomeArquivo, e);
        }
    }

    public void remover(String subpasta, String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            return;
        }
        try {
            Path caminho = raiz.resolve(subpasta).resolve(nomeArquivo).normalize();
            if (caminho.getParent().equals(raiz.resolve(subpasta))) {
                Files.deleteIfExists(caminho);
            }
        } catch (IOException e) {
            throw new StorageException("Falha ao remover o arquivo: " + nomeArquivo, e);
        }
    }
}
