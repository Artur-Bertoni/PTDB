package br.edu.dombosco.ptdb.categoria;

import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.video.VideoRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final VideoRepository videoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, VideoRepository videoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.videoRepository = videoRepository;
    }

    public List<Categoria> listarAtivas() {
        return categoriaRepository.findByAtivaTrueOrderByNomeAsc();
    }

    public Categoria buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findByIdAndAtivaTrue(id);
        if (categoria == null) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada (id=" + id + ").");
        }
        return categoria;
    }

    public Map<String, String> validar(CategoriaForm form) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (form.getNome() == null || form.getNome().isBlank()) {
            erros.put("nome", "O nome é obrigatório.");
            return erros;
        }
        if (form.getNome().length() > 100) {
            erros.put("nome", "O nome deve ter no máximo 100 caracteres.");
            return erros;
        }
        if (form.getDescricao() != null && form.getDescricao().length() > 255) {
            erros.put("descricao", "A descrição deve ter no máximo 255 caracteres.");
        }
        Categoria mesmoNome = categoriaRepository.findByNomeIgnoreCase(form.getNome().trim());
        if (mesmoNome != null && mesmoNome.isAtiva() && !mesmoNome.getId().equals(form.getId())) {
            erros.put("nome", "Já existe uma categoria com esse nome.");
        }
        return erros;
    }

    public Categoria criar(CategoriaForm form) {
        Categoria categoria = categoriaRepository.findByNomeIgnoreCase(form.getNome().trim());
        if (categoria == null) {
            categoria = new Categoria();
        } else if (categoria.isAtiva()) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        }
        categoria.setAtiva(true);
        preencher(categoria, form);
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Long id, CategoriaForm form) {
        Categoria categoria = buscarPorId(id);
        Categoria mesmoNome = categoriaRepository.findByNomeIgnoreCase(form.getNome().trim());
        if (mesmoNome != null && !mesmoNome.getId().equals(id)) {
            if (mesmoNome.isAtiva()) {
                throw new RegraNegocioException("Já existe uma categoria com esse nome.");
            }
            throw new RegraNegocioException("Esse nome pertence a uma categoria excluída. Cadastre-a novamente para reativá-la.");
        }
        preencher(categoria, form);
        return categoriaRepository.save(categoria);
    }

    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        if (videoRepository.existsByCategoriaIdAndAtivoTrue(id)) {
            throw new RegraNegocioException("A categoria \"" + categoria.getNome()
                    + "\" possui vídeos. Mova ou exclua os vídeos antes de excluí-la.");
        }
        categoria.setAtiva(false);
        categoriaRepository.save(categoria);
    }

    private void preencher(Categoria categoria, CategoriaForm form) {
        categoria.setNome(form.getNome().trim());
        String descricao = form.getDescricao();
        if (descricao == null || descricao.isBlank()) {
            categoria.setDescricao(null);
        } else {
            categoria.setDescricao(descricao.trim());
        }
    }
}
