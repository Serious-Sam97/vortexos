package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.contact.Contact;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaContactRepository;
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
class ContactControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaContactRepository contactRepository;
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
        mockMvc.perform(post("/contacts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@x.com\",\"phone\":\"555\",\"notes\":\"friend\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.email", is("alice@x.com")));

        mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")));
    }

    @Test
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/contacts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"email\":\"x@x.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownerIsolation() throws Exception {
        Contact c = new Contact();
        c.setOwnerId(bobId);
        c.setName("Bob's Secret");
        Long id = contactRepository.save(c).getId();

        // tester cannot see bob's contact
        mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // tester cannot update or delete it
        mockMvc.perform(put("/contacts/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hijacked\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/contacts/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updates() throws Exception {
        Contact c = new Contact();
        c.setOwnerId(testerId);
        c.setName("Old Name");
        Long id = contactRepository.save(c).getId();

        mockMvc.perform(put("/contacts/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\",\"phone\":\"999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.phone", is("999")));

        // blank name on update is rejected
        mockMvc.perform(put("/contacts/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletes() throws Exception {
        Contact c = new Contact();
        c.setOwnerId(testerId);
        c.setName("Delete Me");
        Long id = contactRepository.save(c).getId();

        mockMvc.perform(delete("/contacts/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/contacts")).andExpect(jsonPath("$", hasSize(0)));
    }
}
