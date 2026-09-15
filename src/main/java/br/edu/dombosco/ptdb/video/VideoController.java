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

/**
 * Controller do RF06 - Manter videos (area do Administrador).
 *
 * <p>Expõe a listagem com filtro/busca e o formulario de cadastro/edicao,
 * alem das acoes de exclusao logica. Todas as rotas ficam sob
 * {@code /admin/videos} e sao protegidas pelo interceptor de Administrador.
 */
@Controller
@RequestMapping("/admin/videos")
public class VideoController {

    private final VideoService videoService;
    private final CategoriaRepository categoriaRepository;

    public VideoController(VideoService videoService, CategoriaRepository categoriaRepository) {
        this.videoService = videoService;
        this.categoriaRepository = categoriaRepository;
    }

    /** Consulta (Read) - listagem com filtro por texto, categoria e status. */
    @GetMapping
    public String listar(@RequestParam(required = false) String termo,
                         @RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false) VideoStatus status,
                         Model model) {
        model.addAttribute("videos", videoService.listar(termo, categoriaId, status));
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("statuses", VideoStatus.values());
        model.addAttribute("termo", termo);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("status", status);
        return "videos/lista";
    }

    /** Formulario de cadastro (Create). */
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new VideoForm());
        model.addAttribute("edicao", false);
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "videos/formulario";
    }

    /** Persiste o cadastro (Create). */
    @PostMapping
    public String criar(@Valid @ModelAttribute("form") VideoForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("edicao", false);
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "videos/formulario";
        }
        videoService.criar(form);
        redirect.addFlashAttribute("mensagem",
                "Video cadastrado com sucesso (aguardando avaliacao).");
        return "redirect:/admin/videos";
    }

    /** Formulario de edicao (Update). */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Video video = videoService.buscarPorId(id);

        VideoForm form = new VideoForm();
        form.setId(video.getId());
        form.setTitulo(video.getTitulo());
        form.setDescricao(video.getDescricao());
        form.setCategoriaId(video.getCategoria().getId());
        form.setDataPublicacao(video.getDataPublicacao());

        model.addAttribute("form", form);
        model.addAttribute("edicao", true);
        model.addAttribute("video", video);
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "videos/formulario";
    }

    /** Persiste a edicao (Update). */
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") VideoForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("edicao", true);
            model.addAttribute("video", videoService.buscarPorId(id));
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "videos/formulario";
        }
        videoService.atualizar(id, form);
        redirect.addFlashAttribute("mensagem",
                "Video atualizado com sucesso (retornou para avaliacao).");
        return "redirect:/admin/videos";
    }

    /** Exclusao logica (Delete) - inativacao do registro. */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        videoService.excluir(id);
        redirect.addFlashAttribute("mensagem", "Video excluido (inativado) com sucesso.");
        return "redirect:/admin/videos";
    }
}
