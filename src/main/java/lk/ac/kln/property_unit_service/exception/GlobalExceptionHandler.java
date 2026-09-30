package lk.ac.kln.property_unit_service.exception;

import lk.ac.kln.property_unit_service.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnitNotFoundException(UnitNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("UNIT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
