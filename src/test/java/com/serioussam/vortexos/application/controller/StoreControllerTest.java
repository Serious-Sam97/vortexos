package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.domain.store.AppListing;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaAppListingRepository;
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
class StoreControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JpaAppListingRepository appRepository;
    @Autowired private JpaUserRepository userRepository;

    private Long testerId;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setUsername("tester");
        u.setPassword("{noop}x");
        u.setRole("USER");
        u.setCreatedDate(LocalDate.now());
        testerId = userRepository.save(u).getId();
    }

    private String publishBody(String appId) {
        return "{\"appId\":\"" + appId + "\",\"name\":\"Test App\",\"version\":\"1.0.0\","
                + "\"description\":\"a demo\",\"icon\":\"data:img\",\"packageJson\":\"{\\\"x\\\":1}\"}";
    }

    @Test
    void publishesAndListsAndGets() throws Exception {
        mockMvc.perform(post("/store").contentType(MediaType.APPLICATION_JSON).content(publishBody("com.test.app")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appId", is("com.test.app")))
                .andExpect(jsonPath("$.author", is("tester")));

        mockMvc.perform(get("/store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test App")));

        mockMvc.perform(get("/store/com.test.app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageJson", containsString("\"x\":1")));
    }

    @Test
    void rejectsBlankPublish() throws Exception {
        mockMvc.perform(post("/store").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"\",\"name\":\"x\",\"version\":\"1\",\"packageJson\":\"{}\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownIs404() throws Exception {
        mockMvc.perform(get("/store/nope.nope")).andExpect(status().isNotFound());
    }

    @Test
    void installIncrementsCount() throws Exception {
        mockMvc.perform(post("/store").contentType(MediaType.APPLICATION_JSON).content(publishBody("com.test.app")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/store/com.test.app/install"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installs", is(1)));
    }

    @Test
    void cannotOverwriteAnotherUsersApp() throws Exception {
        // An app owned by someone else (authorId 999, not the tester).
        AppListing other = new AppListing();
        other.setAppId("com.other.app");
        other.setName("Other");
        other.setVersion("1.0.0");
        other.setAuthorId(999L);
        other.setAuthor("someone");
        other.setIcon("data:img");
        other.setPackageJson("{}");
        other.setInstalls(0);
        other.setCreatedAt(1);
        other.setUpdatedAt(1);
        appRepository.save(other);

        mockMvc.perform(post("/store").contentType(MediaType.APPLICATION_JSON).content(publishBody("com.other.app")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateInPlaceKeepsOneListing() throws Exception {
        mockMvc.perform(post("/store").contentType(MediaType.APPLICATION_JSON).content(publishBody("com.test.app")))
                .andExpect(status().isCreated());
        // re-publish same appId (owned by tester) → 200 update, still one listing
        mockMvc.perform(post("/store").contentType(MediaType.APPLICATION_JSON).content(publishBody("com.test.app")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/store")).andExpect(jsonPath("$", hasSize(1)));
    }
}
