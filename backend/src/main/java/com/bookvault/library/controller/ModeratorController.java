package com.bookvault.library.controller;

import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MODERATOR')")
public class ModeratorController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;
    private final ReviewReportRepository reportRepository;
    private final ReviewRepository reviewRepository;

    // ── Books ────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/moderator/books
     * Body: {
     *   "title": "...",
     *   "authorId": 1,
     *   "publisherId": 1,
     *   "seriesId": 1,
     *   "publicationYear": 2024,
     *   "pageCount": 300,
     *   "mood": "dark",
     *   "genreIds": [1, 2]
     * }
     */
    @PostMapping("/books")
    public ResponseEntity<?> addBook(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body("title is required");
        }

        Book book = new Book();
        book.setTitle(title.trim());

        if (body.get("authorId") != null) {
            authorRepository.findById((Integer) body.get("authorId"))
                    .ifPresent(book::setAuthor);
        }
        if (body.get("publisherId") != null) {
            publisherRepository.findById((Integer) body.get("publisherId"))
                    .ifPresent(book::setPublisher);
        }
        if (body.get("seriesId") != null) {
            seriesRepository.findById((Integer) body.get("seriesId"))
                    .ifPresent(book::setSeries);
        }
        if (body.get("publicationYear") != null) {
            book.setPublicationYear((Integer) body.get("publicationYear"));
        }
        if (body.get("pageCount") != null) {
            book.setPageCount((Integer) body.get("pageCount"));
        }
        if (body.get("mood") != null) {
            book.setMood((String) body.get("mood"));
        }

        @SuppressWarnings("unchecked")
        List<Integer> genreIds = (List<Integer>) body.get("genreIds");
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(genreIds));
            book.setGenres(genres);
        }

        return ResponseEntity.ok(bookRepository.save(book));
    }

    /** PUT /api/v1/moderator/books/{id} — edit an existing book */
    @PutMapping("/books/{id}")
    public ResponseEntity<?> editBook(@PathVariable Integer id,
                                      @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();

        if (body.get("title") != null) book.setTitle(((String) body.get("title")).trim());
        if (body.containsKey("authorId")) {
            Integer aid = (Integer) body.get("authorId");
            book.setAuthor(aid == null ? null : authorRepository.findById(aid).orElse(null));
        }
        if (body.containsKey("publisherId")) {
            Integer pid = (Integer) body.get("publisherId");
            book.setPublisher(pid == null ? null : publisherRepository.findById(pid).orElse(null));
        }
        if (body.containsKey("seriesId")) {
            Integer sid = (Integer) body.get("seriesId");
            book.setSeries(sid == null ? null : seriesRepository.findById(sid).orElse(null));
        }
        if (body.get("publicationYear") != null) book.setPublicationYear((Integer) body.get("publicationYear"));
        if (body.get("pageCount") != null) book.setPageCount((Integer) body.get("pageCount"));
        if (body.containsKey("mood")) book.setMood((String) body.get("mood"));
        @SuppressWarnings("unchecked")
        List<Integer> genreIds = (List<Integer>) body.get("genreIds");
        if (genreIds != null) {
            book.setGenres(new HashSet<>(genreRepository.findAllById(genreIds)));
        }
        return ResponseEntity.ok(bookRepository.save(book));
    }

    // ── Authors ───────────────────────────────────────────────────────────────

    /** GET /api/v1/moderator/authors — list all authors (for the edit picker) */
    @GetMapping("/authors")
    public ResponseEntity<List<Author>> getAllAuthors() {
        return ResponseEntity.ok(authorRepository.findAll());
    }

    /** PUT /api/v1/moderator/authors/{id} — edit author bio / fields */
    @PutMapping("/authors/{id}")
    public ResponseEntity<?> editAuthor(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> body) {
        Author author = authorRepository.findById(id).orElse(null);
        if (author == null) return ResponseEntity.notFound().build();

        if (body.get("firstName") != null) author.setFirstName((String) body.get("firstName"));
        if (body.get("lastName") != null) author.setLastName((String) body.get("lastName"));
        if (body.containsKey("biography")) author.setBiography((String) body.get("biography"));
        if (body.containsKey("nationality")) author.setNationality((String) body.get("nationality"));
        return ResponseEntity.ok(authorRepository.save(author));
    }

    // ── Series ────────────────────────────────────────────────────────────────

    /** POST /api/v1/moderator/series — add a new series */
    @PostMapping("/series")
    public ResponseEntity<?> addSeries(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) return ResponseEntity.badRequest().body("name is required");

        Series s = new Series();
        s.setName(name.trim());
        if (body.get("volumeCount") != null) s.setVolumeCount(((Integer) body.get("volumeCount")).shortValue());
        if (body.get("authorId") != null) {
            authorRepository.findById((Integer) body.get("authorId")).ifPresent(s::setAuthor);
        }
        return ResponseEntity.ok(seriesRepository.save(s));
    }

    /** PUT /api/v1/moderator/series/{id} — edit an existing series */
    @PutMapping("/series/{id}")
    public ResponseEntity<?> editSeries(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> body) {
        Series s = seriesRepository.findById(id).orElse(null);
        if (s == null) return ResponseEntity.notFound().build();

        if (body.get("name") != null) s.setName(((String) body.get("name")).trim());
        if (body.get("volumeCount") != null) s.setVolumeCount(((Integer) body.get("volumeCount")).shortValue());
        if (body.containsKey("authorId")) {
            Integer aid = (Integer) body.get("authorId");
            s.setAuthor(aid == null ? null : authorRepository.findById(aid).orElse(null));
        }
        return ResponseEntity.ok(seriesRepository.save(s));
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/moderator/reports?status=pending
     * Returns all reports (optionally filtered by status).
     */
    @GetMapping("/reports")
    public ResponseEntity<List<ReviewReport>> getReports(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(reportRepository.findByStatus(status));
        }
        return ResponseEntity.ok(reportRepository.findAll());
    }

    /**
     * PUT /api/v1/moderator/reports/{id}/resolve
     * Marks the report as resolved.
     */
    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Integer id) {
        ReviewReport report = reportRepository.findById(id).orElse(null);
        if (report == null) return ResponseEntity.notFound().build();
        report.setStatus("resolved");
        return ResponseEntity.ok(reportRepository.save(report));
    }

    /**
     * PUT /api/v1/moderator/reports/{id}/dismiss
     * Marks the report as dismissed (not valid).
     */
    @PutMapping("/reports/{id}/dismiss")
    public ResponseEntity<?> dismissReport(@PathVariable Integer id) {
        ReviewReport report = reportRepository.findById(id).orElse(null);
        if (report == null) return ResponseEntity.notFound().build();
        report.setStatus("dismissed");
        return ResponseEntity.ok(reportRepository.save(report));
    }

    /**
     * DELETE /api/v1/moderator/reviews/{id}
     * Deletes a review entirely (e.g. after it was deemed inappropriate).
     */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        if (!reviewRepository.existsById(id)) return ResponseEntity.notFound().build();
        // close all reports for this review first
        List<ReviewReport> reports = reportRepository.findByReview_Id(id);
        reports.forEach(r -> r.setStatus("resolved"));
        reportRepository.saveAll(reports);
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }
}

