package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.favorito.Favorito;
import br.edu.dombosco.ptdb.favorito.FavoritoRepository;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.storage.FileStorageService;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<Video> listar(String termo, Long categoriaId, VideoStatus status) {
        List<Video> todos = videoRepository.findByAtivoTrueOrderByDataPublicacaoDescIdDesc();
        List<Video> resultado = new ArrayList<>();
        for (Video video : todos) {
            if (categoriaId != null && !video.getCategoria().getId().equals(categoriaId)) {
                continue;
            }
            if (status != null && video.getStatus() != status) {
                continue;
            }
            if (!contemTermo(video, termo)) {
                continue;
            }
            resultado.add(video);
        }
        return resultado;
    }

    public List<Video> listarPublicados(String termo, Long categoriaId) {
        return listar(termo, categoriaId, VideoStatus.APROVADO);
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

    public IndicadoresVideos indicadores() {
        return new IndicadoresVideos(
                videoRepository.countByAtivoTrue(),
                videoRepository.countByAtivoTrueAndStatus(VideoStatus.AGUARDANDO_AVALIACAO),
                videoRepository.countByAtivoTrueAndStatus(VideoStatus.APROVADO),
                videoRepository.countByAtivoTrueAndStatus(VideoStatus.REPROVADO));
    }

    public Video buscarPorId(Long id) {
        Video video = videoRepository.findByIdAndAtivoTrue(id);
        if (video == null) {
            throw new RecursoNaoEncontradoException("Vídeo não encontrado (id=" + id + ").");
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
        if (usuario != null && usuario.getPerfil() == PerfilUsuario.ADMINISTRADOR) {
            return true;
        }
        if (!video.isAtivo()) {
            return false;
        }
        if (video.getStatus() == VideoStatus.APROVADO) {
            return true;
        }
        return usuario != null && usuario.getPerfil() == PerfilUsuario.AVALIADOR;
    }

    public void validar(VideoForm form, Video atual, Errors errors) {
        if (vazio(form.getTitulo())) {
            errors.rejectValue("titulo", "obrigatorio", "O título é obrigatório.");
        } else if (form.getTitulo().length() > 150) {
            errors.rejectValue("titulo", "tamanho", "O título deve ter no máximo 150 caracteres.");
        }

        if (vazio(form.getDescricao())) {
            errors.rejectValue("descricao", "obrigatorio", "A descrição é obrigatória.");
        } else if (form.getDescricao().length() > 4000) {
            errors.rejectValue("descricao", "tamanho", "A descrição deve ter no máximo 4000 caracteres.");
        }

        if (vazio(form.getAutor())) {
            errors.rejectValue("autor", "obrigatorio", "O autor é obrigatório.");
        } else if (form.getAutor().length() > 150) {
            errors.rejectValue("autor", "tamanho", "O autor deve ter no máximo 150 caracteres.");
        }

        if (form.getCategoriaId() == null) {
            errors.rejectValue("categoriaId", "obrigatorio", "A categoria é obrigatória.");
        } else if (!categoriaRepository.existsByIdAndAtivaTrue(form.getCategoriaId())) {
            errors.rejectValue("categoriaId", "invalida", "Selecione uma categoria válida.");
        }

        if (form.getDataPublicacao() == null) {
            errors.rejectValue("dataPublicacao", "obrigatorio", "A data de publicação é obrigatória.");
        }

        if (form.getOrigem() == null) {
            errors.rejectValue("origem", "obrigatorio", "Informe se o vídeo será um link ou um arquivo.");
        } else if (form.getOrigem() == OrigemVideo.LINK) {
            if (vazio(form.getLink())) {
                errors.rejectValue("link", "obrigatorio", "Informe o link do vídeo.");
            } else if (form.getLink().length() > 500) {
                errors.rejectValue("link", "tamanho", "O link deve ter no máximo 500 caracteres.");
            } else if (!LinkVideo.valido(form.getLink())) {
                errors.rejectValue("link", "invalido", "Informe um link válido, começando com http:// ou https://.");
            }
        } else {
            boolean enviouArquivo = temArquivo(form.getArquivoVideo());
            boolean jaTemArquivo = atual != null && atual.isArquivo();
            if (!enviouArquivo && !jaTemArquivo) {
                errors.rejectValue("arquivoVideo", "obrigatorio", "Selecione o arquivo do vídeo.");
            } else if (enviouArquivo && !tipoAceito(form.getArquivoVideo(), TIPO_VIDEO)) {
                errors.rejectValue("arquivoVideo", "tipo", "O arquivo enviado não é um vídeo (use MP4, WebM ou similar).");
            }
        }

        if (temArquivo(form.getImagemCapa()) && !tipoAceito(form.getImagemCapa(), TIPO_IMAGEM)) {
            errors.rejectValue("imagemCapa", "tipo", "A imagem de capa deve ser uma imagem (JPG, PNG ou similar).");
        }
    }

    public Video criar(VideoForm form, Long cadastradoPorId) {
        Video video = new Video();
        video.setDataCadastro(LocalDateTime.now());
        if (cadastradoPorId != null) {
            Usuario usuario = usuarioRepository.findById(cadastradoPorId).orElse(null);
            video.setCadastradoPor(usuario);
        }
        preencher(video, form);
        return videoRepository.save(video);
    }

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

        List<Favorito> favoritos = favoritoRepository.findByVideoId(id);
        favoritoRepository.deleteAll(favoritos);
    }

    private void preencher(Video video, VideoForm form) {
        video.setTitulo(form.getTitulo().trim());
        video.setDescricao(form.getDescricao().trim());
        video.setAutor(form.getAutor().trim());
        video.setCategoria(buscarCategoria(form.getCategoriaId()));
        video.setDataPublicacao(form.getDataPublicacao());
        video.setStatus(VideoStatus.AGUARDANDO_AVALIACAO);
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

    private boolean contemTermo(Video video, String termo) {
        if (vazio(termo)) {
            return true;
        }
        String busca = termo.trim().toLowerCase();
        return video.getTitulo().toLowerCase().contains(busca)
                || video.getDescricao().toLowerCase().contains(busca)
                || video.getAutor().toLowerCase().contains(busca);
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
