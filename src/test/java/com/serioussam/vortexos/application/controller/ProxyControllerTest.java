package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The Browser proxy — auth + SSRF guards (no external network calls in these tests). */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProxyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    private String token() {
        return jwtService.generateToken("tester");
    }

    @Test
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get("/proxy").param("url", "https://example.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/proxy").param("url", "https://example.com").param("token", "not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNonHttpScheme() throws Exception {
        mockMvc.perform(get("/proxy").param("url", "file:///etc/passwd").param("token", token()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blocksLoopbackHost_ssrf() throws Exception {
        mockMvc.perform(get("/proxy").param("url", "http://localhost:8082/files").param("token", token()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/proxy").param("url", "http://127.0.0.1/").param("token", token()))
                .andExpect(status().isForbidden());
    }
}
