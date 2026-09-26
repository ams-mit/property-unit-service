package lk.ac.kln.property_unit_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lk.ac.kln.property_unit_service.model.Floor;
import lk.ac.kln.property_unit_service.model.Unit;
import lk.ac.kln.property_unit_service.model.UnitType;
import lk.ac.kln.property_unit_service.repository.FloorRepository;
import lk.ac.kln.property_unit_service.repository.UnitRepository;
import lk.ac.kln.property_unit_service.repository.UnitTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/units")
public class UnitController {
    private final UnitRepository units;
    private final FloorRepository floors;
    private final UnitTypeRepository types;

    public UnitController(UnitRepository units, FloorRepository floors, UnitTypeRepository types) {
        this.units = units;
        this.floors = floors;
        this.types = types;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PROPERTY_MANAGER', 'ROLE_MANAGER')")
    public UnitView create(@Valid @RequestBody UnitCreateRequest request) {
        Floor floor = floors.findById(request.floorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Floor not found"));
        UnitType type = types.findById(request.unitTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit type not found"));
        Unit unit = new Unit();
        unit.setPublicId(UUID.randomUUID());
        unit.setFloor(floor);
        unit.setUnitType(type);
        unit.setUnitNumber(request.unitNumber());
        return UnitView.from(units.save(unit));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PROPERTY_MANAGER', 'ROLE_MANAGER')")
    public List<UnitView> list() {
        return units.findAll().stream().map(UnitView::from).toList();
    }

    public record UnitCreateRequest(@NotNull Long floorId, @NotNull Long unitTypeId,
                                    @NotBlank String unitNumber) { }

    public record UnitView(UUID unitId, Long floorId, Long unitTypeId, String unitNumber, String status) {
        static UnitView from(Unit unit) {
            return new UnitView(unit.getPublicId(), unit.getFloor().getId(), unit.getUnitType().getId(),
                    unit.getUnitNumber(), unit.getStatus());
        }
    }
}
