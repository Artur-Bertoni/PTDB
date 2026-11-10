package br.edu.dombosco.ptdb.painel;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping("/api/admin/painel")
    public Map<String, Object> painel() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("indicadores", painelService.carregarIndicadores());
        dados.put("atividades", painelService.atividadesRecentes());
        dados.put("fila", painelService.filaDeAvaliacao());
        return dados;
    }
}
