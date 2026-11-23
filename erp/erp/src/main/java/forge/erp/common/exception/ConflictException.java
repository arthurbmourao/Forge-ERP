package forge.erp.common.exception;

/**
 * Conflito com o estado atual dos dados (ex.: registro duplicado). Mapeado para HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
