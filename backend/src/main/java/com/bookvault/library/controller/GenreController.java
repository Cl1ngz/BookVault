package com.bookvault.library.controller;

import com.bookvault.library.dto.GenreDto;
import com.bookvault.library.model.Genre;
import com.bookvault.library.repository.GenreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable Integer id) {
        return genreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createGenre(@Valid @RequestBody GenreDto genreDto) {
        String genreName = genreDto.getName().trim();

        if (genreRepository.findByNameIgnoreCase(genreName).isPresent()) {
            return ResponseEntity.badRequest().body("Gatunek o takiej nazwie już istnieje");
        }

        Genre genre = new Genre();
        genre.setName(genreName);

        Genre savedGenre = genreRepository.save(genre);
        return ResponseEntity.ok(savedGenre);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenre(
            @PathVariable Integer id,
            @Valid @RequestBody GenreDto genreDto
    ) {
        return genreRepository.findById(id)
                .map(genre -> {
                    String genreName = genreDto.getName().trim();

                    if (genreRepository.findByNameIgnoreCase(genreName).isPresent()
                            && !genre.getName().equalsIgnoreCase(genreName)) {
                        return ResponseEntity.badRequest().body("Gatunek o takiej nazwie już istnieje");
                    }

                    genre.setName(genreName);

                    Genre updatedGenre = genreRepository.save(genre);
                    return ResponseEntity.ok(updatedGenre);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Integer id) {
        if (!genreRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        genreRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}