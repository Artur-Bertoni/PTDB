package br.edu.dombosco.ptdb.categoria;

import br.edu.dombosco.ptdb.video.VideoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final VideoService videoService;

    public CategoriaController(CategoriaService categoriaService, VideoService videoService) {
        this.categoriaService = categoriaService;
        this.videoService = videoService;
    }

    @GetMapping
    public List<CategoriaResumo> listar() {
        Map<Long, Long> contagem = videoService.contarPublicadosPorCategoria();
        List<CategoriaResumo> lista = new ArrayList<>();
        for (Categoria categoria : categoriaService.listarAtivas()) {
            lista.add(new CategoriaResumo(categoria, contagem.get(categoria.getId())));
        }
        return lista;
    }

    @PostMapping
    public ResponseEntity<Object> criar(@RequestBody CategoriaForm form) {
        form.setId(null);
        Map<String, String> erros = categoriaService.validar(form);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros);
        }
        categoriaService.criar(form);
        return ResponseEntity.ok(Map.of("mensagem", "Categoria cadastrada."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> atualizar(@PathVariable Long id, @RequestBody CategoriaForm form) {
        categoriaService.buscarPorId(id);
        form.setId(id);
        Map<String, String> erros = categoriaService.validar(form);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros);
        }
        categoriaService.atualizar(id, form);
        return ResponseEntity.ok(Map.of("mensagem", "Categoria atualizada."));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return Map.of("mensagem", "Categoria excluída.");
    }
}
