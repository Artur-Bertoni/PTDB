package br.edu.dombosco.ptdb.categoria;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByAtivaTrueOrderByNomeAsc();

    Categoria findByIdAndAtivaTrue(Long id);

    Categoria findByNomeIgnoreCase(String nome);

    boolean existsByIdAndAtivaTrue(Long id);

    long countByAtivaTrue();
}
