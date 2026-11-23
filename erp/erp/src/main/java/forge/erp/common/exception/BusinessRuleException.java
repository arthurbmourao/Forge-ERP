package forge.erp.common.exception;

/**
 * Violacao de regra de negocio (ex.: entidade inativa, unidades incompativeis).
 * Mapeado para HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
