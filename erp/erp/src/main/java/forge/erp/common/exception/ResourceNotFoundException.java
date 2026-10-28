package forge.erp.common.exception;

import java.util.UUID;

/**
 * Recurso solicitado nao existe. Mapeado para HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, UUID id) {
        return new ResourceNotFoundException("%s não encontrado(a): %s".formatted(resource, id));
    }
}
