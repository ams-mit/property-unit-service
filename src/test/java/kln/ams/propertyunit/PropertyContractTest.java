package kln.ams.propertyunit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PropertyContractTest {
    private final PropertyService service=mock(PropertyService.class);
    private final MockMvc mvc=MockMvcBuilders.standaloneSetup(new PropertyController(service),new InternalUnitController(service)).setControllerAdvice(new ApiErrors()).build();

    @Test void allTwelveCanonicalRoutesReturnStandardEnvelope() throws Exception {
        UUID id=UUID.randomUUID();
        when(service.buildings(anyInt(),anyInt(),any(),any())).thenReturn(new PageImpl<>(List.of()));
        when(service.types(anyInt(),anyInt(),any())).thenReturn(new PageImpl<>(List.of()));
        when(service.units(anyInt(),anyInt(),any(),any(),any(),any(),any(),any())).thenReturn(new PageImpl<>(List.of()));
        when(service.ownerships(anyInt(),anyInt(),any(),any(),any())).thenReturn(new PageImpl<>(List.of()));
        when(service.create(any(BuildingInput.class))).thenReturn(new BuildingView(id,"B","Building","Address",null,RecordStatus.ACTIVE,List.of(),null,null));
        when(service.create(any(UnitTypeInput.class))).thenReturn(new UnitTypeView(id,"T","Type",null,2,RecordStatus.ACTIVE,null,null));
        when(service.create(any(UnitInput.class))).thenReturn(new UnitView(id,"101",id,id,id,UnitStatus.AVAILABLE,true,null,null));
        when(service.create(any(OwnershipInput.class))).thenReturn(new OwnershipView(id,id,id,BigDecimal.valueOf(100),LocalDate.now(),null,RecordStatus.ACTIVE,null,null));
        when(service.exists(id)).thenReturn(true);
        when(service.validation(id)).thenReturn(new UnitValidation(id,true,UnitStatus.AVAILABLE,true,id,id,id,2));
        when(service.ownership(id)).thenReturn(new UnitOwnership(id,List.of()));
        when(service.status(id)).thenReturn(new UnitStatusView(id,UnitStatus.AVAILABLE,true));
        String[][] calls={{"POST","/api/v1/buildings","{\"buildingCode\":\"B\",\"name\":\"Building\",\"address\":\"Address\"}"},
            {"GET","/api/v1/buildings",null},{"POST","/api/v1/unit-types","{\"code\":\"T\",\"name\":\"Type\",\"capacity\":2}"},
            {"GET","/api/v1/unit-types",null},{"POST","/api/v1/units","{\"unitNumber\":\"101\",\"buildingId\":\""+id+"\",\"floorId\":\""+id+"\",\"unitTypeId\":\""+id+"\"}"},
            {"GET","/api/v1/units",null},{"POST","/api/v1/ownerships","{\"unitId\":\""+id+"\",\"ownerId\":\""+id+"\",\"ownershipPercentage\":100,\"startDate\":\"2026-09-01\"}"},
            {"GET","/api/v1/ownerships",null},{"GET","/api/v1/internal/units/"+id+"/exists",null},
            {"GET","/api/v1/internal/units/"+id+"/validate",null},{"GET","/api/v1/internal/units/"+id+"/ownership",null},{"GET","/api/v1/internal/units/"+id+"/status",null}};
        for(String[] call:calls) {
            var request=call[0].equals("POST")?post(call[1]).contentType("application/json").content(call[2]):get(call[1]);
            mvc.perform(request).andExpect(status().is(call[0].equals("POST")?201:200)).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.requestId").isNotEmpty()).andExpect(jsonPath("$.timestamp").isNotEmpty());
        }
        mvc.perform(get("/api/v1/internal/units/"+id+"/validate"))
            .andExpect(jsonPath("$.data.capacity").value(2));
    }
    @Test void validationAndDomainErrorsUseStandardEnvelope() throws Exception {
        mvc.perform(post("/api/v1/unit-types").contentType("application/json").content("{\"code\":\"\",\"capacity\":0}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        when(service.validation(any())).thenThrow(new ApiException(HttpStatus.NOT_FOUND,"UNIT_NOT_FOUND","Unit not found"));
        mvc.perform(get("/api/v1/internal/units/"+UUID.randomUUID()+"/validate"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("UNIT_NOT_FOUND"));
    }
    @Test void ownershipDateOverlapAndAvailabilityFollowRules() {
        assertTrue(PropertyService.overlaps(LocalDate.parse("2026-01-01"),null,LocalDate.parse("2026-02-01"),null));
        assertFalse(PropertyService.overlaps(LocalDate.parse("2026-01-01"),LocalDate.parse("2026-01-31"),LocalDate.parse("2026-02-01"),null));
        Unit u=new Unit();u.status=UnitStatus.AVAILABLE;u.floor=new Floor();u.floor.status=RecordStatus.ACTIVE;u.floor.building=new Building();
        assertTrue(PropertyService.available(u));
        u.floor.building.status=RecordStatus.INACTIVE;assertFalse(PropertyService.available(u));
    }
}
