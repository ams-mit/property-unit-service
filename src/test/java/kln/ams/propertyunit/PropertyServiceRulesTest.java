package kln.ams.propertyunit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class PropertyServiceRulesTest {
    private final Buildings buildings=mock(Buildings.class); private final Floors floors=mock(Floors.class);
    private final UnitTypes types=mock(UnitTypes.class); private final Units units=mock(Units.class);
    private final Ownerships ownerships=mock(Ownerships.class); private final ResidentClient residents=mock(ResidentClient.class);
    private final PropertyService service=new PropertyService(buildings,floors,types,units,ownerships,residents);
    @AfterEach void clearSecurity(){SecurityContextHolder.clearContext();}

    @Test void duplicateBuildingCodeIsRejectedBeforeWrite() {
        when(buildings.existsByBuildingCodeIgnoreCase("A")).thenReturn(true);
        ApiException error=assertThrows(ApiException.class,()->service.create(new BuildingInput("A","Building A","Address",null,null)));
        assertEquals("BUILDING_ALREADY_EXISTS",error.code);verify(buildings,never()).saveAndFlush(any());
    }
    @Test void floorMustBelongToSelectedBuilding() {
        Building selected=new Building();selected.id=1L;selected.publicId=UUID.randomUUID();
        Building other=new Building();other.id=2L;
        Floor floor=new Floor();floor.building=other;floor.publicId=UUID.randomUUID();
        UnitType type=new UnitType();type.publicId=UUID.randomUUID();
        when(buildings.lockByPublicId(selected.publicId)).thenReturn(Optional.of(selected));
        when(floors.findByPublicId(floor.publicId)).thenReturn(Optional.of(floor));
        when(types.findByPublicId(type.publicId)).thenReturn(Optional.of(type));
        ApiException error=assertThrows(ApiException.class,()->service.create(new UnitInput("101",selected.publicId,floor.publicId,type.publicId)));
        assertEquals("INVALID_FLOOR_BUILDING_RELATIONSHIP",error.code);verify(units,never()).saveAndFlush(any());
    }
    @Test void dependencyFailureNeverWritesOwnership() {
        Unit unit=activeUnit();UUID owner=UUID.randomUUID();
        when(units.lockByPublicId(unit.publicId)).thenReturn(Optional.of(unit));
        doThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"DEPENDENCY_UNAVAILABLE","Unavailable")).when(residents).validateOwner(owner);
        ApiException error=assertThrows(ApiException.class,()->service.create(new OwnershipInput(unit.publicId,owner,BigDecimal.valueOf(100),LocalDate.now(),null)));
        assertEquals("DEPENDENCY_UNAVAILABLE",error.code);verify(ownerships,never()).saveAndFlush(any());
    }
    @Test void overlappingSharesCannotExceedOneHundred() {
        Unit unit=activeUnit();UUID owner=UUID.randomUUID();
        when(units.lockByPublicId(unit.publicId)).thenReturn(Optional.of(unit));
        Ownership existing=new Ownership();existing.ownerId=UUID.randomUUID();existing.status=RecordStatus.ACTIVE;
        existing.startDate=LocalDate.now().minusDays(10);existing.ownershipPercentage=BigDecimal.valueOf(80);
        when(ownerships.findByUnitId(unit.id)).thenReturn(List.of(existing));
        ApiException error=assertThrows(ApiException.class,()->service.create(new OwnershipInput(unit.publicId,owner,BigDecimal.valueOf(30),LocalDate.now(),null)));
        assertEquals("OWNERSHIP_CONFLICT",error.code);verify(ownerships,never()).saveAndFlush(any());
    }
    @Test void ownerWithoutVerifiedScopeCannotSearchInventory() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(),null,List.of(new SimpleGrantedAuthority("ROLE_OWNER"))));
        ApiException error=assertThrows(ApiException.class,()->service.units(0,20,null,null,null,null,null,null));
        assertEquals(HttpStatus.FORBIDDEN,error.status);verifyNoInteractions(units);
    }
    @Test void validationReadsCapacityThroughLazyUnitTypeProxy() {
        Unit unit=activeUnit();unit.floor.publicId=UUID.randomUUID();
        // A lazy Hibernate proxy leaves its fields null and only loads data through getters.
        UnitType proxy=mock(UnitType.class);UUID typeId=UUID.randomUUID();
        when(proxy.getCapacity()).thenReturn(3);when(proxy.getPublicId()).thenReturn(typeId);
        unit.unitType=proxy;
        when(units.findByPublicId(unit.publicId)).thenReturn(Optional.of(unit));
        UnitValidation validation=service.validation(unit.publicId);
        assertEquals(3,validation.capacity());assertEquals(typeId,validation.unitTypeId());
    }
    private Unit activeUnit() {
        Building building=new Building();Floor floor=new Floor();floor.building=building;
        Unit unit=new Unit();unit.id=1L;unit.publicId=UUID.randomUUID();unit.floor=floor;return unit;
    }
}
