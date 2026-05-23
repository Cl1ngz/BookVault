package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModeratorControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReaderRepository readerRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtils jwtUtils;

    private String userToken;
    private String moderatorToken;

    @BeforeEach
    void setup() {
        // Regular user
        Reader user = readerRepository.findByEmail("modtest_user@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("modtestuser");
            r.setEmail("modtest_user@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            r.setRole("USER");
            return readerRepository.save(r);
        });
        userToken = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getUsername(), user.getRole());

        // Moderator
        Reader mod = readerRepository.findByEmail("modtest_mod@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("modtestmoderator");
            r.setEmail("modtest_mod@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            r.setRole("MODERATOR");
            return readerRepository.save(r);
        });
        // Ensure role is up to date in case previous run left it as USER
        if (!"MODERATOR".equals(mod.getRole())) {
            mod.setRole("MODERATOR");
            mod = readerRepository.save(mod);
        }
        moderatorToken = jwtUtils.generateToken(mod.getEmail(), mod.getId(), mod.getUsername(), mod.getRole());
    }

    // ── Access control ────────────────────────────────────────────

    @Test
    void getAuthors_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/moderator/authors"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuthors_withUserToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/moderator/authors")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuthors_withModeratorToken_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/moderator/authors")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getReports_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/moderator/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReports_withUserToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/moderator/reports")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReports_withModeratorToken_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/moderator/reports")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── Book CRUD via moderator ───────────────────────────────────

    @Test
    void addBook_withUserToken_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/moderator/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content("""
                    {"title":"Test Book from User"}
                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void addBook_withModeratorToken_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/moderator/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + moderatorToken)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBook_withModeratorToken_validTitle_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/moderator/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + moderatorToken)
                        .content("""
                    {"title":"Moderator Test Book"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Moderator Test Book"));
    }

    // ── Series CRUD via moderator ─────────────────────────────────

    @Test
    void addSeries_withModeratorToken_validName_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/moderator/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + moderatorToken)
                        .content("""
                    {"name":"Test Series from Moderator","volumeCount":5}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Series from Moderator"));
    }

    @Test
    void addSeries_withModeratorToken_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/moderator/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + moderatorToken)
                        .content("""
                    {"volumeCount":3}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSeries_withUserToken_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/moderator/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content("""
                    {"name":"Unauthorized Series"}
                """))
                .andExpect(status().isForbidden());
    }

    // ── Report resolution ─────────────────────────────────────────

    @Test
    void resolveReport_withUserToken_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/moderator/reports/1/resolve")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void dismissReport_withUserToken_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/moderator/reports/1/dismiss")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}



