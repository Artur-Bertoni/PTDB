package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.favorito.Favorito;
import br.edu.dombosco.ptdb.favorito.FavoritoRepository;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.storage.FileStorageService;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {

    private static final String TIPO_VIDEO = "video/";
    private static final String TIPO_IMAGEM = "image/";

    private final VideoRepository videoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FavoritoRepository favoritoRepository;
    private final FileStorageService storage;

    public VideoService(VideoRepository videoRepository,
                        CategoriaRepository categoriaRepository,
                        UsuarioRepository usuarioRepository,
                        FavoritoRepository favoritoRepository,
                        FileStorageService storage) {
        this.videoRepository = videoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.favoritoRepository = favoritoRepository;
        this.storage = storage;
    }

    public List<Video> listarPublicados() {
        return videoRepository.findByAtivoTrueAndStatusOrderByDataPublicacaoDescIdDesc(VideoStatus.APROVADO);
    }

    public List<Video> listarParaGestao(UsuarioLogado usuario) {
        if (usuario.isAdministrador()) {
            return videoRepository.findByAtivoTrueOrderByDataPublicacaoDescIdDesc();
        }
        return videoRepository.findByAtivoTrueAndCadastradoPorIdOrderByDataPublicacaoDescIdDesc(usuario.getId());
    }

    public Map<Long, Long> contarPublicadosPorCategoria() {
        Map<Long, Long> contagem = new HashMap<>();
        List<Video> publicados = videoRepository.findByAtivoTrueAndStatus(VideoStatus.APROVADO);
        for (Video video : publicados) {
            Long categoriaId = video.getCategoria().getId();
            Long atual = contagem.get(categoriaId);
            if (atual == null) {
                atual = 0L;
            }
            contagem.put(categoriaId, atual + 1);
        }
        return contagem;
    }

    public Video buscarParaGestao(Long id, UsuarioLogado usuario) {
        Video video = videoRepository.findByIdAndAtivoTrue(id);
        if (video == null) {
            throw new RecursoNaoEncontradoException("Vídeo não encontrado.");
        }
        if (!usuario.isAdministrador() && !video.foiCadastradoPor(usuario.getId())) {
            throw new RecursoNaoEncontradoException("Vídeo não encontrado.");
        }
        return video;
    }

    public Video buscarParaExibir(Long id, UsuarioLogado usuario) {
        Video video = videoRepository.findById(id).orElse(null);
        if (video == null || !podeVisualizar(video, usuario)) {
            throw new RecursoNaoEncontradoException("Vídeo não encontrado ou indisponível.");
        }
        return video;
    }

    public void registrarVisualizacao(Video video) {
        if (video.isPublicado()) {
            video.setQtdViews(video.getQtdViews() + 1);
            videoRepository.save(video);
        }
    }

    public Video buscarPorArquivo(String nomeArquivo, UsuarioLogado usuario) {
        Video video = videoRepository.findFirstByOrigemVideoAndArquivoLink(OrigemVideo.ARQUIVO, nomeArquivo);
        if (video == null || !podeVisualizar(video, usuario)) {
            return null;
        }
        return video;
    }

    public Video buscarPorImagemCapa(String nomeArquivo, UsuarioLogado usuario) {
        Video video = videoRepository.findFirstByImagemCapa(nomeArquivo);
        if (video == null || !podeVisualizar(video, usuario)) {
            return null;
        }
        return video;
    }

    public boolean podeVisualizar(Video video, UsuarioLogado usuario) {
        if (usuario == null) {
            return false;
        }
        if (usuario.isAdministrador()) {
            return true;
        }
        if (!video.isAtivo()) {
            return false;
        }
        if (video.getStatus() == VideoStatus.APROVADO) {
            return true;
        }
        return usuario.isAvaliador() || video.foiCadastradoPor(usuario.getId());
    }

    public Map<String, String> validar(VideoForm form, Video atual) {
        Map<String, String> erros = new LinkedHashMap<>();

        if (vazio(form.getTitulo())) {
            erros.put("titulo", "O título é obrigatório.");
        } else if (form.getTitulo().length() > 150) {
            erros.put("titulo", "O título deve ter no máximo 150 caracteres.");
        }

        if (vazio(form.getDescricao())) {
            erros.put("descricao", "A descrição é obrigatória.");
        } else if (form.getDescricao().length() > 4000) {
            erros.put("descricao", "A descrição deve ter no máximo 4000 caracteres.");
        }

        if (vazio(form.getAutor())) {
            erros.put("autor", "O autor é obrigatório.");
        } else if (form.getAutor().length() > 150) {
            erros.put("autor", "O autor deve ter no máximo 150 caracteres.");
        }

        if (form.getCategoriaId() == null) {
            erros.put("categoriaId", "A categoria é obrigatória.");
        } else if (!categoriaRepository.existsByIdAndAtivaTrue(form.getCategoriaId())) {
            erros.put("categoriaId", "Selecione uma categoria válida.");
        }

        if (form.getDataPublicacao() == null) {
            erros.put("dataPublicacao", "A data de publicação é obrigatória.");
        }

        if (form.getOrigem() == null) {
            erros.put("origem", "Informe se o vídeo será um link ou um arquivo.");
        } else if (form.getOrigem() == OrigemVideo.LINK) {
            if (vazio(form.getLink())) {
                erros.put("link", "Informe o link do vídeo.");
            } else if (form.getLink().length() > 500) {
                erros.put("link", "O link deve ter no máximo 500 caracteres.");
            } else if (!LinkVideo.valido(form.getLink())) {
                erros.put("link", "Informe um link válido, começando com http:// ou https://.");
            }
        } else {
            boolean enviouArquivo = temArquivo(form.getArquivoVideo());
            boolean jaTemArquivo = atual != null && atual.isArquivo();
            if (!enviouArquivo && !jaTemArquivo) {
                erros.put("arquivoVideo", "Selecione o arquivo do vídeo.");
            } else if (enviouArquivo && !tipoAceito(form.getArquivoVideo(), TIPO_VIDEO)) {
                erros.put("arquivoVideo", "O arquivo enviado não é um vídeo (use MP4, WebM ou similar).");
            }
        }

        if (temArquivo(form.getImagemCapa()) && !tipoAceito(form.getImagemCapa(), TIPO_IMAGEM)) {
            erros.put("imagemCapa", "A imagem de capa deve ser uma imagem (JPG, PNG ou similar).");
        }
        return erros;
    }

    public Video criar(VideoForm form, UsuarioLogado usuario) {
        Video video = new Video();
        video.setDataCadastro(LocalDateTime.now());
        Usuario cadastradoPor = usuarioRepository.findById(usuario.getId()).orElse(null);
        video.setCadastradoPor(cadastradoPor);
        preencher(video, form, usuario);
        return videoRepository.save(video);
    }

    public Video atualizar(Long id, VideoForm form, UsuarioLogado usuario) {
        Video video = buscarParaGestao(id, usuario);
        video.setDataAtualizacao(LocalDateTime.now());
        preencher(video, form, usuario);
        return videoRepository.save(video);
    }

    @Transactional
    public void excluir(Long id, UsuarioLogado usuario) {
        Video video = buscarParaGestao(id, usuario);
        video.setAtivo(false);
        video.setDataAtualizacao(LocalDateTime.now());
        videoRepository.save(video);

        List<Favorito> favoritos = favoritoRepository.findByVideoId(id);
        favoritoRepository.deleteAll(favoritos);
    }

    private void preencher(Video video, VideoForm form, UsuarioLogado usuario) {
        video.setTitulo(form.getTitulo().trim());
        video.setDescricao(form.getDescricao().trim());
        video.setAutor(form.getAutor().trim());
        video.setCategoria(buscarCategoria(form.getCategoriaId()));
        video.setDataPublicacao(form.getDataPublicacao());
        if (usuario.isAdministrador()) {
            video.setStatus(VideoStatus.APROVADO);
        } else {
            video.setStatus(VideoStatus.AGUARDANDO_AVALIACAO);
        }
        video.setAtivo(true);
        aplicarMidia(video, form);
        aplicarImagemCapa(video, form.getImagemCapa());
    }

    private void aplicarMidia(Video video, VideoForm form) {
        String arquivoAnterior = null;
        if (video.isArquivo()) {
            arquivoAnterior = video.getArquivoLink();
        }

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
        if (!temArquivo(arquivo)) {
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
        if (!temArquivo(capa)) {
            return;
        }
        String anterior = video.getImagemCapa();
        video.setImagemCapa(storage.armazenar(capa, FileStorageService.PASTA_CAPAS, TIPO_IMAGEM));
        storage.remover(FileStorageService.PASTA_CAPAS, anterior);
    }

    private Categoria buscarCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new RegraNegocioException("A categoria é obrigatória.");
        }
        Categoria categoria = categoriaRepository.findByIdAndAtivaTrue(categoriaId);
        if (categoria == null) {
            throw new RegraNegocioException("Selecione uma categoria válida.");
        }
        return categoria;
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean temArquivo(MultipartFile arquivo) {
        return arquivo != null && !arquivo.isEmpty();
    }

    private boolean tipoAceito(MultipartFile arquivo, String prefixo) {
        String tipo = arquivo.getContentType();
        return tipo != null && tipo.toLowerCase().startsWith(prefixo);
    }
}
