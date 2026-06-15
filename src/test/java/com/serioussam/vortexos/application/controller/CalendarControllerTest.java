package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.calendar.CalendarEvent;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaCalendarRepository;
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
class CalendarControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaCalendarRepository calendarRepository;
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

    private Long saveEvent(Long ownerId, String title) {
        CalendarEvent e = new CalendarEvent();
        e.setOwnerId(ownerId);
        e.setTitle(title);
        e.setStart(System.currentTimeMillis());
        e.setAllDay(false);
        e.setReminderMinutes(-1);
        return calendarRepository.save(e).getId();
    }

    @Test
    void createsAndListsEvent() throws Exception {
        long start = System.currentTimeMillis();
        mockMvc.perform(post("/calendar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Standup\",\"start\":" + start + ",\"endAt\":null,\"allDay\":false,\"notes\":\"daily\",\"reminderMinutes\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Standup")));

        mockMvc.perform(get("/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Standup")));
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        long start = System.currentTimeMillis();
        mockMvc.perform(post("/calendar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"start\":" + start + ",\"endAt\":null,\"allDay\":false,\"notes\":null,\"reminderMinutes\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownerIsolation() throws Exception {
        Long bobsEvent = saveEvent(bobId, "Bob's meeting");

        mockMvc.perform(get("/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(put("/calendar/" + bobsEvent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hijack\",\"start\":" + System.currentTimeMillis() + ",\"endAt\":null,\"allDay\":false,\"notes\":null,\"reminderMinutes\":-1}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/calendar/" + bobsEvent))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatesEvent() throws Exception {
        Long id = saveEvent(testerId, "Old title");

        mockMvc.perform(put("/calendar/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New title\",\"start\":" + System.currentTimeMillis() + ",\"endAt\":null,\"allDay\":false,\"notes\":null,\"reminderMinutes\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New title")));

        mockMvc.perform(get("/calendar"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("New title")));
    }

    @Test
    void deletesEvent() throws Exception {
        Long id = saveEvent(testerId, "Doomed");

        mockMvc.perform(delete("/calendar/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/calendar"))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
