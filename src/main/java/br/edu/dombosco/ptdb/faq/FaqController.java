package br.edu.dombosco.ptdb.faq;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping("/faq")
    public String faq(@RequestParam(required = false) String termo,
                      @RequestParam(required = false) String tema,
                      Model model) {
        model.addAttribute("grupos", faqService.agruparPorTema(termo, tema));
        model.addAttribute("temas", faqService.temas());
        model.addAttribute("termo", termo);
        model.addAttribute("tema", tema);
        return "faq/faq";
    }
}
