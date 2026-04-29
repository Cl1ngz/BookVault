package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReviewReportRepository;
import com.bookvault.library.repository.ReviewRepository;
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
class ReviewReportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReaderRepository readerRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtils jwtUtils;
    @Autowired ReviewRepository reviewRepository;
    @Autowired ReviewReportRepository reportRepository;  // ← ADDED

    private String token;
    private Integer reviewId;
    private Integer readerId;  // ← ADDED

    @BeforeEach
    void setup() {
        // Create or reuse test reader
        Reader reader = readerRepository.findByEmail("reporttest@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("reporttestuser");
            r.setEmail("reporttest@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            return readerRepository.save(r);
        });
        readerId = reader.getId();  // ← ADDED
        token = jwtUtils.generateToken(reader.getEmail(), reader.getId(), reader.getUsername());

        // Get any existing review ID
        reviewId = reviewRepository.findAll().stream()
                .findFirst()
                .map(r -> r.getId())
                .orElse(1);

        // ← ADDED: delete any previous reports from this test reader on this review
        reportRepository.findByReview_Id(reviewId).stream()
                .filter(rep -> rep.getReporterId().equals(readerId))
                .forEach(reportRepository::delete);
    }

    @Test
    void report_withoutToken_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"reviewId":1,"reason":"spam"}
                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void report_withInvalidToken_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer totally.invalid.token")
                        .content("""
                    {"reviewId":1,"reason":"spam"}
                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void report_withValidToken_success() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"reviewId":%d,"reason":"spam test"}
                """, reviewId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reporterType").value("reader"))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.reason").value("spam test"));
    }

    @Test
    void report_missingReason_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"reviewId":%d}
                """, reviewId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReports_requiresAuth_returns403WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/reports?status=pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReports_withValidToken_returnsArray() throws Exception {
        mockMvc.perform(get("/api/v1/reports?status=pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
