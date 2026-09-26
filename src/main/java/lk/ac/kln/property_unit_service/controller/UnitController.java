package lk.ac.kln.property_unit_service.controller;

import lk.ac.kln.property_unit_service.dto.UnitStatusUpdateRequest;
import lk.ac.kln.property_unit_service.exception.InvalidStatusTransitionException;
import lk.ac.kln.property_unit_service.model.Unit;
import lk.ac.kln.property_unit_service.service.UnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Unit> updateUnitStatus(
            @PathVariable Long id,
            @RequestBody UnitStatusUpdateRequest request) {
        
        Unit updatedUnit = unitService.updateUnitStatus(id, request.getNewStatus());
        return ResponseEntity.ok(updatedUnit);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
