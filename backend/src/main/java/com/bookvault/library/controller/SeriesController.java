package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.model.Series;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesRepository seriesRepository;
    private final BookRepository bookRepository;

    @GetMapping
    public ResponseEntity<List<Series>> getAllSeries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String authorLastName
    ) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(seriesRepository.findByNameContainingIgnoreCase(name.trim()));
        }
        if (authorLastName != null && !authorLastName.isBlank()) {
            return ResponseEntity.ok(seriesRepository.findByAuthor_LastNameContainingIgnoreCase(authorLastName.trim()));
        }
        return ResponseEntity.ok(seriesRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Series> getSeriesById(@PathVariable Integer id) {
        return seriesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getBooksBySeries(@PathVariable Integer id) {
        return ResponseEntity.ok(bookRepository.findBySeries_Id(id));
    }
}