package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga inicial de categorias (dev).
 *
 * <p><b>STUB do RF13.</b> Popula algumas categorias para que o formulario do
 * RF06 tenha opcoes selecionaveis enquanto o gerenciamento de categorias
 * (RF13) nao e implementado.
 */
@Component
public class CargaInicialCategorias implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;

    public CargaInicialCategorias(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }
        List<String> nomes = List.of(
                "Moodle",
                "Programacao",
                "Banco de Dados",
                "Engenharia de Software",
                "Redes",
                "Geral");
        nomes.forEach(nome -> categoriaRepository.save(new Categoria(nome)));
    }
}
