package br.edu.dombosco.ptdb.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redireciona a raiz para a tela de gerenciamento de videos (RF06).
 * Provisorio ate a existencia da tela inicial/administrativa (RF03/RF09).
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/videos";
    }
}
