package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaAchievementRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class AchievementControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaAchievementRepository achievementRepository;
    @Autowired private JpaUserRepository userRepository;

    @BeforeEach
    void setUp() {
        persistUser("tester");
    }

    private Long persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        return userRepository.save(u).getId();
    }

    @Test
    void firstUnlockIsCreatedAndNew() throws Exception {
        mockMvc.perform(post("/achievements/first_blood"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key", is("first_blood")))
                .andExpect(jsonPath("$.newlyUnlocked", is(true)));
    }

    @Test
    void secondUnlockIsIdempotentAndNotNew() throws Exception {
        mockMvc.perform(post("/achievements/hat_trick")).andExpect(status().isCreated());
        mockMvc.perform(post("/achievements/hat_trick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newlyUnlocked", is(false)));
    }

    @Test
    void listReturnsTheUsersUnlocked() throws Exception {
        mockMvc.perform(post("/achievements/insert_coin")).andExpect(status().isCreated());
        mockMvc.perform(post("/achievements/challenger")).andExpect(status().isCreated());
        mockMvc.perform(get("/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listIsScopedToTheCaller() throws Exception {
        Long otherId = persistUser("rival");
        com.serioussam.vortexos.domain.achievement.Achievement a = new com.serioussam.vortexos.domain.achievement.Achievement();
        a.setOwnerId(otherId);
        a.setAchKey("first_blood");
        a.setUnlockedAt(System.currentTimeMillis());
        achievementRepository.save(a);

        mockMvc.perform(get("/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
