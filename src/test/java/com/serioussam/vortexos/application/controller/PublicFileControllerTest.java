package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.infrastructure.repository.JpaFileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The shared public drive (/mnt/public): not scoped to the caller, so any signed-in user
 * sees the same files — and they stay separate from anyone's private /mnt/cloud files.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "tester")
@Transactional
class PublicFileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaFileRepository fileRepository;

    private File privateFile(String path, Long ownerId) {
        File f = new File();
        f.setOwnerId(ownerId);
        f.setPath(path);
        f.setName(path.substring(path.lastIndexOf('/') + 1));
        f.setType("file");
        f.setCreatedDate(java.time.LocalDate.now());
        f.setUpdatedDate(java.time.LocalDate.now());
        return fileRepository.save(f);
    }

    @Test
    void upsert_thenIndex_returnsPublicFile() throws Exception {
        String body = "{\"path\":\"/mnt/public/notice.txt\",\"name\":\"notice.txt\",\"type\":\"file\",\"content\":\"aGk=\"}";
        mockMvc.perform(post("/public/files").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/mnt/public/notice.txt"));

        mockMvc.perform(get("/public/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].path", hasItem("/mnt/public/notice.txt")));

        // stored under the reserved public owner (0)
        assertThat(fileRepository.findByPathAndOwnerId("/mnt/public/notice.txt", PublicFileController.PUBLIC_OWNER))
                .isPresent();
    }

    @Test
    void publicIndex_excludesPrivateFiles() throws Exception {
        privateFile("/mnt/cloud/secret.txt", 42L); // some user's private file
        privateFile("/mnt/public/shared.txt", PublicFileController.PUBLIC_OWNER);

        mockMvc.perform(get("/public/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].path", hasItem("/mnt/public/shared.txt")))
                .andExpect(jsonPath("$[*].path", not(hasItem("/mnt/cloud/secret.txt"))));
    }

    @Test
    void delete_removesPublicFile() throws Exception {
        privateFile("/mnt/public/temp.txt", PublicFileController.PUBLIC_OWNER);

        mockMvc.perform(delete("/public/files").param("path", "/mnt/public/temp.txt"))
                .andExpect(status().isNoContent());

        assertThat(fileRepository.findByPathAndOwnerId("/mnt/public/temp.txt", PublicFileController.PUBLIC_OWNER))
                .isEmpty();
    }

    @Test
    void upsert_withBlankPath_returns400() throws Exception {
        mockMvc.perform(post("/public/files").contentType(MediaType.APPLICATION_JSON).content("{\"path\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
