package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.categoria.Categoria;
import br.edu.dombosco.ptdb.categoria.CategoriaRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
        categoriaRepository.saveAll(List.of(
                new Categoria("Moodle", "Uso do ambiente virtual de aprendizagem."),
                new Categoria("Programação", "Lógica, linguagens e práticas de desenvolvimento."),
                new Categoria("Banco de Dados", "Modelagem, SQL e administração de bancos de dados."),
                new Categoria("Engenharia de Software", "Requisitos, processos, modelagem e qualidade de software."),
                new Categoria("Redes", "Fundamentos e configuração de redes de computadores."),
                new Categoria("Geral", "Conteúdos gerais e institucionais do portal.")));
    }
}
