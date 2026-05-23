package com.bookvault.library.controller;

import com.bookvault.library.model.Series;
import com.bookvault.library.repository.SeriesRepository;
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
class SeriesControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired SeriesRepository seriesRepository;

    // ── GET /series ───────────────────────────────────────────────

    @Test
    void getAllSeries_isPublic_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/series"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchSeries_byName() throws Exception {
        mockMvc.perform(get("/api/v1/series?name=Discworld"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchSeries_byAuthorLastName() throws Exception {
        mockMvc.perform(get("/api/v1/series?authorLastName=Pratchett"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET /series/{id} ──────────────────────────────────────────

    @Test
    void getSeriesById_existingId_returns200WithFields() throws Exception {
        Integer id = seriesRepository.findAll().stream()
                .findFirst()
                .map(Series::getId)
                .orElseThrow(() -> new IllegalStateException("No series in DB"));

        mockMvc.perform(get("/api/v1/series/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").isNotEmpty());
    }

    @Test
    void getSeriesById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/series/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSeriesById_responseContainsDescriptionField() throws Exception {
        // description may be null but the key must be present in JSON
        Integer id = seriesRepository.findAll().stream()
                .findFirst()
                .map(Series::getId)
                .orElseThrow(() -> new IllegalStateException("No series in DB"));

        mockMvc.perform(get("/api/v1/series/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void getSeriesById_seriesWithDescription_returnsDescriptionValue() throws Exception {
        // Find a series that has a non-null description (seeded via 21_series_description.sql)
        seriesRepository.findAll().stream()
                .filter(s -> s.getDescription() != null && !s.getDescription().isBlank())
                .findFirst()
                .ifPresent(s -> {
                    try {
                        mockMvc.perform(get("/api/v1/series/" + s.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.description").value(s.getDescription()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ── GET /series/{id}/books ────────────────────────────────────

    @Test
    void getBooksBySeries_existingId_returnsArray() throws Exception {
        Integer id = seriesRepository.findAll().stream()
                .findFirst()
                .map(Series::getId)
                .orElseThrow(() -> new IllegalStateException("No series in DB"));

        mockMvc.perform(get("/api/v1/series/" + id + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getBooksBySeries_nonExistingId_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/series/999999999/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

