package br.edu.dombosco.ptdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicacao PTDB (Portal de Treinamento Dom Bosco).
 *
 * <p>Escopo atual: RF06 - Manter videos (CRUD do Administrador).
 */
@SpringBootApplication
public class PtdbApplication {

    public static void main(String[] args) {
        SpringApplication.run(PtdbApplication.class, args);
    }
}
