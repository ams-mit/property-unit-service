package lk.ac.kln.property_unit_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.kln.property_unit_service.model.Unit;
import lk.ac.kln.property_unit_service.model.UnitType;
import lk.ac.kln.property_unit_service.repository.OwnershipRepository;
import lk.ac.kln.property_unit_service.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {
    @Mock UnitRepository units;
    @Mock OwnershipRepository ownerships;
    @InjectMocks UnitService service;

    UUID unitId;
    Unit unit;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();
        UnitType type = new UnitType();
        type.setCapacityLimit(2);
        unit = new Unit();
        unit.setPublicId(unitId);
        unit.setUnitType(type);
    }

    @Test
    void capacity_usesPublicUuidAndUnitTypeCapacity() {
        when(units.findByPublicId(unitId)).thenReturn(Optional.of(unit));

        assertThat(service.capacity(unitId).capacityLimit()).isEqualTo(2);
    }

    @Test
    void changeStatus_rejectsMaintenanceUnit() {
        unit.setStatus("UNDER_MAINTENANCE");
        when(units.findByPublicId(unitId)).thenReturn(Optional.of(unit));

        assertThatThrownBy(() -> service.changeStatus(unitId, "OCCUPIED"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void changeStatus_acceptsOccupiedTransition() {
        when(units.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(ownerships.findByUnitId(unit.getId())).thenReturn(List.of());

        assertThat(service.changeStatus(unitId, "OCCUPIED").status()).isEqualTo("OCCUPIED");
        verify(units).save(unit);
    }
}
