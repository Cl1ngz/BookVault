package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;

    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String series
    ) {
        if (name != null && !name.isBlank()) {
            return bookRepository.findDistinctByTytulContainingIgnoreCase(name.trim());
        }

        if (genre != null && !genre.isBlank()) {
            return bookRepository.findDistinctByGenresNazwaIgnoreCase(genre.trim());
        }

        if (series != null && !series.isBlank()) {
            return bookRepository.findDistinctBySeriesNameContainingIgnoreCase(series.trim());
        }

        return bookRepository.findAll();
    }
}