package lk.ac.kln.property_unit_service.exception;

public class UnitNotFoundException extends RuntimeException {
    public UnitNotFoundException(Long id) {
        super("Unit with ID " + id + " not found");
    }
}
