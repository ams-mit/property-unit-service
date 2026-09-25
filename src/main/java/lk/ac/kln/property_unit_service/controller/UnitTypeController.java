package lk.ac.kln.property_unit_service.controller;

import jakarta.validation.Valid;
import lk.ac.kln.property_unit_service.model.UnitType;
import lk.ac.kln.property_unit_service.repository.UnitTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/unit-types")
public class UnitTypeController {

    private final UnitTypeRepository unitTypeRepository;

    @Autowired
    public UnitTypeController(UnitTypeRepository unitTypeRepository) {
        this.unitTypeRepository = unitTypeRepository;
    }

    @PostMapping
    public ResponseEntity<UnitType> createUnitType(@Valid @RequestBody UnitType unitType) {
        UnitType savedUnitType = unitTypeRepository.save(unitType);
        return new ResponseEntity<>(savedUnitType, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UnitType>> getAllUnitTypes() {
        List<UnitType> unitTypes = unitTypeRepository.findAll();
        return new ResponseEntity<>(unitTypes, HttpStatus.OK);
    }
}
