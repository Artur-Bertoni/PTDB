package br.edu.dombosco.ptdb.faq;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping("/api/faq")
    public List<ItemFaq> listar() {
        return faqService.listar();
    }
}
