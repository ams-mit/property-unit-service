package lk.ac.kln.property_unit_service.exception;

public class ExternalValidationException extends RuntimeException {
    public ExternalValidationException(String message) {
        super(message);
    }

    public ExternalValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
