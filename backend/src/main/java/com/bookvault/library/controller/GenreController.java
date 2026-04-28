package com.bookvault.library.controller;

import com.bookvault.library.model.Genre;
import com.bookvault.library.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres") // Liczba pojedyncza - konsekwentnie!
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres(
            @RequestParam(required = false) String name
    ) {
        // Jeśli podano nazwę, szukamy fragmentu (np. "fant" -> "Fantasy")
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(genreRepository.findByNameContainingIgnoreCase(name.trim()));
        }

        // Domyślnie zwracamy wszystkie gatunki
        return ResponseEntity.ok(genreRepository.findAll());
    }
}