package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.mail.Mail;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaMailRepository;
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
class MailControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaMailRepository mailRepository;
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
    void sendDeliversToRecipientInbox() throws Exception {
        mockMvc.perform(post("/mail").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"bob\",\"subject\":\"Hi\",\"body\":\"hello bob\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromName", is("tester")))
                .andExpect(jsonPath("$.toName", is("bob")));

        var inbox = mailRepository.findByToIdAndDraftFalseAndTrashedByRecipientFalseOrderByCreatedAtDesc(bobId);
        org.junit.jupiter.api.Assertions.assertEquals(1, inbox.size());
        org.junit.jupiter.api.Assertions.assertFalse(inbox.get(0).isReadByRecipient());
    }

    @Test
    void sendToUnknownUserIs404() throws Exception {
        mockMvc.perform(post("/mail").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"ghost\",\"subject\":\"x\",\"body\":\"y\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sentFolderShowsMyOutgoing() throws Exception {
        mockMvc.perform(post("/mail").contentType(MediaType.APPLICATION_JSON)
                .content("{\"to\":\"bob\",\"subject\":\"Hi\",\"body\":\"x\"}"));
        mockMvc.perform(get("/mail").param("folder", "sent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].toName", is("bob")));
    }

    @Test
    void openingInboxMailMarksItRead() throws Exception {
        Mail m = new Mail();
        m.setFromId(bobId); m.setFromName("bob");
        m.setToId(testerId); m.setToName("tester");
        m.setSubject("yo"); m.setBody("read me"); m.setCreatedAt(1);
        Long id = mailRepository.save(m).getId();

        mockMvc.perform(get("/mail").param("folder", "inbox"))
                .andExpect(jsonPath("$[0].read", is(false)));
        mockMvc.perform(get("/mail/" + id)).andExpect(jsonPath("$.read", is(true)));
        // now unread count is 0
        mockMvc.perform(get("/mail/unread")).andExpect(jsonPath("$.count", is(0)));
    }

    @Test
    void deleteTrashesThenRemoves() throws Exception {
        Mail m = new Mail();
        m.setFromId(bobId); m.setFromName("bob");
        m.setToId(testerId); m.setToName("tester");
        m.setSubject("bye"); m.setBody("trash me"); m.setCreatedAt(1);
        Long id = mailRepository.save(m).getId();

        mockMvc.perform(delete("/mail/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/mail").param("folder", "inbox")).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/mail").param("folder", "trash")).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(delete("/mail/" + id)).andExpect(status().isNoContent()); // from trash → gone
        mockMvc.perform(get("/mail").param("folder", "trash")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void draftSaveThenSend() throws Exception {
        String json = mockMvc.perform(post("/mail/draft").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"bob\",\"subject\":\"d\",\"body\":\"draft\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.draft", is(true)))
                .andReturn().getResponse().getContentAsString();
        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(json).get("id").asLong();
        mockMvc.perform(get("/mail").param("folder", "drafts")).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(post("/mail/" + id + "/send").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"bob\",\"subject\":\"d\",\"body\":\"draft\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.draft", is(false)));
        mockMvc.perform(get("/mail").param("folder", "drafts")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void directoryExcludesMe() throws Exception {
        mockMvc.perform(get("/mail/directory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("bob")))
                .andExpect(jsonPath("$", not(hasItem("tester"))));
    }
}
