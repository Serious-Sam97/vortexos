package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.match.MatchResult;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaMatchResultRepository;
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
class MatchResultControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaMatchResultRepository matchRepository;
    @Autowired private JpaUserRepository userRepository;

    private Long testerId;

    @BeforeEach
    void setUp() { testerId = persistUser("tester"); }

    private Long persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        return userRepository.save(u).getId();
    }

    private void persistMatch(Long ownerId, String game, String opp, boolean won) {
        MatchResult m = new MatchResult();
        m.setOwnerId(ownerId); m.setGame(game); m.setOpponent(opp); m.setWon(won);
        m.setCreatedAt(System.currentTimeMillis());
        matchRepository.save(m);
    }

    @Test
    void reportsAMatch() throws Exception {
        mockMvc.perform(post("/matches").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"game\":\"pong\",\"opponent\":\"rival\",\"won\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.game", is("pong")))
                .andExpect(jsonPath("$.won", is(true)));
    }

    @Test
    void rejectsBlankOpponent() throws Exception {
        mockMvc.perform(post("/matches").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"game\":\"pong\",\"opponent\":\"\",\"won\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsTheCallersHistoryOnly() throws Exception {
        Long otherId = persistUser("rival");
        persistMatch(testerId, "pong", "rival", true);
        persistMatch(testerId, "tictactoe", "rival", false);
        persistMatch(otherId, "pong", "tester", false);
        mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
