package br.edu.dombosco.ptdb.faq;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemFaqRepository extends JpaRepository<ItemFaq, Long> {

    List<ItemFaq> findAllByOrderByCategoriaTemaAscIdAsc();
}
