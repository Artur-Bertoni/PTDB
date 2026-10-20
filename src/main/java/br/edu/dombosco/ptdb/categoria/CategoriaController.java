package br.edu.dombosco.ptdb.categoria;

import br.edu.dombosco.ptdb.video.VideoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaController {

    private static final String VIEW = "categorias/lista";
    private static final String REDIRECT_LISTA = "redirect:/admin/categorias";

    private final CategoriaService categoriaService;
    private final VideoService videoService;

    public CategoriaController(CategoriaService categoriaService, VideoService videoService) {
        this.categoriaService = categoriaService;
        this.videoService = videoService;
    }

    @GetMapping
    public String listar(Model model) {
        prepararListagem(model);
        return VIEW;
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        prepararListagem(model);
        abrirFormulario(model, new CategoriaForm(), false);
        return VIEW;
    }

    @PostMapping
    public String criar(@ModelAttribute("form") CategoriaForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        form.setId(null);
        categoriaService.validar(form, result);
        if (result.hasErrors()) {
            prepararListagem(model);
            abrirFormulario(model, form, false);
            return VIEW;
        }
        categoriaService.criar(form);
        redirect.addFlashAttribute("mensagem", "Categoria cadastrada.");
        return REDIRECT_LISTA;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);
        CategoriaForm form = new CategoriaForm();
        form.setId(categoria.getId());
        form.setNome(categoria.getNome());
        form.setDescricao(categoria.getDescricao());
        prepararListagem(model);
        abrirFormulario(model, form, true);
        return VIEW;
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @ModelAttribute("form") CategoriaForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        categoriaService.buscarPorId(id);
        form.setId(id);
        categoriaService.validar(form, result);
        if (result.hasErrors()) {
            prepararListagem(model);
            abrirFormulario(model, form, true);
            return VIEW;
        }
        categoriaService.atualizar(id, form);
        redirect.addFlashAttribute("mensagem", "Categoria atualizada.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        categoriaService.excluir(id);
        redirect.addFlashAttribute("mensagem", "Categoria excluída.");
        return REDIRECT_LISTA;
    }

    private void prepararListagem(Model model) {
        model.addAttribute("categorias", categoriaService.listarAtivas());
        model.addAttribute("publicadosPorCategoria", videoService.contarPublicadosPorCategoria());
    }

    private static void abrirFormulario(Model model, CategoriaForm form, boolean edicao) {
        model.addAttribute("form", form);
        model.addAttribute("edicao", edicao);
    }
}
