package br.edu.dombosco.ptdb.common;

/**
 * Lancada quando um recurso solicitado nao existe ou esta inativo.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
