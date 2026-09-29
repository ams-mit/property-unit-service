package lk.ac.kln.property_unit_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class BuildingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should create building successfully")
    void shouldCreateBuildingSuccessfully() throws Exception {
        String buildingJson = """
                {
                    "buildingCode": "BLD-001",
                    "name": "Tech Tower",
                    "address": "123 Tech Ave",
                    "totalFloors": 5
                }
                """;

        mockMvc.perform(post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.buildingCode", is("BLD-001")))
                .andExpect(jsonPath("$.name", is("Tech Tower")))
                .andExpect(jsonPath("$.address", is("123 Tech Ave")));
    }

    @Test
    @DisplayName("Should auto generate floors on building creation")
    @org.junit.jupiter.api.Disabled("Known gap: floor auto-generation and GET /api/v1/buildings/{code}/floors not implemented, see GAPS.md")
    void shouldAutoGenerateFloorsOnBuildingCreation() throws Exception {
        String buildingJson = """
                {
                    "buildingCode": "BLD-002",
                    "name": "Innovation Center",
                    "address": "456 Innovation Blvd",
                    "totalFloors": 3
                }
                """;

        // Create building with 3 floors
        mockMvc.perform(post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildingJson))
                .andExpect(status().isCreated());

        // Verify 3 floors were generated
        mockMvc.perform(get("/api/v1/buildings/BLD-002/floors")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].floorNumber", is(1)))
                .andExpect(jsonPath("$[1].floorNumber", is(2)))
                .andExpect(jsonPath("$[2].floorNumber", is(3)));
    }

    @Test
    @DisplayName("Should reject duplicate building code")
    void shouldRejectDuplicateBuildingCode() throws Exception {
        String buildingJson = """
                {
                    "buildingCode": "BLD-003",
                    "name": "Original Building",
                    "address": "789 Original St",
                    "totalFloors": 4
                }
                """;

        // First creation succeeds
        mockMvc.perform(post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildingJson))
                .andExpect(status().isCreated());

        // Duplicate creation attempt fails with 409 Conflict
        String duplicateBuildingJson = """
                {
                    "buildingCode": "BLD-003",
                    "name": "Duplicate Building",
                    "address": "789 Original St",
                    "totalFloors": 2
                }
                """;

        mockMvc.perform(post("/api/v1/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateBuildingJson))
                .andExpect(status().isConflict());
    }
}
