package br.edu.dombosco.ptdb.faq;

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
@RequestMapping("/admin/faq")
public class FaqAdminController {

    private static final String VIEW = "faq/gerenciar";
    private static final String REDIRECT_LISTA = "redirect:/admin/faq";

    private final FaqService faqService;

    public FaqAdminController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String termo, Model model) {
        prepararListagem(model, termo);
        return VIEW;
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararListagem(model, null);
        abrirFormulario(model, new ItemFaqForm(), false);
        return VIEW;
    }

    @PostMapping
    public String criar(@ModelAttribute("form") ItemFaqForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        faqService.validar(form, result);
        if (result.hasErrors()) {
            prepararListagem(model, null);
            abrirFormulario(model, form, false);
            return VIEW;
        }
        faqService.criar(form);
        redirect.addFlashAttribute("mensagem", "Pergunta cadastrada.");
        return REDIRECT_LISTA;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ItemFaq item = faqService.buscarPorId(id);
        ItemFaqForm form = new ItemFaqForm();
        form.setId(item.getId());
        form.setPergunta(item.getPergunta());
        form.setResposta(item.getResposta());
        form.setCategoriaTema(item.getCategoriaTema());
        prepararListagem(model, null);
        abrirFormulario(model, form, true);
        return VIEW;
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @ModelAttribute("form") ItemFaqForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        faqService.buscarPorId(id);
        form.setId(id);
        faqService.validar(form, result);
        if (result.hasErrors()) {
            prepararListagem(model, null);
            abrirFormulario(model, form, true);
            return VIEW;
        }
        faqService.atualizar(id, form);
        redirect.addFlashAttribute("mensagem", "Pergunta atualizada.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        faqService.excluir(id);
        redirect.addFlashAttribute("mensagem", "Pergunta excluída.");
        return REDIRECT_LISTA;
    }

    private void prepararListagem(Model model, String termo) {
        model.addAttribute("itens", faqService.listar(termo));
        model.addAttribute("temas", faqService.temas());
        model.addAttribute("termo", termo);
    }

    private void abrirFormulario(Model model, ItemFaqForm form, boolean edicao) {
        model.addAttribute("form", form);
        model.addAttribute("edicao", edicao);
    }
}
