package lk.ac.kln.property_unit_service.service;

import lk.ac.kln.property_unit_service.exception.InvalidStatusTransitionException;
import lk.ac.kln.property_unit_service.model.Unit;
import lk.ac.kln.property_unit_service.model.enums.UnitStatus;
import lk.ac.kln.property_unit_service.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnitService {

    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @Transactional
    public Unit updateUnitStatus(Long unitId, UnitStatus newStatus) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new lk.ac.kln.property_unit_service.exception.UnitNotFoundException(unitId));

        UnitStatus currentStatus = unit.getStatus();

        // Enforce strict state machine for Unit status
        boolean isValid = switch (currentStatus) {
            case AVAILABLE -> newStatus == UnitStatus.RESERVED;
            case RESERVED -> newStatus == UnitStatus.OCCUPIED;
            case OCCUPIED -> newStatus == UnitStatus.UNDER_MAINTENANCE || newStatus == UnitStatus.INACTIVE;
            case UNDER_MAINTENANCE -> newStatus == UnitStatus.AVAILABLE;
            case INACTIVE -> false; // No permitted transitions from INACTIVE
        };

        if (!isValid) {
            throw new InvalidStatusTransitionException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }

        unit.setStatus(newStatus);
        return unitRepository.save(unit);
    }
}
