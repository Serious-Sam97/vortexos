package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.infrastructure.repository.JpaFileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaFileRepository fileRepository;

    @Test
    void index_returnsStoredFiles() throws Exception {
        File folder = new File();
        folder.setName("Documents");
        folder.setPath("/Documents");
        folder.setType("folder");
        folder.setCreatedDate(LocalDate.now());
        folder.setUpdatedDate(LocalDate.now());
        fileRepository.save(folder);

        mockMvc.perform(get("/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Documents")))
                .andExpect(jsonPath("$[*].type", hasItem("folder")));
    }

    @Test
    void index_returnsOkWhenEmpty() throws Exception {
        mockMvc.perform(get("/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
