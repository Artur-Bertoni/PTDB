package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.faq.ItemFaq;
import br.edu.dombosco.ptdb.faq.ItemFaqRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialFaq implements CommandLineRunner {

    private static final String VIDEOS = "Vídeos e favoritos";
    private static final String AVALIACAO = "Avaliação dos vídeos";
    private static final String CATEGORIAS = "Categorias";

    private final ItemFaqRepository repository;

    public CargaInicialFaq(ItemFaqRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new ItemFaq("Como faço para favoritar um vídeo?",
                "Clique no ícone de coração no canto do card do vídeo ou na página do vídeo. "
                        + "Os favoritos ficam reunidos em \"Meus favoritos\", no menu lateral.", VIDEOS));
        repository.save(new ItemFaq("Por que um vídeo que eu assisti não aparece mais no catálogo?",
                "O vídeo pode ter sido removido ou estar passando por uma nova avaliação depois de "
                        + "ser editado. Ele volta ao catálogo quando for aprovado novamente.", VIDEOS));
        repository.save(new ItemFaq("Quando um vídeo cadastrado aparece no catálogo?",
                "Todo vídeo cadastrado passa pela avaliação. Ele só aparece no catálogo depois de "
                        + "aprovado pelo avaliador.", AVALIACAO));
        repository.save(new ItemFaq("O que acontece quando o avaliador solicita ajustes?",
                "O vídeo sai da fila de avaliação até que o administrador faça as correções indicadas "
                        + "no parecer. Ao salvar a edição, o vídeo volta para a fila.", AVALIACAO));
        repository.save(new ItemFaq("Como os vídeos são organizados por categoria?",
                "Cada vídeo pertence a uma categoria principal. No catálogo, escolha a categoria na "
                        + "lista lateral para ver somente os vídeos dela.", CATEGORIAS));
    }
}
