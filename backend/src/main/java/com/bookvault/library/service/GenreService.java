package com.bookvault.library.service;

import com.bookvault.library.dto.GenreDto;
import com.bookvault.library.model.Genre;
import com.bookvault.library.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<Genre> getAllGenres(String name) {
        if (name != null && !name.isBlank()) {
            return genreRepository.findByNameContainingIgnoreCase(name.trim());
        }
        return genreRepository.findAll();
    }

    public ResponseEntity<Genre> getGenreById(Integer id) {
        return genreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> createGenre(GenreDto genreDto) {
        String genreName = genreDto.getName().trim();
        if (genreRepository.findByNameIgnoreCase(genreName).isPresent()) {
            return ResponseEntity.badRequest().body("A genre with that name already exists");
        }
        Genre genre = new Genre();
        genre.setName(genreName);
        return ResponseEntity.ok(genreRepository.save(genre));
    }

    public ResponseEntity<?> updateGenre(Integer id, GenreDto genreDto) {
        return genreRepository.findById(id)
                .map(genre -> {
                    String genreName = genreDto.getName().trim();
                    if (genreRepository.findByNameIgnoreCase(genreName).isPresent()
                            && !genre.getName().equalsIgnoreCase(genreName)) {
                        return ResponseEntity.badRequest().body("A genre with that name already exists");
                    }
                    genre.setName(genreName);
                    return ResponseEntity.ok(genreRepository.save(genre));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteGenre(Integer id) {
        if (!genreRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        genreRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

