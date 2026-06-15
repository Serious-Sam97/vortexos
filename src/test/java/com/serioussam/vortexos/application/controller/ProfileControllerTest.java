package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "tester")
@Transactional
class ProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaUserRepository userRepository;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setUsername("tester");
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        userRepository.save(u);
    }

    @Test
    void defaultsDisplayNameToUsernameWhenNoProfile() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("tester")))
                .andExpect(jsonPath("$.settings", is("{}")));
    }

    @Test
    void savesAndReadsBackTheProfile() throws Exception {
        mockMvc.perform(put("/profile").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Neo\",\"avatar\":\"😎\",\"settings\":\"{\\\"theme\\\":\\\"dark\\\"}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("Neo")))
                .andExpect(jsonPath("$.avatar", is("😎")));
        mockMvc.perform(get("/profile"))
                .andExpect(jsonPath("$.displayName", is("Neo")))
                .andExpect(jsonPath("$.settings", is("{\"theme\":\"dark\"}")));
    }

    @Test
    void upsertUpdatesTheExistingRow() throws Exception {
        mockMvc.perform(put("/profile").contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"First\"}")).andExpect(status().isOk());
        mockMvc.perform(put("/profile").contentType(MediaType.APPLICATION_JSON)
                .content("{\"avatar\":\"🚀\"}")).andExpect(status().isOk());
        // display name from the first PUT survives; avatar from the second is applied
        mockMvc.perform(get("/profile"))
                .andExpect(jsonPath("$.displayName", is("First")))
                .andExpect(jsonPath("$.avatar", is("🚀")));
    }
}
