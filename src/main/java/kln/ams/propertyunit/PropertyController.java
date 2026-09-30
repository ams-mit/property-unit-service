package kln.ams.propertyunit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name="gatewayBearer")
@ApiResponses({@ApiResponse(responseCode="400",description="VALIDATION_ERROR"),@ApiResponse(responseCode="401",description="UNAUTHORIZED"),
    @ApiResponse(responseCode="403",description="FORBIDDEN"),@ApiResponse(responseCode="404",description="Resource not found"),
    @ApiResponse(responseCode="409",description="Property conflict"),@ApiResponse(responseCode="503",description="DEPENDENCY_UNAVAILABLE"),
    @ApiResponse(responseCode="500",description="INTERNAL_SERVER_ERROR")})
class PropertyController {
    private final PropertyService service;
    PropertyController(PropertyService service) {this.service=service;}

    @PostMapping("/api/v1/buildings") @Operation(operationId="PROP-001",summary="Create building and its floors",description="SYSTEM_ADMINISTRATOR or APARTMENT_MANAGER. Building code and floor numbers must be unique; status is assigned by the service.")
    @ApiResponse(responseCode="201",description="Building created") @ApiResponse(responseCode="409",description="BUILDING_ALREADY_EXISTS")
    ResponseEntity<kln.ams.propertyunit.ApiResponse<BuildingView>> building(@Valid @RequestBody BuildingInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kln.ams.propertyunit.ApiResponse.of("Building created successfully",service.create(input),null));
    }
    @GetMapping("/api/v1/buildings") @Operation(operationId="PROP-002",summary="List buildings and floors",description="SYSTEM_ADMINISTRATOR, APARTMENT_MANAGER, OWNER or TENANT_RESIDENT. Supports page, size, search and status.")
    kln.ams.propertyunit.ApiResponse<java.util.List<BuildingView>> buildings(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String search,@RequestParam(required=false) RecordStatus status) {
        Page<BuildingView> data=service.buildings(page,size,search,status);return kln.ams.propertyunit.ApiResponse.of("Buildings retrieved successfully",data.getContent(),PropertyService.pagination(data));
    }
    @PostMapping("/api/v1/unit-types") @Operation(operationId="PROP-003",summary="Create unit type",description="SYSTEM_ADMINISTRATOR or APARTMENT_MANAGER. Code is unique and capacity positive.")
    ResponseEntity<kln.ams.propertyunit.ApiResponse<UnitTypeView>> type(@Valid @RequestBody UnitTypeInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kln.ams.propertyunit.ApiResponse.of("Unit type created successfully",service.create(input),null));
    }
    @GetMapping("/api/v1/unit-types") @Operation(operationId="PROP-004",summary="List unit types",description="Any authenticated Project A user. Supports page, size and status.")
    kln.ams.propertyunit.ApiResponse<java.util.List<UnitTypeView>> types(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) RecordStatus status) {
        Page<UnitTypeView> data=service.types(page,size,status);return kln.ams.propertyunit.ApiResponse.of("Unit types retrieved successfully",data.getContent(),PropertyService.pagination(data));
    }
    @PostMapping("/api/v1/units") @Operation(operationId="PROP-005",summary="Create unit",description="SYSTEM_ADMINISTRATOR or APARTMENT_MANAGER. Building, floor and active type must exist; floor must belong to building; number unique within building.")
    ResponseEntity<kln.ams.propertyunit.ApiResponse<UnitView>> unit(@Valid @RequestBody UnitInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kln.ams.propertyunit.ApiResponse.of("Unit created successfully",service.create(input),null));
    }
    @GetMapping("/api/v1/units") @Operation(operationId="PROP-006",summary="Search units",description="Authenticated users. Management sees inventory; resident views require authoritative relationship validation. Supports page, size, buildingId, floorId, unitTypeId, status, availability, unitNumber.")
    kln.ams.propertyunit.ApiResponse<java.util.List<UnitView>> units(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(required=false) UUID buildingId,@RequestParam(required=false) UUID floorId,@RequestParam(required=false) UUID unitTypeId,
        @RequestParam(required=false) UnitStatus status,@RequestParam(required=false) Boolean availability,@RequestParam(required=false) String unitNumber) {
        Page<UnitView> data=service.units(page,size,buildingId,floorId,unitTypeId,status,availability,unitNumber);
        return kln.ams.propertyunit.ApiResponse.of("Units retrieved successfully",data.getContent(),PropertyService.pagination(data));
    }
    @PostMapping("/api/v1/ownerships") @Operation(operationId="PROP-007",summary="Create ownership",description="SYSTEM_ADMINISTRATOR or APARTMENT_MANAGER. Validates owner through RES-INT-001 via Gateway; unavailable provider returns 503 DEPENDENCY_UNAVAILABLE. Percentage must be positive and overlapping total at most 100.")
    ResponseEntity<kln.ams.propertyunit.ApiResponse<OwnershipView>> ownership(@Valid @RequestBody OwnershipInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kln.ams.propertyunit.ApiResponse.of("Ownership created successfully",service.create(input),null));
    }
    @GetMapping("/api/v1/ownerships") @Operation(operationId="PROP-008",summary="Search ownerships",description="Authenticated users. Management can search by unitId, ownerId, status; resident views require authoritative relationship validation.")
    kln.ams.propertyunit.ApiResponse<java.util.List<OwnershipView>> ownerships(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(required=false) UUID unitId,@RequestParam(required=false) UUID ownerId,@RequestParam(required=false) RecordStatus status) {
        Page<OwnershipView> data=service.ownerships(page,size,unitId,ownerId,status);
        return kln.ams.propertyunit.ApiResponse.of("Ownerships retrieved successfully",data.getContent(),PropertyService.pagination(data));
    }
}

