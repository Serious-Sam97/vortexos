package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaFileRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Network Neighborhood: only shared files of other users are visible; private stay private. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "tester")
@Transactional
class ShareControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaFileRepository fileRepository;
    @Autowired private JpaUserRepository userRepository;

    private Long alice;
    private Long testerId;

    @BeforeEach
    void setUp() {
        testerId = user("tester");
        alice = user("alice");
    }

    private Long user(String name) {
        User u = new User();
        u.setUsername(name);
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        return userRepository.save(u).getId();
    }

    private void file(String path, Long ownerId, boolean shared) {
        File f = new File();
        f.setOwnerId(ownerId);
        f.setPath(path);
        f.setName(path.substring(path.lastIndexOf('/') + 1));
        f.setType("file");
        f.setContent("aGk=");
        f.setShared(shared);
        f.setCreatedDate(LocalDate.now());
        f.setUpdatedDate(LocalDate.now());
        fileRepository.save(f);
    }

    @Test
    void sharers_listsOnlyUsersWithSharedFiles() throws Exception {
        file("/mnt/cloud/pub.txt", alice, true);
        file("/mnt/cloud/priv.txt", testerId, false); // tester shares nothing

        mockMvc.perform(get("/share/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("alice")))
                .andExpect(jsonPath("$[*].username", not(hasItem("tester"))))
                .andExpect(jsonPath("$[*].password").doesNotExist());
    }

    @Test
    void shares_returnsOnlyThatUsersSharedFiles() throws Exception {
        file("/mnt/cloud/shared.txt", alice, true);
        file("/mnt/cloud/private.txt", alice, false);

        mockMvc.perform(get("/share/users/" + alice + "/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].path").value("/mnt/cloud/shared.txt"))
                .andExpect(jsonPath("$[0].content").value("aGk=")); // content included for read-only open
    }

    @Test
    void setShared_togglesOwnFile_andSurfacesInNeighborhood() throws Exception {
        file("/mnt/cloud/mine.txt", testerId, false);

        mockMvc.perform(put("/files/share").param("path", "/mnt/cloud/mine.txt").param("shared", "true"))
                .andExpect(status().isNoContent());

        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/mine.txt", testerId).orElseThrow().isShared()).isTrue();
        mockMvc.perform(get("/share/users")).andExpect(jsonPath("$[*].username", hasItem("tester")));

        // unshare again
        mockMvc.perform(put("/files/share").param("path", "/mnt/cloud/mine.txt").param("shared", "false"))
                .andExpect(status().isNoContent());
        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/mine.txt", testerId).orElseThrow().isShared()).isFalse();
    }

    @Test
    void setShared_onAnotherUsersFile_returns404() throws Exception {
        file("/mnt/cloud/alices.txt", alice, false); // owned by alice, tester can't share it

        mockMvc.perform(put("/files/share").param("path", "/mnt/cloud/alices.txt").param("shared", "true"))
                .andExpect(status().isNotFound());
    }
}
