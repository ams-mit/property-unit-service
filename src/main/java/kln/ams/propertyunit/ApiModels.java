package kln.ams.propertyunit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

final class ApiModels { private ApiModels() {} }
record FloorInput(@NotNull Integer floorNumber,@Size(max=50) String name,@Size(max=500) String description) {}
record BuildingInput(@NotBlank @Size(max=50) String buildingCode,@NotBlank @Size(max=255) String name,
                     @NotBlank @Size(max=255) String address,@Size(max=500) String description,
                     List<@Valid FloorInput> floors) {}
record UnitTypeInput(@NotBlank @Size(max=50) String code,@NotBlank @Size(max=255) String name,
                     @Size(max=500) String description,@NotNull @Positive Integer capacity) {}
record UnitInput(@NotBlank @Size(max=50) String unitNumber,@NotNull UUID buildingId,@NotNull UUID floorId,@NotNull UUID unitTypeId) {}
record OwnershipInput(@NotNull UUID unitId,@NotNull UUID ownerId,@NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal ownershipPercentage,
                      @NotNull LocalDate startDate,LocalDate endDate) {}
record FloorView(UUID id,UUID buildingId,Integer floorNumber,String name,String description,RecordStatus status) {}
record BuildingView(UUID id,String buildingCode,String name,String address,String description,RecordStatus status,List<FloorView> floors,Instant createdAt,Instant updatedAt) {}
record UnitTypeView(UUID id,String code,String name,String description,Integer capacity,RecordStatus status,Instant createdAt,Instant updatedAt) {}
record UnitView(UUID id,String unitNumber,UUID buildingId,UUID floorId,UUID unitTypeId,UnitStatus status,boolean availability,Instant createdAt,Instant updatedAt) {}
record OwnershipView(UUID id,UUID unitId,UUID ownerId,BigDecimal ownershipPercentage,LocalDate startDate,LocalDate endDate,RecordStatus status,Instant createdAt,Instant updatedAt) {}
record Pagination(int page,int size,long totalElements,int totalPages,boolean hasNext,boolean hasPrevious) {}
record ApiResponse<T>(boolean success,String message,T data,Pagination pagination,Instant timestamp,String requestId) {
    static <T> ApiResponse<T> of(String message,T data,Pagination pagination) { return new ApiResponse<>(true,message,data,pagination,Instant.now(),RequestIds.current()); }
}
record ErrorBody(String code,Object details) {}
record ApiError(boolean success,String message,ErrorBody error,Instant timestamp,String requestId) {
    static ApiError of(String message,String code,Object details) { return new ApiError(false,message,new ErrorBody(code,details),Instant.now(),RequestIds.current()); }
}
record UnitExists(UUID unitId,boolean exists) {}
record UnitValidation(UUID unitId,boolean exists,UnitStatus status,boolean availability,UUID buildingId,UUID floorId,UUID unitTypeId,Integer capacity) {}
record UnitStatusView(UUID unitId,UnitStatus status,boolean availability) {}
record UnitOwnership(UUID unitId,List<OwnershipView> owners) {}
