package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String author
    ) {
        // 1. Wyszukiwanie po tytule
        if (title != null && !title.isBlank()) {
            return ResponseEntity.ok(bookRepository.findByTitleContainingIgnoreCase(title.trim()));
        }

        // 2. Wyszukiwanie po gatunku
        if (genre != null && !genre.isBlank()) {
            return ResponseEntity.ok(bookRepository.findDistinctByGenres_NameContainingIgnoreCase(genre.trim()));
        }

        // 3. Wyszukiwanie po serii
        if (series != null && !series.isBlank()) {
            return ResponseEntity.ok(bookRepository.findBySeries_NameContainingIgnoreCase(series.trim()));
        }

        // 4. Wyszukiwanie po nazwisku autora
        if (author != null && !author.isBlank()) {
            return ResponseEntity.ok(bookRepository.findByAuthor_LastNameContainingIgnoreCase(author.trim()));
        }

        // 5. Jeśli brak parametrów, zwróć wszystkie książki
        return ResponseEntity.ok(bookRepository.findAll());
    }
}