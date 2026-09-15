package br.edu.dombosco.ptdb.common;

/**
 * Lancada quando uma regra de negocio e violada (ex.: campo obrigatorio
 * ausente, arquivo invalido).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
