package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        // s by title
        if (title != null && !title.isBlank()) {
            return ResponseEntity.ok(bookRepository.findByTitleContainingIgnoreCase(title.trim()));
        }

        // s by genere
        if (genre != null && !genre.isBlank()) {
            return ResponseEntity.ok(bookRepository.findDistinctByGenres_NameContainingIgnoreCase(genre.trim()));
        }

        // s by series
        if (series != null && !series.isBlank()) {
            return ResponseEntity.ok(bookRepository.findBySeries_NameContainingIgnoreCase(series.trim()));
        }

        // s by surname
        if (author != null && !author.isBlank()) {
            return ResponseEntity.ok(bookRepository.findByAuthor_LastNameContainingIgnoreCase(author.trim()));
        }

        // return all
        return ResponseEntity.ok(bookRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}