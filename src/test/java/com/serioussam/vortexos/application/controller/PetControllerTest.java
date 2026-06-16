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
@WithMockUser(username = "petter")
@Transactional
class PetControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaUserRepository userRepository;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setUsername("petter");
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        userRepository.save(u);
    }

    @Test
    void noCardYetReturns404() throws Exception {
        mockMvc.perform(get("/pet/me")).andExpect(status().isNotFound());
    }

    @Test
    void upsertsAndReadsBackTheCard_clampingCareScore() throws Exception {
        mockMvc.perform(put("/pet/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pixel\",\"stage\":\"teen\",\"mood\":\"playful\",\"level\":4,\"careScore\":150,\"happiness\":90,\"bond\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("petter")))
                .andExpect(jsonPath("$.stage", is("teen")))
                .andExpect(jsonPath("$.careScore", is(100))); // clamped from 150
        mockMvc.perform(get("/pet/me"))
                .andExpect(jsonPath("$.name", is("Pixel")))
                .andExpect(jsonPath("$.level", is(4)));
    }

    @Test
    void leaderboardIncludesPublishedCards() throws Exception {
        mockMvc.perform(put("/pet/me").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Pixel\",\"level\":7,\"careScore\":95}")).andExpect(status().isOk());
        mockMvc.perform(get("/pet/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("petter")));
    }

    @Test
    void cardsByUsersReturnsOnlyRequested() throws Exception {
        mockMvc.perform(put("/pet/me").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Pixel\",\"level\":2,\"careScore\":50}")).andExpect(status().isOk());
        mockMvc.perform(get("/pet/cards").param("users", "petter,ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("petter")));
        mockMvc.perform(get("/pet/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
