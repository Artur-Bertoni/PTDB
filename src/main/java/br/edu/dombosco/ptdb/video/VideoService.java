package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.storage.FileStorageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {

    private static final String TIPO_VIDEO = "video/";
    private static final String TIPO_IMAGEM = "image/";

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

    @Transactional(readOnly = true)
    public List<Video> listar(String termo, Long categoriaId, VideoStatus status) {
        String termoNormalizado = (termo != null && !termo.isBlank()) ? termo.trim() : null;
        return videoRepository.buscar(termoNormalizado, categoriaId, status);
    }

    @Transactional(readOnly = true)
    public List<Video> listarAprovados(Long categoriaId) {
        return videoRepository.buscar(null, categoriaId, VideoStatus.APROVADO);
    }

    @Transactional(readOnly = true)
    public IndicadoresVideos indicadores() {
        return new IndicadoresVideos(
                videoRepository.countByAtivoTrue(),
                videoRepository.countByAtivoTrueAndStatus(VideoStatus.AGUARDANDO_AVALIACAO),
                videoRepository.countByAtivoTrueAndStatus(VideoStatus.APROVADO),
                videoRepository.countByAtivoTrueAndStatus(VideoStatus.REPROVADO));
    }

    @Transactional(readOnly = true)
    public Video buscarPorId(Long id) {
        return videoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Vídeo não encontrado (id=" + id + ")."));
    }

    @Transactional(readOnly = true)
    public Optional<Video> buscarPorArquivo(String nomeArquivo, PerfilUsuario perfil) {
        return videoRepository.findFirstByOrigemVideoAndArquivoLink(OrigemVideo.ARQUIVO, nomeArquivo)
                .filter(video -> podeVisualizar(video, perfil));
    }

    @Transactional(readOnly = true)
    public Optional<Video> buscarPorImagemCapa(String nomeArquivo, PerfilUsuario perfil) {
        return videoRepository.findFirstByImagemCapa(nomeArquivo)
                .filter(video -> podeVisualizar(video, perfil));
    }

    public boolean podeVisualizar(Video video, PerfilUsuario perfil) {
        if (perfil == PerfilUsuario.ADMINISTRADOR) {
            return true;
        }
        if (!video.isAtivo()) {
            return false;
        }
        return video.getStatus() == VideoStatus.APROVADO || perfil == PerfilUsuario.AVALIADOR;
    }

    public void validar(VideoForm form, Video atual, Errors errors) {
        if (form.getCategoriaId() != null && !categoriaRepository.existsById(form.getCategoriaId())) {
            errors.rejectValue("categoriaId", "categoria.invalida", "Selecione uma categoria válida.");
        }

        if (form.getOrigem() == OrigemVideo.LINK) {
            if (form.getLink() == null || form.getLink().isBlank()) {
                errors.rejectValue("link", "link.obrigatorio", "Informe o link do vídeo.");
            } else if (!LinkVideo.valido(form.getLink())) {
                errors.rejectValue("link", "link.invalido",
                        "Informe um link válido, começando com http:// ou https://.");
            }
        } else if (form.getOrigem() == OrigemVideo.ARQUIVO) {
            MultipartFile arquivo = form.getArquivoVideo();
            boolean enviado = arquivo != null && !arquivo.isEmpty();
            boolean possuiArquivoAtual = atual != null && atual.isArquivo();
            if (!enviado && !possuiArquivoAtual) {
                errors.rejectValue("arquivoVideo", "arquivo.obrigatorio", "Selecione o arquivo do vídeo.");
            } else if (enviado && !tipoAceito(arquivo, TIPO_VIDEO)) {
                errors.rejectValue("arquivoVideo", "arquivo.tipo",
                        "O arquivo enviado não é um vídeo (use MP4, WebM ou similar).");
            }
        }

        MultipartFile capa = form.getImagemCapa();
        if (capa != null && !capa.isEmpty() && !tipoAceito(capa, TIPO_IMAGEM)) {
            errors.rejectValue("imagemCapa", "capa.tipo",
                    "A imagem de capa deve ser uma imagem (JPG, PNG ou similar).");
        }
    }

    @Transactional
    public Video criar(VideoForm form) {
        Video video = new Video();
        video.setDataCadastro(LocalDateTime.now());
        preencher(video, form);
        return videoRepository.save(video);
    }

    @Transactional
    public Video atualizar(Long id, VideoForm form) {
        Video video = buscarPorId(id);
        video.setDataAtualizacao(LocalDateTime.now());
        preencher(video, form);
        return videoRepository.save(video);
    }

    @Transactional
    public void excluir(Long id) {
        Video video = buscarPorId(id);
        video.setAtivo(false);
        video.setDataAtualizacao(LocalDateTime.now());
        videoRepository.save(video);
    }

    private void preencher(Video video, VideoForm form) {
        video.setTitulo(form.getTitulo().trim());
        video.setDescricao(form.getDescricao().trim());
        video.setCategoria(resolverCategoria(form.getCategoriaId()));
        video.setDataPublicacao(form.getDataPublicacao());
        video.setStatus(VideoStatus.AGUARDANDO_AVALIACAO);
        video.setAtivo(true);
        aplicarMidia(video, form);
        aplicarImagemCapa(video, form.getImagemCapa());
    }

    private void aplicarMidia(Video video, VideoForm form) {
        String arquivoAnterior = video.isArquivo() ? video.getArquivoLink() : null;

        if (form.getOrigem() == OrigemVideo.LINK) {
            if (!LinkVideo.valido(form.getLink())) {
                throw new RegraNegocioException("Informe um link válido para o vídeo.");
            }
            video.setOrigemVideo(OrigemVideo.LINK);
            video.setArquivoLink(form.getLink().trim());
            video.setNomeArquivoOriginal(null);
            storage.remover(FileStorageService.PASTA_VIDEOS, arquivoAnterior);
            return;
        }

        MultipartFile arquivo = form.getArquivoVideo();
        if (arquivo == null || arquivo.isEmpty()) {
            if (!video.isArquivo()) {
                throw new RegraNegocioException("Selecione o arquivo do vídeo.");
            }
            return;
        }
        video.setArquivoLink(storage.armazenar(arquivo, FileStorageService.PASTA_VIDEOS, TIPO_VIDEO));
        video.setOrigemVideo(OrigemVideo.ARQUIVO);
        video.setNomeArquivoOriginal(arquivo.getOriginalFilename());
        storage.remover(FileStorageService.PASTA_VIDEOS, arquivoAnterior);
    }

    private void aplicarImagemCapa(Video video, MultipartFile capa) {
        if (capa == null || capa.isEmpty()) {
            return;
        }
        String anterior = video.getImagemCapa();
        video.setImagemCapa(storage.armazenar(capa, FileStorageService.PASTA_CAPAS, TIPO_IMAGEM));
        storage.remover(FileStorageService.PASTA_CAPAS, anterior);
    }

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new RegraNegocioException("A categoria é obrigatória.");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RegraNegocioException("Selecione uma categoria válida."));
    }

    private static boolean tipoAceito(MultipartFile arquivo, String prefixo) {
        String tipo = arquivo.getContentType();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).startsWith(prefixo);
    }
}
