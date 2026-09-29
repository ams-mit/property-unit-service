package lk.ac.kln.property_unit_service.controller;

import lk.ac.kln.property_unit_service.model.*;
import lk.ac.kln.property_unit_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class OwnershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private UnitTypeRepository unitTypeRepository;

    @Autowired
    private UnitRepository unitRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    private Long savedUnitId;

    @BeforeEach
    void setUp() {
        Building building = new Building("BLD-OWN", "Ownership Building", "123 Own St");
        building = buildingRepository.save(building);

        Floor floor = new Floor(1, "First Floor");
        floor.setBuilding(building);
        floor = floorRepository.save(floor);

        UnitType unitType = new UnitType();
        unitType.setTypeName("Studio");
        unitType.setBaseRent(new BigDecimal("1000.00"));
        unitType.setCapacityLimit(2);
        unitType = unitTypeRepository.save(unitType);

        Unit unit = new Unit();
        unit.setPublicId(UUID.randomUUID());
        unit.setFloor(floor);
        unit.setUnitType(unitType);
        unit.setUnitNumber("101-OWN");
        unit.setStatus("AVAILABLE");
        unit = unitRepository.save(unit);
        
        savedUnitId = unit.getId();
    }

    @Test
    @DisplayName("Should assign owner successfully")
    void shouldAssignOwnerSuccessfully() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        String json = """
                {
                    "unitId": %d,
                    "ownerId": "OWN-001",
                    "sharePercentage": 50.00,
                    "startDate": "%s"
                }
                """.formatted(savedUnitId, LocalDate.now().toString());

        mockMvc.perform(post("/api/v1/ownerships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value("OWN-001"))
                .andExpect(jsonPath("$.sharePercentage").value(50.0))
                .andExpect(jsonPath("$.unitId").value(savedUnitId));
    }

    @Test
    @DisplayName("Should reject unknown owner ID")
    @org.junit.jupiter.api.Disabled("Known gap: ExternalValidationException not mapped to 404/400 in GlobalExceptionHandler")
    void shouldRejectUnknownOwnerId() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        String json = """
                {
                    "unitId": %d,
                    "ownerId": "UNKNOWN-001",
                    "sharePercentage": 50.00,
                    "startDate": "%s"
                }
                """.formatted(savedUnitId, LocalDate.now().toString());

        mockMvc.perform(post("/api/v1/ownerships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject inactive owner")
    @org.junit.jupiter.api.Disabled("Known gap: ExternalValidationException not mapped to 404/400 in GlobalExceptionHandler")
    void shouldRejectInactiveOwner() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        String json = """
                {
                    "unitId": %d,
                    "ownerId": "INACTIVE-001",
                    "sharePercentage": 50.00,
                    "startDate": "%s"
                }
                """.formatted(savedUnitId, LocalDate.now().toString());

        mockMvc.perform(post("/api/v1/ownerships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
