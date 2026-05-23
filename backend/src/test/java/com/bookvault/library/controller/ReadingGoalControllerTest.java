package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReadingGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReadingGoalControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReaderRepository readerRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtils jwtUtils;
    @Autowired ReadingGoalRepository readingGoalRepository;

    private String token;
    private Integer readerId;

    @BeforeEach
    void setup() {
        Reader reader = readerRepository.findByEmail("goaltest@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("goaltestuser");
            r.setEmail("goaltest@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            return readerRepository.save(r);
        });
        readerId = reader.getId();
        token = jwtUtils.generateToken(reader.getEmail(), reader.getId(), reader.getUsername(), reader.getRole());

        // Clean up goal for current year to avoid unique constraint issues
        int currentYear = LocalDate.now().getYear();
        readingGoalRepository.findByReader_IdAndYear(readerId, currentYear)
                .ifPresent(readingGoalRepository::delete);
    }

    // ── GET /reading-goals ────────────────────────────────────────

    @Test
    void getGoals_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reading-goals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getGoals_withToken_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reading-goals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── POST /reading-goals ───────────────────────────────────────

    @Test
    void upsertGoal_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/reading-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"year":2025,"targetBooks":20}
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void upsertGoal_withToken_returns200() throws Exception {
        int currentYear = LocalDate.now().getYear();

        mockMvc.perform(post("/api/v1/reading-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"year":%d,"targetBooks":25}
                """, currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetBooks").value(25))
                .andExpect(jsonPath("$.year").value(currentYear));
    }

    @Test
    void upsertGoal_invalidTargetBooks_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reading-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                    {"year":2025,"targetBooks":0}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsertGoal_missingTargetBooks_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reading-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                    {"year":2025}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsertGoal_update_returnsUpdatedValue() throws Exception {
        int currentYear = LocalDate.now().getYear();

        // Create goal
        mockMvc.perform(post("/api/v1/reading-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"year":%d,"targetBooks":10}
                """, currentYear)))
                .andExpect(status().isOk());

        // Update same year → upsert should replace
        mockMvc.perform(post("/api/v1/reading-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"year":%d,"targetBooks":30}
                """, currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetBooks").value(30));
    }

    // ── DELETE /reading-goals/{id} ────────────────────────────────

    @Test
    void deleteGoal_withValidToken_returns200() throws Exception {
        int currentYear = LocalDate.now().getYear();

        String response = mockMvc.perform(post("/api/v1/reading-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"year":%d,"targetBooks":15}
                """, currentYear)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Integer goalId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(delete("/api/v1/reading-goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deleteGoal_withoutToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/reading-goals/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteGoal_nonExistingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/reading-goals/999999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}




