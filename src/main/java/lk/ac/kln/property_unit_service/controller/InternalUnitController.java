package lk.ac.kln.property_unit_service.controller;

import java.util.UUID;
import lk.ac.kln.property_unit_service.dto.UnitContract;
import lk.ac.kln.property_unit_service.service.UnitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/units")
public class InternalUnitController {
    private final UnitService service;

    public InternalUnitController(UnitService service) {
        this.service = service;
    }

    @GetMapping("/{unitId}")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_lease-occupancy-service')")
    public UnitContract.Response<UnitContract.Details> details(@PathVariable UUID unitId) {
        return new UnitContract.Response<>(true, service.details(unitId));
    }

    @GetMapping("/{unitId}/capacity")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_lease-occupancy-service')")
    public UnitContract.Response<UnitContract.Capacity> capacity(@PathVariable UUID unitId) {
        return new UnitContract.Response<>(true, service.capacity(unitId));
    }

    @PatchMapping("/{unitId}/status")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_lease-occupancy-service')")
    public UnitContract.Response<UnitContract.Details> status(@PathVariable UUID unitId,
            @RequestBody UnitContract.StatusRequest request) {
        return new UnitContract.Response<>(true, service.changeStatus(unitId, request.status()));
    }
}
