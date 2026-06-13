package com.serioussam.vortexos.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifies GET /platforms serves the catalogue seeded by PlatformSeeder. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PlatformControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void getAllPlatforms_returnsTheSeededCatalogue() throws Exception {
        mockMvc.perform(get("/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(24))
                .andExpect(jsonPath("$[*].name", hasItems("PC", "Nintendo Switch", "PlayStation 5", "Xbox Series X")));
    }
}
