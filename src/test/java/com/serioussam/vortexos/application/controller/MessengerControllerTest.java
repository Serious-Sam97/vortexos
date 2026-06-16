package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.messenger.Buddy;
import com.serioussam.vortexos.domain.messenger.Message;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaBuddyRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaMessageRepository;
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
class MessengerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaBuddyRepository buddyRepository;
    @Autowired private JpaUserRepository userRepository;
    @Autowired private JpaMessageRepository messageRepository;

    private Long bobId;

    @BeforeEach
    void setUp() {
        saveUser("tester");
        bobId = saveUser("bob");
        saveUser("carol");
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
    void addsAndListsBuddies() throws Exception {
        mockMvc.perform(post("/messenger/buddies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("bob")))
                .andExpect(jsonPath("$.displayName", is("bob")));

        mockMvc.perform(get("/messenger/buddies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("bob")));
    }

    @Test
    void rejectsUnknownUserSelfAndDuplicates() throws Exception {
        // unknown user → 404
        mockMvc.perform(post("/messenger/buddies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\"}"))
                .andExpect(status().isNotFound());

        // yourself → 400
        mockMvc.perform(post("/messenger/buddies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\"}"))
                .andExpect(status().isBadRequest());

        // first add ok, second add → 409
        mockMvc.perform(post("/messenger/buddies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"carol\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/messenger/buddies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"carol\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void removesBuddy() throws Exception {
        mockMvc.perform(post("/messenger/buddies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\"}")).andExpect(status().isCreated());

        mockMvc.perform(delete("/messenger/buddies/bob")).andExpect(status().isNoContent());
        mockMvc.perform(get("/messenger/buddies")).andExpect(jsonPath("$", hasSize(0)));

        // removing a non-buddy → 404
        mockMvc.perform(delete("/messenger/buddies/carol")).andExpect(status().isNotFound());
    }

    @Test
    void buddyListIsOwnerScoped() throws Exception {
        // bob has carol on his list; tester must not see it
        Buddy b = new Buddy();
        b.setOwnerId(bobId);
        b.setBuddyUsername("carol");
        buddyRepository.save(b);

        mockMvc.perform(get("/messenger/buddies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void upsertsMyProfile() throws Exception {
        // defaults when nothing saved yet
        mockMvc.perform(get("/messenger/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("tester")))
                .andExpect(jsonPath("$.status", is("available")));

        mockMvc.perform(put("/messenger/profile").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"personalMessage\":\"out chasing bugs\",\"status\":\"busy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalMessage", is("out chasing bugs")))
                .andExpect(jsonPath("$.status", is("busy")));

        mockMvc.perform(get("/messenger/profile"))
                .andExpect(jsonPath("$.personalMessage", is("out chasing bugs")))
                .andExpect(jsonPath("$.status", is("busy")));
    }

    private void saveMsg(String sender, String recipient, String groupId, String body, long ts) {
        Message m = new Message();
        m.setSender(sender); m.setRecipient(recipient); m.setGroupId(groupId); m.setBody(body); m.setCreatedAt(ts);
        messageRepository.save(m);
    }

    @Test
    void returns1to1HistoryBothDirectionsOrdered() throws Exception {
        saveMsg("tester", "bob", null, "hi bob", 1000);
        saveMsg("bob", "tester", null, "hey tester", 2000);
        saveMsg("tester", "carol", null, "not in bob convo", 1500); // different conversation

        mockMvc.perform(get("/messenger/history").param("with", "bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].body", is("hi bob")))     // oldest first
                .andExpect(jsonPath("$[1].body", is("hey tester")));
    }

    @Test
    void groupHistoryRequiresMembership() throws Exception {
        saveMsg("bob", null, "bob|carol|tester", "group hello", 1000);

        // tester IS a member of the group id → ok
        mockMvc.perform(get("/messenger/history").param("group", "bob|carol|tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].body", is("group hello")));

        // tester is NOT a member → forbidden
        mockMvc.perform(get("/messenger/history").param("group", "bob|carol"))
                .andExpect(status().isForbidden());
    }

    @Test
    void historyRequiresWithOrGroup() throws Exception {
        mockMvc.perform(get("/messenger/history")).andExpect(status().isBadRequest());
    }
}
