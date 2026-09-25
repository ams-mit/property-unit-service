package lk.ac.kln.property_unit_service.exception;

public class ShareLimitExceededException extends RuntimeException {
    public ShareLimitExceededException(String message) {
        super(message);
    }
}
