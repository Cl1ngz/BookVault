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
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres(
            @RequestParam(required = false) String name
    ) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(genreRepository.findByNameContainingIgnoreCase(name.trim()));
        }
        return ResponseEntity.ok(genreRepository.findAll());
    }

    /** Returns all genres sorted by number of books descending (most popular first). */
    @GetMapping("/popular")
    public ResponseEntity<List<Genre>> getGenresPopular() {
        return ResponseEntity.ok(genreRepository.findAllOrderByBookCountDesc());
    }
}