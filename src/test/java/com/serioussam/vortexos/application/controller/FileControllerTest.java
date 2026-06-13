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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP-layer tests for the /files CRUD endpoints that back the VFS /mnt/cloud mount. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "tester")
@Transactional
class FileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaFileRepository fileRepository;
    @Autowired private JpaUserRepository userRepository;

    // base64 of "hello"
    private static final String HELLO_B64 = "aGVsbG8=";

    private Long testerId;

    @BeforeEach
    void setUp() {
        testerId = persistUser("tester");
    }

    private Long persistUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("{noop}irrelevant");
        user.setRole("USER");
        user.setCreatedDate(LocalDate.now());
        return userRepository.save(user).getId();
    }

    private File persist(String path, String type) {
        return persist(path, type, testerId);
    }

    private File persist(String path, String type, Long ownerId) {
        File f = new File();
        f.setOwnerId(ownerId);
        f.setPath(path);
        f.setName(path.substring(path.lastIndexOf('/') + 1));
        f.setType(type);
        f.setCreatedDate(LocalDate.now());
        f.setUpdatedDate(LocalDate.now());
        return fileRepository.save(f);
    }

    @Test
    void index_returnsStoredFiles() throws Exception {
        persist("/mnt/cloud/a.txt", "file");
        mockMvc.perform(get("/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].path", hasItem("/mnt/cloud/a.txt")));
    }

    @Test
    void index_excludesOtherUsersFiles() throws Exception {
        Long otherId = persistUser("someone-else");
        persist("/mnt/cloud/mine.txt", "file", testerId);
        persist("/mnt/cloud/theirs.txt", "file", otherId);

        mockMvc.perform(get("/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].path", hasItem("/mnt/cloud/mine.txt")))
                .andExpect(jsonPath("$[*].path", not(hasItem("/mnt/cloud/theirs.txt"))));
    }

    @Test
    void upsert_createsThenUpdatesByPath() throws Exception {
        String body = """
                { "path": "/mnt/cloud/note.txt", "name": "note.txt", "type": "file", "content": "%s" }
                """.formatted(HELLO_B64);

        mockMvc.perform(post("/files").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/mnt/cloud/note.txt"));

        File saved = fileRepository.findByPathAndOwnerId("/mnt/cloud/note.txt", testerId).orElseThrow();
        assertThat(saved.getContent()).isEqualTo(HELLO_B64); // stored verbatim as base64 text

        // posting the same path again updates rather than duplicating
        mockMvc.perform(post("/files").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        assertThat(fileRepository.findByPathStartingWithAndOwnerId("/mnt/cloud/note.txt", testerId)).hasSize(1);
    }

    @Test
    void upsert_withBlankPath_returns400() throws Exception {
        mockMvc.perform(post("/files").contentType(MediaType.APPLICATION_JSON).content("{\"path\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_removesPathAndDescendants() throws Exception {
        persist("/mnt/cloud/dir", "folder");
        persist("/mnt/cloud/dir/a.txt", "file");
        persist("/mnt/cloud/other.txt", "file");

        mockMvc.perform(delete("/files").param("path", "/mnt/cloud/dir"))
                .andExpect(status().isNoContent());

        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/dir", testerId)).isEmpty();
        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/dir/a.txt", testerId)).isEmpty();
        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/other.txt", testerId)).isPresent(); // sibling untouched
    }

    @Test
    void delete_leavesAnotherUsersFileAtTheSamePath() throws Exception {
        Long otherId = persistUser("someone-else");
        persist("/mnt/cloud/shared.txt", "file", testerId);
        persist("/mnt/cloud/shared.txt", "file", otherId);

        mockMvc.perform(delete("/files").param("path", "/mnt/cloud/shared.txt"))
                .andExpect(status().isNoContent());

        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/shared.txt", testerId)).isEmpty();
        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/shared.txt", otherId)).isPresent();
    }

    @Test
    void rename_rewritesPathAndDescendantPrefixes() throws Exception {
        persist("/mnt/cloud/old", "folder");
        persist("/mnt/cloud/old/file.txt", "file");

        mockMvc.perform(put("/files/rename").param("from", "/mnt/cloud/old").param("to", "/mnt/cloud/new"))
                .andExpect(status().isNoContent());

        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/new", testerId)).isPresent();
        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/new/file.txt", testerId)).isPresent();
        assertThat(fileRepository.findByPathAndOwnerId("/mnt/cloud/old", testerId)).isEmpty();
    }
}
