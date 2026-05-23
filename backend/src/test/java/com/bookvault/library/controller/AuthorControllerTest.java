package com.bookvault.library.controller;

import com.bookvault.library.model.Author;
import com.bookvault.library.repository.AuthorRepository;
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
class AuthorControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AuthorRepository authorRepository;

    // ── GET /authors ──────────────────────────────────────────────

    @Test
    void getAllAuthors_isPublic_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchAuthors_byLastName() throws Exception {
        mockMvc.perform(get("/api/v1/authors?lastName=Tolkien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchAuthors_byFirstName() throws Exception {
        mockMvc.perform(get("/api/v1/authors?firstName=Terry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchAuthors_byNationality() throws Exception {
        mockMvc.perform(get("/api/v1/authors?nationality=British"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET /authors/{id} ─────────────────────────────────────────

    @Test
    void getAuthorById_existingId_returns200WithFields() throws Exception {
        Integer id = authorRepository.findAll().stream()
                .findFirst()
                .map(Author::getId)
                .orElseThrow(() -> new IllegalStateException("No authors in DB"));

        mockMvc.perform(get("/api/v1/authors/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.lastName").isNotEmpty());
    }

    @Test
    void getAuthorById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/authors/999999999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /authors/{id}/books ───────────────────────────────────

    @Test
    void getBooksByAuthor_existingId_returnsArray() throws Exception {
        Integer id = authorRepository.findAll().stream()
                .findFirst()
                .map(Author::getId)
                .orElseThrow(() -> new IllegalStateException("No authors in DB"));

        mockMvc.perform(get("/api/v1/authors/" + id + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getBooksByAuthor_nonExistingId_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/authors/999999999/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