@RestController
@RequestMapping("/api/v1/internal/units")
@SecurityRequirement(name="gatewayBearer")
@ApiResponses({@ApiResponse(responseCode="400",description="VALIDATION_ERROR"),@ApiResponse(responseCode="401",description="UNAUTHORIZED"),
    @ApiResponse(responseCode="403",description="FORBIDDEN"),@ApiResponse(responseCode="404",description="UNIT_NOT_FOUND"),
    @ApiResponse(responseCode="503",description="DEPENDENCY_UNAVAILABLE"),@ApiResponse(responseCode="500",description="INTERNAL_SERVER_ERROR")})
class InternalUnitController {
    private final PropertyService service;
    InternalUnitController(PropertyService service) {this.service=service;}
    @GetMapping("/{unitId}/exists") @Operation(operationId="PROP-INT-001",summary="Check unit existence",description="Allowed: Resident, Lease, Billing, Utility, Operations and Community services. A missing unit returns exists=false.")
    kln.ams.propertyunit.ApiResponse<UnitExists> exists(@PathVariable UUID unitId) {
        return kln.ams.propertyunit.ApiResponse.of("Unit existence validated",new UnitExists(unitId,service.exists(unitId)),null);
    }
    @GetMapping("/{unitId}/validate") @Operation(operationId="PROP-INT-002",summary="Validate unit property state",description="Allowed: Resident, Lease, Billing, Utility, Operations and Community services. Returns the unit type capacity for lease validation. Missing unit returns 404 UNIT_NOT_FOUND.")
    kln.ams.propertyunit.ApiResponse<UnitValidation> validate(@PathVariable UUID unitId) {
        return kln.ams.propertyunit.ApiResponse.of("Unit validated",service.validation(unitId),null);
    }
    @GetMapping("/{unitId}/ownership") @Operation(operationId="PROP-INT-003",summary="Get current unit ownership",description="Allowed: Resident, Lease, Billing, Operations and Community services. Owner profiles remain in Resident Management.")
    kln.ams.propertyunit.ApiResponse<UnitOwnership> ownership(@PathVariable UUID unitId) {
        return kln.ams.propertyunit.ApiResponse.of("Unit ownership retrieved",service.ownership(unitId),null);
    }
    @GetMapping("/{unitId}/status") @Operation(operationId="PROP-INT-004",summary="Get unit status",description="Allowed: Resident, Lease, Billing, Utility, Operations and Community services. Does not expose occupancy history.")
    kln.ams.propertyunit.ApiResponse<UnitStatusView> status(@PathVariable UUID unitId) {
        return kln.ams.propertyunit.ApiResponse.of("Unit status retrieved",service.status(unitId),null);
    }
}
