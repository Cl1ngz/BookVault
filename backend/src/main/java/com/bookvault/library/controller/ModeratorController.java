package com.bookvault.library.controller;

import com.bookvault.library.dto.AuthorUpdateDto;
import com.bookvault.library.dto.BookDto;
import com.bookvault.library.dto.BookUpdateDto;
import com.bookvault.library.dto.SeriesDto;
import com.bookvault.library.dto.SeriesUpdateDto;
import com.bookvault.library.model.Author;
import com.bookvault.library.model.ReviewReport;
import com.bookvault.library.service.ModeratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MODERATOR')")
public class ModeratorController {

    private final ModeratorService moderatorService;

    @PostMapping("/books")
    public ResponseEntity<?> addBook(@Valid @RequestBody BookDto bookDto) {
        return moderatorService.addBook(bookDto);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<?> editBook(
            @PathVariable Integer id,
            @Valid @RequestBody BookUpdateDto bookDto
    ) {
        return moderatorService.editBook(id, bookDto);
    }

    @GetMapping("/authors")
    public ResponseEntity<List<Author>> getAllAuthors() {
        return ResponseEntity.ok(moderatorService.getAllAuthors());
    }

    @PutMapping("/authors/{id}")
    public ResponseEntity<?> editAuthor(
            @PathVariable Integer id,
            @Valid @RequestBody AuthorUpdateDto authorDto
    ) {
        return moderatorService.editAuthor(id, authorDto);
    }

    @PostMapping("/series")
    public ResponseEntity<?> addSeries(@Valid @RequestBody SeriesDto seriesDto) {
        return moderatorService.addSeries(seriesDto);
    }

    @PutMapping("/series/{id}")
    public ResponseEntity<?> editSeries(
            @PathVariable Integer id,
            @Valid @RequestBody SeriesUpdateDto seriesDto
    ) {
        return moderatorService.editSeries(id, seriesDto);
    }


    @GetMapping("/reports")
    public ResponseEntity<List<ReviewReport>> getReports(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(moderatorService.getReports(status));
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Integer id) {
        return moderatorService.resolveReport(id);
    }

    @PutMapping("/reports/{id}/dismiss")
    public ResponseEntity<?> dismissReport(@PathVariable Integer id) {
        return moderatorService.dismissReport(id);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        return moderatorService.deleteReview(id);
    }
}