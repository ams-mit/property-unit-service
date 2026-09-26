package lk.ac.kln.property_unit_service.dto;

import lk.ac.kln.property_unit_service.model.enums.UnitStatus;
import lombok.Data;

@Data
public class UnitStatusUpdateRequest {
    private UnitStatus newStatus;
}
