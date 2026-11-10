package br.edu.dombosco.ptdb.faq;

import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FaqService {

    private final ItemFaqRepository repository;

    public FaqService(ItemFaqRepository repository) {
        this.repository = repository;
    }

    public List<ItemFaq> listar() {
        return repository.findAllByOrderByCategoriaTemaAscIdAsc();
    }

    public ItemFaq buscarPorId(Long id) {
        ItemFaq item = repository.findById(id).orElse(null);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Pergunta não encontrada (id=" + id + ").");
        }
        return item;
    }

    public Map<String, String> validar(ItemFaqForm form) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (form.getPergunta() == null || form.getPergunta().isBlank()) {
            erros.put("pergunta", "A pergunta é obrigatória.");
        } else if (form.getPergunta().length() > 300) {
            erros.put("pergunta", "A pergunta deve ter no máximo 300 caracteres.");
        }
        if (form.getResposta() == null || form.getResposta().isBlank()) {
            erros.put("resposta", "A resposta é obrigatória.");
        } else if (form.getResposta().length() > 4000) {
            erros.put("resposta", "A resposta deve ter no máximo 4000 caracteres.");
        }
        if (form.getCategoriaTema() != null && form.getCategoriaTema().length() > 100) {
            erros.put("categoriaTema", "O tema deve ter no máximo 100 caracteres.");
        }
        return erros;
    }

    public ItemFaq criar(ItemFaqForm form) {
        ItemFaq item = new ItemFaq();
        item.setDataCadastro(LocalDateTime.now());
        preencher(item, form);
        return repository.save(item);
    }

    public ItemFaq atualizar(Long id, ItemFaqForm form) {
        ItemFaq item = buscarPorId(id);
        item.setDataAtualizacao(LocalDateTime.now());
        preencher(item, form);
        return repository.save(item);
    }

    public void excluir(Long id) {
        ItemFaq item = buscarPorId(id);
        repository.delete(item);
    }

    private void preencher(ItemFaq item, ItemFaqForm form) {
        item.setPergunta(form.getPergunta().trim());
        item.setResposta(form.getResposta().trim());
        String tema = form.getCategoriaTema();
        if (tema == null || tema.isBlank()) {
            item.setCategoriaTema(null);
        } else {
            item.setCategoriaTema(tema.trim());
        }
    }
}
