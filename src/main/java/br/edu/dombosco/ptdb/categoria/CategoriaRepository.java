package br.edu.dombosco.ptdb.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio da entidade {@link Categoria} (stub RF13/RF10).
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}
