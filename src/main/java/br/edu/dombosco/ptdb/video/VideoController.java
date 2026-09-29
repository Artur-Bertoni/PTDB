package br.edu.dombosco.ptdb.video;

import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/videos")
public class VideoController {

    private static final String VIEW = "videos/lista";
    private static final String REDIRECT_LISTA = "redirect:/admin/videos";

    private final VideoService videoService;
    private final CategoriaRepository categoriaRepository;

    public VideoController(VideoService videoService, CategoriaRepository categoriaRepository) {
        this.videoService = videoService;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String termo,
                         @RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false) VideoStatus status,
                         Model model) {
        prepararListagem(model, termo, categoriaId, status);
        return VIEW;
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararListagem(model, null, null, null);
        abrirFormulario(model, new VideoForm(), null);
        return VIEW;
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") VideoForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        videoService.validar(form, null, result);
        if (result.hasErrors()) {
            prepararListagem(model, null, null, null);
            abrirFormulario(model, form, null);
            return VIEW;
        }
        videoService.criar(form);
        redirect.addFlashAttribute("mensagem", "Vídeo cadastrado. Ele ficará aguardando avaliação.");
        return REDIRECT_LISTA;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Video video = videoService.buscarPorId(id);
        VideoForm form = new VideoForm();
        form.setId(video.getId());
        form.setTitulo(video.getTitulo());
        form.setDescricao(video.getDescricao());
        form.setCategoriaId(video.getCategoria().getId());
        form.setDataPublicacao(video.getDataPublicacao());
        form.setOrigem(video.getOrigemVideo());
        if (video.isLink()) {
            form.setLink(video.getArquivoLink());
        }
        prepararListagem(model, null, null, null);
        abrirFormulario(model, form, video);
        return VIEW;
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") VideoForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        Video atual = videoService.buscarPorId(id);
        form.setId(id);
        videoService.validar(form, atual, result);
        if (result.hasErrors()) {
            prepararListagem(model, null, null, null);
            abrirFormulario(model, form, atual);
            return VIEW;
        }
        videoService.atualizar(id, form);
        redirect.addFlashAttribute("mensagem", "Vídeo atualizado. Ele voltou para avaliação.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        videoService.excluir(id);
        redirect.addFlashAttribute("mensagem", "Vídeo excluído.");
        return REDIRECT_LISTA;
    }

    private void prepararListagem(Model model, String termo, Long categoriaId, VideoStatus status) {
        model.addAttribute("videos", videoService.listar(termo, categoriaId, status));
        model.addAttribute("indicadores", videoService.indicadores());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("statuses", VideoStatus.values());
        model.addAttribute("termo", termo);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("status", status);
    }

    private void abrirFormulario(Model model, VideoForm form, Video video) {
        model.addAttribute("form", form);
        model.addAttribute("edicao", video != null);
        model.addAttribute("video", video);
        model.addAttribute("origens", OrigemVideo.values());
    }
}
