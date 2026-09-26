package lk.ac.kln.property_unit_service.dto;

import java.util.UUID;

/** Shared Group 2 fields consumed by lease-occupancy-service. */
public final class UnitContract {
    private UnitContract() { }

    public record Response<T>(boolean success, T data) { }
    public record Details(UUID unitId, String status, int capacityLimit, UUID ownerId) { }
    public record Capacity(UUID unitId, int capacityLimit) { }
    public record StatusRequest(String status) { }
}
