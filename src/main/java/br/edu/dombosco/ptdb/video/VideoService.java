package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.storage.FileStorageService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Regras de negocio do RF06 - Manter videos.
 *
 * <p>Concentra o CRUD do Administrador: cadastro, consulta, edicao e
 * exclusao logica dos videos, alem das regras associadas (campos
 * obrigatorios, tratamento de upload e ciclo de status de avaliacao).
 */
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FileStorageService storage;

    public VideoService(VideoRepository videoRepository,
                        CategoriaRepository categoriaRepository,
                        FileStorageService storage) {
        this.videoRepository = videoRepository;
        this.categoriaRepository = categoriaRepository;
        this.storage = storage;
    }

    /** Consulta (Read) com filtro por texto, categoria e status. */
    @Transactional(readOnly = true)
    public List<Video> listar(String termo, Long categoriaId, VideoStatus status) {
        String termoNormalizado = (termo != null && !termo.isBlank()) ? termo.trim() : null;
        return videoRepository.buscar(termoNormalizado, categoriaId, status);
    }

    /** Busca um video ativo pelo id ou lanca excecao. */
    @Transactional(readOnly = true)
    public Video buscarPorId(Long id) {
        return videoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Video nao encontrado (id=" + id + ")."));
    }

    /**
     * Cadastra (Create) um novo video.
     *
     * <p>Regras: campos obrigatorios validados; arquivo de video obrigatorio;
     * status inicial "Aguardando avaliacao"; registro criado como ativo.
     */
    @Transactional
    public Video criar(VideoForm form) {
        Categoria categoria = resolverCategoria(form.getCategoriaId());

        MultipartFile arquivo = form.getArquivoVideo();
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("O arquivo do video e obrigatorio no cadastro.");
        }

        Video video = new Video();
        video.setTitulo(form.getTitulo().trim());
        video.setDescricao(form.getDescricao().trim());
        video.setCategoria(categoria);
        video.setDataPublicacao(form.getDataPublicacao());
        video.setStatus(VideoStatus.AGUARDANDO_AVALIACAO);
        video.setAtivo(true);
        video.setCriadoEm(LocalDateTime.now());

        video.setArquivoVideo(storage.armazenar(
                arquivo, FileStorageService.PASTA_VIDEOS, "video/"));
        video.setArquivoVideoOriginal(arquivo.getOriginalFilename());

        aplicarThumbnailSeEnviada(video, form.getThumbnail());

        return videoRepository.save(video);
    }

    /**
     * Edita (Update) um video existente.
     *
     * <p>Regras: campos obrigatorios validados; se um novo arquivo for
     * enviado ele substitui o anterior, caso contrario o arquivo atual e
     * mantido; ao ser editado, o video retorna para "Aguardando avaliacao"
     * (RF06/RF08).
     */
    @Transactional
    public Video atualizar(Long id, VideoForm form) {
        Video video = buscarPorId(id);
        Categoria categoria = resolverCategoria(form.getCategoriaId());

        video.setTitulo(form.getTitulo().trim());
        video.setDescricao(form.getDescricao().trim());
        video.setCategoria(categoria);
        video.setDataPublicacao(form.getDataPublicacao());
        // Edicao reabre o fluxo de avaliacao (RF08).
        video.setStatus(VideoStatus.AGUARDANDO_AVALIACAO);
        video.setAtualizadoEm(LocalDateTime.now());

        MultipartFile arquivo = form.getArquivoVideo();
        if (arquivo != null && !arquivo.isEmpty()) {
            String anterior = video.getArquivoVideo();
            video.setArquivoVideo(storage.armazenar(
                    arquivo, FileStorageService.PASTA_VIDEOS, "video/"));
            video.setArquivoVideoOriginal(arquivo.getOriginalFilename());
            storage.remover(FileStorageService.PASTA_VIDEOS, anterior);
        }

        aplicarThumbnailSeEnviada(video, form.getThumbnail());

        return videoRepository.save(video);
    }

    /**
     * Exclusao logica (Delete) - inativa o registro sem apaga-lo, preservando
     * a integridade referencial com favoritos (RF07) e avaliacao (RF08).
     */
    @Transactional
    public void excluir(Long id) {
        Video video = buscarPorId(id);
        video.setAtivo(false);
        video.setAtualizadoEm(LocalDateTime.now());
        videoRepository.save(video);
    }

    // ---- apoio ----

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new RegraNegocioException("A categoria e obrigatoria.");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Categoria invalida (id=" + categoriaId + ")."));
    }

    private void aplicarThumbnailSeEnviada(Video video, MultipartFile thumbnail) {
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String anterior = video.getThumbnail();
            video.setThumbnail(storage.armazenar(
                    thumbnail, FileStorageService.PASTA_THUMBNAILS, "image/"));
            storage.remover(FileStorageService.PASTA_THUMBNAILS, anterior);
        }
    }
}
