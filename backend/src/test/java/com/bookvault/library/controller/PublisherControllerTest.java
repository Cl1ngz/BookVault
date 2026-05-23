package com.bookvault.library.controller;

import com.bookvault.library.repository.PublisherRepository;
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
class PublisherControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired PublisherRepository publisherRepository;

    // ── GET /publishers ───────────────────────────────────────────

    @Test
    void getAllPublishers_isPublic_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/publishers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchPublishers_byName() throws Exception {
        mockMvc.perform(get("/api/v1/publishers?name=Orbit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchPublishers_byOwner() throws Exception {
        mockMvc.perform(get("/api/v1/publishers?owner=Hachette"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchPublishers_byYear() throws Exception {
        mockMvc.perform(get("/api/v1/publishers?year=1990"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET /publishers/{id} ──────────────────────────────────────

    @Test
    void getPublisherById_existingId_returns200WithFields() throws Exception {
        Integer id = publisherRepository.findAll().stream()
                .findFirst()
                .map(p -> p.getId())
                .orElseThrow(() -> new IllegalStateException("No publishers in DB"));

        mockMvc.perform(get("/api/v1/publishers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").isNotEmpty());
    }

    @Test
    void getPublisherById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/publishers/999999999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /publishers/{id}/books ────────────────────────────────

    @Test
    void getBooksByPublisher_existingId_returnsArray() throws Exception {
        Integer id = publisherRepository.findAll().stream()
                .findFirst()
                .map(p -> p.getId())
                .orElseThrow(() -> new IllegalStateException("No publishers in DB"));

        mockMvc.perform(get("/api/v1/publishers/" + id + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getBooksByPublisher_nonExistingPublisher_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/publishers/999999999/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

