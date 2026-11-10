package br.edu.dombosco.ptdb.faq;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/faq")
public class FaqAdminController {

    private final FaqService faqService;

    public FaqAdminController(FaqService faqService) {
        this.faqService = faqService;
    }

    @PostMapping
    public ResponseEntity<Object> criar(@RequestBody ItemFaqForm form) {
        Map<String, String> erros = faqService.validar(form);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros);
        }
        faqService.criar(form);
        return ResponseEntity.ok(Map.of("mensagem", "Pergunta cadastrada."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizar(@PathVariable Long id, @RequestBody ItemFaqForm form) {
        faqService.buscarPorId(id);
        Map<String, String> erros = faqService.validar(form);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros);
        }
        faqService.atualizar(id, form);
        return ResponseEntity.ok(Map.of("mensagem", "Pergunta atualizada."));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> excluir(@PathVariable Long id) {
        faqService.excluir(id);
        return Map.of("mensagem", "Pergunta excluída.");
    }
}
