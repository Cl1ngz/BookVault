package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.ReadingLog;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.JournalEntryRepository;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReadingLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReadingLogControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReaderRepository readerRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtils jwtUtils;
    @Autowired BookRepository bookRepository;
    @Autowired ReadingLogRepository readingLogRepository;
    @Autowired JournalEntryRepository journalEntryRepository;

    private String token;
    private Integer readerId;
    private Integer bookId;

    @BeforeEach
    void setup() {
        Reader reader = readerRepository.findByEmail("shelftest@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("shelftestuser");
            r.setEmail("shelftest@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            return readerRepository.save(r);
        });
        readerId = reader.getId();
        token = jwtUtils.generateToken(reader.getEmail(), reader.getId(), reader.getUsername(), reader.getRole());

        bookId = bookRepository.findAll().stream()
                .findFirst()
                .map(b -> b.getId())
                .orElse(1);

        // Clean up any leftover test shelf entries
        List<ReadingLog> existing = readingLogRepository.findByReader_Id(readerId);
        existing.forEach(log -> {
            journalEntryRepository.findByReadingLog_IdOrderByEntryDateDescCreatedAtDesc(log.getId())
                    .forEach(journalEntryRepository::delete);
            readingLogRepository.delete(log);
        });
    }

    // ── GET /reading-log ──────────────────────────────────────────

    @Test
    void getShelf_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reading-log"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getShelf_withToken_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reading-log")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getShelf_filteredByStatus_returnsArray() throws Exception {
        mockMvc.perform(get("/api/v1/reading-log?status=TO_READ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getShelf_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/reading-log?status=INVALID_STATUS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ── POST /reading-log ─────────────────────────────────────────

    @Test
    void addToShelf_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                    {"bookId":%d}
                """, bookId)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addToShelf_withValidToken_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d,"status":"TO_READ"}
                """, bookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void addToShelf_duplicate_returns409() throws Exception {
        // First add
        mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d}
                """, bookId)))
                .andExpect(status().isOk());

        // Duplicate
        mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d}
                """, bookId)))
                .andExpect(status().isConflict());
    }

    @Test
    void addToShelf_missingBookId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addToShelf_nonExistingBook_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                    {"bookId":999999999}
                """))
                .andExpect(status().isNotFound());
    }

    // ── PUT /reading-log/{id} ─────────────────────────────────────

    @Test
    void updateShelfEntry_withValidToken_returns200() throws Exception {
        // Create an entry first
        String response = mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d,"status":"TO_READ"}
                """, bookId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        Integer logId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(put("/api/v1/reading-log/" + logId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                    {"status":"READING"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READING"));
    }

    // ── DELETE /reading-log/{id} ──────────────────────────────────

    @Test
    void removeFromShelf_withValidToken_returns200() throws Exception {
        String response = mockMvc.perform(post("/api/v1/reading-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("""
                    {"bookId":%d}
                """, bookId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Integer logId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(delete("/api/v1/reading-log/" + logId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void removeFromShelf_withoutToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/reading-log/1"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /reading-log/stats ────────────────────────────────────

    @Test
    void getStats_withToken_returns200WithKeys() throws Exception {
        mockMvc.perform(get("/api/v1/reading-log/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toRead").isNumber())
                .andExpect(jsonPath("$.reading").isNumber())
                .andExpect(jsonPath("$.finished").isNumber())
                .andExpect(jsonPath("$.dnf").isNumber())
                .andExpect(jsonPath("$.finishedThisYear").isNumber());
    }

    @Test
    void getStats_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reading-log/stats"))
                .andExpect(status().isUnauthorized());
    }
}


