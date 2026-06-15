package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaBbsRepository;
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
class BbsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaBbsRepository bbsRepository;
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

    private Long createThread(String title, String body) throws Exception {
        String json = mockMvc.perform(post("/bbs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName", is("tester")))
                .andReturn().getResponse().getContentAsString();
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(json).get("id").asLong();
    }

    @Test
    void createsAndListsThreads() throws Exception {
        createThread("Welcome", "First post!");
        createThread("Second", "Another thread");
        mockMvc.perform(get("/bbs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                // newest first
                .andExpect(jsonPath("$[0].title", is("Second")))
                .andExpect(jsonPath("$[0].replyCount", is(0)));
    }

    @Test
    void rejectsBlankThread() throws Exception {
        mockMvc.perform(post("/bbs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"body\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void repliesAppearUnderTheThread() throws Exception {
        Long id = createThread("QnA", "Ask me anything");
        mockMvc.perform(post("/bbs/" + id + "/reply").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"great question\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/bbs/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thread.title", is("QnA")))
                .andExpect(jsonPath("$.replies", hasSize(1)))
                .andExpect(jsonPath("$.replies[0].body", is("great question")));
        // reply count reflected in the listing
        mockMvc.perform(get("/bbs"))
                .andExpect(jsonPath("$[0].replyCount", is(1)));
    }

    @Test
    void replyToMissingThreadIs404() throws Exception {
        mockMvc.perform(post("/bbs/99999/reply").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"hello?\"}"))
                .andExpect(status().isNotFound());
    }
}
