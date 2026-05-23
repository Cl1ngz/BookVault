package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.BookRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReaderRepository readerRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtils jwtUtils;
    @Autowired BookRepository bookRepository;

    private String token;
    private Integer bookId;

    @BeforeEach
    void setup() {
        Reader reader = readerRepository.findByEmail("reviewtest@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("reviewtestuser");
            r.setEmail("reviewtest@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            return readerRepository.save(r);
        });
        token = jwtUtils.generateToken(reader.getEmail(), reader.getId(), reader.getUsername(), reader.getRole());

        bookId = bookRepository.findAll().stream()
                .findFirst()
                .map(b -> b.getId())
                .orElse(1);
    }

    // ── GET /reviews ──────────────────────────────────────────────

    @Test
    void getAllReviews_isPublic_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getReviewsByBookId_isPublic_returnsArray() throws Exception {
        mockMvc.perform(get("/api/v1/reviews?bookId=" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getReviewsByRating_isPublic_returnsArray() throws Exception {
        mockMvc.perform(get("/api/v1/reviews?rating=5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── POST /reviews ─────────────────────────────────────────────

    @Test
    void addReview_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                    {"bookId":%d,"rating":4.0,"content":"No auth test"}
                """, bookId)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addReview_withValidToken_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d,"rating":4.0,"content":"Great read!"}
                """, bookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.0))
                .andExpect(jsonPath("$.content").value("Great read!"));
    }

    @Test
    void addReview_missingRating_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d,"content":"Missing rating"}
                """, bookId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addReview_missingBookId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                    {"rating":3.0,"content":"Missing bookId"}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addReview_ratingOutOfRange_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d,"rating":10.0}
                """, bookId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addReview_nonExistingBook_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                    {"bookId":999999999,"rating":4.0}
                """))
                .andExpect(status().isNotFound());
    }
}




