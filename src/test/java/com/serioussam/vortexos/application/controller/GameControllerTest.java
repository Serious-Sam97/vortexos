package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.game.Game;
import com.serioussam.vortexos.domain.platform.Platform;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaGameRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaPlatformRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full HTTP-layer tests for /games against a real (in-memory) database.
 * Each test runs in a transaction that rolls back, so games never leak between tests;
 * the platforms seeded at startup stay put.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "tester")
@Transactional
class GameControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaGameRepository gameRepository;
    @Autowired private JpaPlatformRepository platformRepository;
    @Autowired private JpaUserRepository userRepository;

    private Platform platform;
    private Long testerId;

    @BeforeEach
    void setUp() {
        platform = platformRepository.findAll().get(0); // seeded "PC"
        testerId = persistUser("tester");
    }

    private Long persistUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("{noop}irrelevant"); // never authenticated against in these tests
        user.setRole("USER");
        user.setCreatedDate(LocalDate.now());
        return userRepository.save(user).getId();
    }

    private Game persistGame(String title, boolean backlog, boolean completed) {
        return persistGame(title, backlog, completed, testerId);
    }

    private Game persistGame(String title, boolean backlog, boolean completed, Long ownerId) {
        Game game = new Game();
        game.setOwnerId(ownerId);
        game.setTitle(title);
        game.setPlatform(platform);
        game.setBacklog(backlog);
        game.setCompleted(completed);
        game.setStartedDate(LocalDate.now());
        return gameRepository.save(game);
    }

    // ── read / filtering ────────────────────────────────────────────────

    @Test
    void getAllGames_returnsOnlyNonBacklogGames() throws Exception {
        persistGame("Active Game", false, false);
        persistGame("Shelved Game", true, false);

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Active Game"))
                .andExpect(jsonPath("$[0].backlog").value(false));
    }

    @Test
    void getBacklog_returnsOnlyBacklogGames() throws Exception {
        persistGame("Active Game", false, false);
        persistGame("Shelved Game", true, false);

        mockMvc.perform(get("/games/backlog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Shelved Game"))
                .andExpect(jsonPath("$[0].backlog").value(true));
    }

    // ── create ──────────────────────────────────────────────────────────

    @Test
    void createGame_persistsAndReturns201() throws Exception {
        String body = """
                { "title": "Hollow Knight", "platform_id": %d, "backlog": true }
                """.formatted(platform.getId());

        mockMvc.perform(post("/games").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Hollow Knight"))
                .andExpect(jsonPath("$.backlog").value(true))
                .andExpect(jsonPath("$.platform.id").value(platform.getId()))
                .andExpect(jsonPath("$.startedDate").isNotEmpty());

        assertThat(gameRepository.gamesList(testerId)).isEmpty(); // it's in the backlog
        assertThat(gameRepository.backlogGamesList(testerId)).extracting(Game::getTitle).contains("Hollow Knight");
    }

    @Test
    void getAllGames_excludesOtherUsersGames() throws Exception {
        Long otherId = persistUser("someone-else");
        persistGame("My Game", false, false, testerId);
        persistGame("Their Game", false, false, otherId);

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("My Game"));
    }

    @Test
    void mutating_anotherUsersGame_returns404() throws Exception {
        Long otherId = persistUser("someone-else");
        Game theirs = persistGame("Their Game", false, false, otherId);

        // The tester must not be able to see, update, complete, start, or delete it.
        mockMvc.perform(put("/games/" + theirs.getId()).contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"x\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/games/" + theirs.getId())).andExpect(status().isNotFound());
        mockMvc.perform(post("/games/" + theirs.getId() + "/complete")).andExpect(status().isNotFound());
        mockMvc.perform(get("/games/" + theirs.getId() + "/start")).andExpect(status().isNotFound());
    }

    @Test
    void createGame_withUnknownPlatform_returns404() throws Exception {
        String body = """
                { "title": "Orphan", "platform_id": 999999, "backlog": false }
                """;
        mockMvc.perform(post("/games").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGame_withMissingTitle_returns400() throws Exception {
        String body = """
                { "platform_id": %d, "backlog": false }
                """.formatted(platform.getId());
        mockMvc.perform(post("/games").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    // ── update notes ────────────────────────────────────────────────────

    @Test
    void updateGame_updatesNotes() throws Exception {
        Game game = persistGame("Celeste", false, false);
        String body = """
                { "notes": "Beat chapter 7" }
                """;

        mockMvc.perform(put("/games/" + game.getId()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Beat chapter 7"));

        assertThat(gameRepository.findById(game.getId()).orElseThrow().getNotes()).isEqualTo("Beat chapter 7");
    }

    @Test
    void updateGame_notFound_returns404() throws Exception {
        mockMvc.perform(put("/games/999999").contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    // ── complete ────────────────────────────────────────────────────────

    @Test
    void completeGame_marksCompletedAndReturns204() throws Exception {
        Game game = persistGame("Inside", false, false);

        mockMvc.perform(post("/games/" + game.getId() + "/complete"))
                .andExpect(status().isNoContent());

        Game updated = gameRepository.findById(game.getId()).orElseThrow();
        assertThat(updated.isCompleted()).isTrue();
        assertThat(updated.getCompletedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void completeGame_notFound_returns404() throws Exception {
        mockMvc.perform(post("/games/999999/complete")).andExpect(status().isNotFound());
    }

    // ── delete ──────────────────────────────────────────────────────────

    @Test
    void deleteGame_removesItAndReturns204() throws Exception {
        Game game = persistGame("Limbo", false, false);

        mockMvc.perform(delete("/games/" + game.getId())).andExpect(status().isNoContent());

        assertThat(gameRepository.existsById(game.getId())).isFalse();
    }

    @Test
    void deleteGame_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/games/999999")).andExpect(status().isNotFound());
    }

    // ── start (move out of backlog) ─────────────────────────────────────

    @Test
    void startGame_clearsBacklogAndReturns204() throws Exception {
        Game game = persistGame("Hades", true, false);

        mockMvc.perform(get("/games/" + game.getId() + "/start"))
                .andExpect(status().isNoContent());

        Game updated = gameRepository.findById(game.getId()).orElseThrow();
        assertThat(updated.getBacklog()).isFalse();
        assertThat(updated.getNotes()).isEqualTo("");
        assertThat(updated.getStartedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void startGame_notFound_returns404() throws Exception {
        mockMvc.perform(get("/games/999999/start")).andExpect(status().isNotFound());
    }
}
