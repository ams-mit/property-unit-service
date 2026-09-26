package lk.ac.kln.property_unit_service.service;

import java.time.LocalDate;
import java.util.UUID;
import lk.ac.kln.property_unit_service.dto.UnitContract;
import lk.ac.kln.property_unit_service.model.Ownership;
import lk.ac.kln.property_unit_service.model.Unit;
import lk.ac.kln.property_unit_service.repository.OwnershipRepository;
import lk.ac.kln.property_unit_service.repository.UnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UnitService {
    private final UnitRepository units;
    private final OwnershipRepository ownerships;

    public UnitService(UnitRepository units, OwnershipRepository ownerships) {
        this.units = units;
        this.ownerships = ownerships;
    }

    @Transactional(readOnly = true)
    public UnitContract.Details details(UUID unitId) {
        Unit unit = find(unitId);
        UUID ownerId = ownerships.findByUnitId(unit.getId()).stream()
                .filter(o -> !o.getStartDate().isAfter(LocalDate.now())
                        && (o.getEndDate() == null || !o.getEndDate().isBefore(LocalDate.now())))
                .map(Ownership::getOwnerId)
                .map(this::parseUuid)
                .filter(id -> id != null)
                .findFirst().orElse(null);
        return new UnitContract.Details(unitId, unit.getStatus(), unit.getUnitType().getCapacityLimit(), ownerId);
    }

    @Transactional(readOnly = true)
    public UnitContract.Capacity capacity(UUID unitId) {
        Unit unit = find(unitId);
        return new UnitContract.Capacity(unitId, unit.getUnitType().getCapacityLimit());
    }

    @Transactional
    public UnitContract.Details changeStatus(UUID unitId, String status) {
        if (!"OCCUPIED".equals(status) && !"AVAILABLE".equals(status)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Lease service may only set OCCUPIED or AVAILABLE");
        }
        Unit unit = find(unitId);
        if ("UNDER_MAINTENANCE".equals(unit.getStatus()) || "INACTIVE".equals(unit.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unit is unavailable");
        }
        if ("OCCUPIED".equals(status) && unit.getUnitType().getCapacityLimit() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unit has no occupancy capacity");
        }
        unit.setStatus(status);
        units.save(unit);
        return details(unitId);
    }

    private Unit find(UUID unitId) {
        return units.findByPublicId(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));
    }

    private UUID parseUuid(String value) {
        try { return UUID.fromString(value); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
