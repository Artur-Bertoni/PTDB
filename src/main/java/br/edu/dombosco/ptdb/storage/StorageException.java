package br.edu.dombosco.ptdb.storage;

/**
 * Erro relacionado ao armazenamento de arquivos de upload (RF06).
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
