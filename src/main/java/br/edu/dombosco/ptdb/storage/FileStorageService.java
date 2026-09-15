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

/**
 * Servico de armazenamento de arquivos de upload do RF06 (arquivo do video
 * e imagem de capa).
 *
 * <p>Os arquivos sao gravados em disco em subpastas ({@link #PASTA_VIDEOS} e
 * {@link #PASTA_THUMBNAILS}) sob o diretorio configurado em
 * {@code ptdb.storage.location}. No banco (RF10) e persistido apenas o nome
 * do arquivo gerado, mantendo o conteudo audiovisual fora da base
 * relacional (RNF05).
 */
@Service
public class FileStorageService {

    public static final String PASTA_VIDEOS = "videos";
    public static final String PASTA_THUMBNAILS = "thumbnails";

    private final Path raiz;

    public FileStorageService(@Value("${ptdb.storage.location:uploads}") String location) {
        this.raiz = Paths.get(location).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(raiz.resolve(PASTA_VIDEOS));
            Files.createDirectories(raiz.resolve(PASTA_THUMBNAILS));
        } catch (IOException e) {
            throw new StorageException("Nao foi possivel inicializar o diretorio de uploads.", e);
        }
    }

    /**
     * Grava um arquivo de upload na subpasta informada, validando que o
     * tipo de conteudo comeca pelo prefixo esperado (ex.: "video/", "image/").
     *
     * @return o nome do arquivo gerado (a ser persistido no banco).
     */
    public String armazenar(MultipartFile arquivo, String subpasta, String prefixoTipoEsperado) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new StorageException("Arquivo vazio ou nao informado.");
        }
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith(prefixoTipoEsperado)) {
            throw new StorageException("Tipo de arquivo invalido. Esperado: " + prefixoTipoEsperado + "*");
        }

        String nomeOriginal = StringUtils.cleanPath(
                arquivo.getOriginalFilename() == null ? "" : arquivo.getOriginalFilename());
        String extensao = StringUtils.getFilenameExtension(nomeOriginal);
        String nomeGerado = UUID.randomUUID().toString()
                + (extensao != null && !extensao.isBlank() ? "." + extensao : "");

        Path destino = raiz.resolve(subpasta).resolve(nomeGerado).normalize();
        // Protege contra path traversal.
        if (!destino.getParent().equals(raiz.resolve(subpasta))) {
            throw new StorageException("Caminho de destino invalido.");
        }

        try (InputStream in = arquivo.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Falha ao gravar o arquivo enviado.", e);
        }
        return nomeGerado;
    }

    /**
     * Carrega um arquivo previamente armazenado como {@link Resource} para
     * download/streaming.
     */
    public Resource carregar(String subpasta, String nomeArquivo) {
        try {
            Path caminho = raiz.resolve(subpasta).resolve(nomeArquivo).normalize();
            if (!caminho.getParent().equals(raiz.resolve(subpasta))) {
                throw new StorageException("Caminho de arquivo invalido.");
            }
            Resource resource = new UrlResource(caminho.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new StorageException("Arquivo nao encontrado: " + nomeArquivo);
            }
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new StorageException("Arquivo nao encontrado: " + nomeArquivo, e);
        }
    }

    /** Remove um arquivo do disco (uso opcional; a exclusao do RF06 e logica). */
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
