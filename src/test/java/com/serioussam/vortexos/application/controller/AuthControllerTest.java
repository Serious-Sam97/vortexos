package com.serioussam.vortexos.application.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full HTTP-layer auth tests WITH the security filter chain active (no addFilters=false),
 * so register/login and JWT-gated access are exercised end to end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String body(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String register(String username, String password) throws Exception {
        String json = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body(username, password)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    @Test
    void register_returnsTokenAndUsername() throws Exception {
        String json = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body("alice", "hunter2")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        assertThat(node.has("password")).isFalse(); // never leak the hash
    }

    @Test
    void register_rejectsBlankCredentials() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body("", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_rejectsDuplicateUsername() throws Exception {
        register("bob", "pw");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body("bob", "other")))
                .andExpect(status().isConflict());
    }

    @Test
    void login_succeedsWithCorrectPassword() throws Exception {
        register("carol", "s3cret");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body("carol", "s3cret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("carol"));
    }

    @Test
    void login_failsWithWrongPassword() throws Exception {
        register("dave", "right");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body("dave", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_is401WithoutToken() throws Exception {
        mockMvc.perform(get("/games"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_is200WithToken() throws Exception {
        String token = register("erin", "pw");
        mockMvc.perform(get("/games").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_is401WithGarbageToken() throws Exception {
        mockMvc.perform(get("/games").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }
}
