package br.edu.dombosco.ptdb.painel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping("/admin")
    public String painel(Model model) {
        model.addAttribute("indicadores", painelService.carregarIndicadores());
        model.addAttribute("atividades", painelService.atividadesRecentes());
        model.addAttribute("fila", painelService.filaDeAvaliacao());
        return "painel/painel";
    }
}
