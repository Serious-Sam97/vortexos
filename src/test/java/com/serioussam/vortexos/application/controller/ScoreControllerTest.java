package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.score.Score;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaScoreRepository;
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
class ScoreControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaScoreRepository scoreRepository;
    @Autowired private JpaUserRepository userRepository;

    private Long testerId;

    @BeforeEach
    void setUp() {
        testerId = persistUser("tester");
    }

    private Long persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        return userRepository.save(u).getId();
    }

    private void persistScore(Long ownerId, String game, long value) {
        Score s = new Score();
        s.setOwnerId(ownerId);
        s.setGame(game);
        s.setValue(value);
        s.setCreatedAt(System.currentTimeMillis());
        scoreRepository.save(s);
    }

    @Test
    void recordsAScore() throws Exception {
        mockMvc.perform(post("/scores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"game\":\"snake\",\"value\":420}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.game", is("snake")))
                .andExpect(jsonPath("$.value", is(420)));
    }

    @Test
    void blankGameIsRejected() throws Exception {
        mockMvc.perform(post("/scores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"game\":\"\",\"value\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsUserScoresHighestFirst() throws Exception {
        persistScore(testerId, "snake", 100);
        persistScore(testerId, "snake", 500);
        persistScore(testerId, "snake", 250);
        mockMvc.perform(get("/scores").param("game", "snake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].value", is(500)));
    }

    @Test
    void supportsAscendingOrderForTimeGames() throws Exception {
        persistScore(testerId, "minesweeper:expert", 120);
        persistScore(testerId, "minesweeper:expert", 88);
        mockMvc.perform(get("/scores").param("game", "minesweeper:expert").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value", is(88)));
    }

    @Test
    void onlyReturnsTheCallersOwnScores() throws Exception {
        Long otherId = persistUser("rival");
        persistScore(otherId, "snake", 9999);
        persistScore(testerId, "snake", 10);
        mockMvc.perform(get("/scores").param("game", "snake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].value", is(10)));
    }

    @Test
    void recordStampsOwnerNameAndInitials() throws Exception {
        mockMvc.perform(post("/scores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"game\":\"snake\",\"value\":420,\"initials\":\"sam\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerName", is("tester")))
                .andExpect(jsonPath("$.initials", is("SAM")));
    }

    @Test
    void recordDefaultsInitialsFromUsername() throws Exception {
        mockMvc.perform(post("/scores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"game\":\"snake\",\"value\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.initials", is("TES")));
    }

    @Test
    void globalBoardIncludesEveryUsersScores() throws Exception {
        Long rivalId = persistUser("rival");
        persistScore(rivalId, "snake", 9999);
        persistScore(testerId, "snake", 10);
        mockMvc.perform(get("/scores/global").param("game", "snake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].value", is(9999)));
    }

    @Test
    void globalBoardSupportsAscendingOrder() throws Exception {
        Long rivalId = persistUser("rival");
        persistScore(rivalId, "race", 88);
        persistScore(testerId, "race", 120);
        mockMvc.perform(get("/scores/global").param("game", "race").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value", is(88)));
    }
}
