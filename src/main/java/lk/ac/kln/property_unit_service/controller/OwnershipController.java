package lk.ac.kln.property_unit_service.controller;

import lk.ac.kln.property_unit_service.model.Ownership;
import lk.ac.kln.property_unit_service.service.OwnershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ownerships")
public class OwnershipController {

    private final OwnershipService ownershipService;

    @Autowired
    public OwnershipController(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Ownership> createOwnership(@RequestBody Ownership ownership) {
        Ownership created = ownershipService.createOwnership(ownership);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/units/{unitId}")
    public ResponseEntity<List<Ownership>> getOwnershipsByUnitId(@PathVariable Long unitId) {
        List<Ownership> ownerships = ownershipService.getOwnershipsByUnitId(unitId);
        return ResponseEntity.ok(ownerships);
    }

    @GetMapping("/owners/{ownerId}")
    public ResponseEntity<List<Ownership>> getOwnershipsByOwnerId(@PathVariable String ownerId) {
        List<Ownership> ownerships = ownershipService.getOwnershipsByOwnerId(ownerId);
        return ResponseEntity.ok(ownerships);
    }
}
