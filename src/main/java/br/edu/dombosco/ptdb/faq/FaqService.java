package br.edu.dombosco.ptdb.faq;

import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
public class FaqService {

    private final ItemFaqRepository repository;

    public FaqService(ItemFaqRepository repository) {
        this.repository = repository;
    }

    public List<ItemFaq> listar(String termo) {
        List<ItemFaq> todos = repository.findAllByOrderByCategoriaTemaAscIdAsc();
        if (termo == null || termo.isBlank()) {
            return todos;
        }
        String busca = termo.trim().toLowerCase();
        List<ItemFaq> resultado = new ArrayList<>();
        for (ItemFaq item : todos) {
            boolean naPergunta = item.getPergunta().toLowerCase().contains(busca);
            boolean naResposta = item.getResposta().toLowerCase().contains(busca);
            boolean noTema = item.getTema().toLowerCase().contains(busca);
            if (naPergunta || naResposta || noTema) {
                resultado.add(item);
            }
        }
        return resultado;
    }

    public Map<String, List<ItemFaq>> agruparPorTema(String termo, String tema) {
        Map<String, List<ItemFaq>> grupos = new LinkedHashMap<>();
        List<ItemFaq> semTema = new ArrayList<>();

        for (ItemFaq item : listar(termo)) {
            if (tema != null && !tema.isBlank() && !item.getTema().equals(tema)) {
                continue;
            }
            if (item.getCategoriaTema() == null) {
                semTema.add(item);
                continue;
            }
            List<ItemFaq> grupo = grupos.get(item.getTema());
            if (grupo == null) {
                grupo = new ArrayList<>();
                grupos.put(item.getTema(), grupo);
            }
            grupo.add(item);
        }

        if (!semTema.isEmpty()) {
            List<ItemFaq> geral = grupos.get(ItemFaq.TEMA_PADRAO);
            if (geral == null) {
                geral = new ArrayList<>();
                grupos.put(ItemFaq.TEMA_PADRAO, geral);
            }
            geral.addAll(semTema);
        }
        return grupos;
    }

    public List<String> temas() {
        List<String> temas = new ArrayList<>();
        for (String tema : agruparPorTema(null, null).keySet()) {
            temas.add(tema);
        }
        return temas;
    }

    public long total() {
        return repository.count();
    }

    public ItemFaq buscarPorId(Long id) {
        ItemFaq item = repository.findById(id).orElse(null);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Pergunta não encontrada (id=" + id + ").");
        }
        return item;
    }

    public void validar(ItemFaqForm form, Errors errors) {
        if (form.getPergunta() == null || form.getPergunta().isBlank()) {
            errors.rejectValue("pergunta", "obrigatorio", "A pergunta é obrigatória.");
        } else if (form.getPergunta().length() > 300) {
            errors.rejectValue("pergunta", "tamanho", "A pergunta deve ter no máximo 300 caracteres.");
        }
        if (form.getResposta() == null || form.getResposta().isBlank()) {
            errors.rejectValue("resposta", "obrigatorio", "A resposta é obrigatória.");
        } else if (form.getResposta().length() > 4000) {
            errors.rejectValue("resposta", "tamanho", "A resposta deve ter no máximo 4000 caracteres.");
        }
        if (form.getCategoriaTema() != null && form.getCategoriaTema().length() > 100) {
            errors.rejectValue("categoriaTema", "tamanho", "O tema deve ter no máximo 100 caracteres.");
        }
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
