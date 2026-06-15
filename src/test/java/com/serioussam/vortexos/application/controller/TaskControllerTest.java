package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.task.Task;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaTaskRepository;
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
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaTaskRepository taskRepository;
    @Autowired private JpaUserRepository userRepository;

    private Long testerId;
    private Long bobId;

    @BeforeEach
    void setUp() {
        testerId = saveUser("tester");
        bobId = saveUser("bob");
    }

    private Long saveUser(String name) {
        User u = new User();
        u.setUsername(name);
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        return userRepository.save(u).getId();
    }

    @Test
    void createsAndLists() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Buy milk\",\"done\":false,\"dueAt\":null,\"priority\":1,\"notes\":\"2%\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Buy milk")))
                .andExpect(jsonPath("$.ownerId", is(testerId.intValue())));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Buy milk")));
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"done\":false,\"dueAt\":null,\"priority\":0,\"notes\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void togglesDone() throws Exception {
        String json = mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Walk dog\",\"done\":false,\"dueAt\":null,\"priority\":1,\"notes\":null}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.done", is(false)))
                .andReturn().getResponse().getContentAsString();
        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(json).get("id").asLong();

        mockMvc.perform(put("/tasks/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Walk dog\",\"done\":true,\"dueAt\":null,\"priority\":1,\"notes\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done", is(true)));

        mockMvc.perform(get("/tasks"))
                .andExpect(jsonPath("$[0].done", is(true)));
    }

    @Test
    void ownerIsolation() throws Exception {
        Task t = new Task();
        t.setOwnerId(bobId);
        t.setTitle("bob's task");
        t.setDone(false);
        t.setPriority(1);
        t.setCreatedAt(1);
        Long id = taskRepository.save(t).getId();

        mockMvc.perform(get("/tasks")).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(put("/tasks/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"hijack\",\"done\":true,\"dueAt\":null,\"priority\":2,\"notes\":null}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/tasks/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void deletes() throws Exception {
        String json = mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Throwaway\",\"done\":false,\"dueAt\":null,\"priority\":0,\"notes\":null}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(json).get("id").asLong();

        mockMvc.perform(delete("/tasks/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/tasks")).andExpect(jsonPath("$", hasSize(0)));
    }
}
