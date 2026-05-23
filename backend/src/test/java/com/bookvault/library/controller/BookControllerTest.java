package com.bookvault.library.controller;

import com.bookvault.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired BookRepository bookRepository;

    // ── GET /books ────────────────────────────────────────────────

    @Test
    void getAllBooks_isPublic_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getAllBooks_returnsArray() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchBooks_byTitle() throws Exception {
        mockMvc.perform(get("/api/v1/books?title=Solaris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchBooks_byGenre() throws Exception {
        mockMvc.perform(get("/api/v1/books?genre=Fantasy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchBooks_bySeries() throws Exception {
        mockMvc.perform(get("/api/v1/books?series=Discworld"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchBooks_byAuthorLastName() throws Exception {
        mockMvc.perform(get("/api/v1/books?author=Tolkien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchBooks_noTokenRequired() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk());
    }

    // ── GET /books/{id} ───────────────────────────────────────────

    @Test
    void getBookById_existingId_returns200() throws Exception {
        Integer id = bookRepository.findAll().stream()
                .findFirst()
                .map(b -> b.getId())
                .orElse(1);

        mockMvc.perform(get("/api/v1/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    void getBookById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/books/999999999"))
                .andExpect(status().isNotFound());
    }
}
