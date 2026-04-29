package com.bookvault.library.controller;

import com.bookvault.library.model.Series;
import com.bookvault.library.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesRepository seriesRepository;

    @GetMapping
    public ResponseEntity<List<Series>> getAllSeries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String authorLastName
    ) {
        // Search by series name
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(seriesRepository.findByNameContainingIgnoreCase(name.trim()));
        }

        // Search by autor
        if (authorLastName != null && !authorLastName.isBlank()) {
            return ResponseEntity.ok(seriesRepository.findByAuthor_LastNameContainingIgnoreCase(authorLastName.trim()));
        }

        // return everythin
        return ResponseEntity.ok(seriesRepository.findAll());
    }
}