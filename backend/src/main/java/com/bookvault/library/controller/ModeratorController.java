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
        String title = body.get("title") instanceof String
                ? ((String) body.get("title")).trim()
                : null;

        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body("title is required");
        }

        Book book = new Book();
        book.setTitle(title);

        Integer authorId = getInteger(body.get("authorId"));
        if (authorId != null) {
            Author author = authorRepository.findById(authorId).orElse(null);
            if (author == null) {
                return ResponseEntity.badRequest().body("Author not found");
            }
            book.setAuthor(author);
        }

        Integer publisherId = getInteger(body.get("publisherId"));
        if (publisherId != null) {
            Publisher publisher = publisherRepository.findById(publisherId).orElse(null);
            if (publisher == null) {
                return ResponseEntity.badRequest().body("Publisher not found");
            }
            book.setPublisher(publisher);
        }

        Integer seriesId = getInteger(body.get("seriesId"));
        if (seriesId != null) {
            Series series = seriesRepository.findById(seriesId).orElse(null);
            if (series == null) {
                return ResponseEntity.badRequest().body("Series not found");
            }
            book.setSeries(series);
        }

        Integer publicationYear = getInteger(body.get("publicationYear"));
        if (publicationYear != null) {
            book.setPublicationYear(publicationYear);
        }

        Integer pageCount = getInteger(body.get("pageCount"));
        if (pageCount != null) {
            book.setPageCount(pageCount);
        }

        if (body.get("mood") instanceof String mood) {
            book.setMood(mood.trim());
        }

        Set<Genre> genres = getGenresFromBody(body.get("genreIds"));
        if (genres != null) {
            book.setGenres(genres);
        }

        return ResponseEntity.ok(bookRepository.save(book));
    }

    /**
     * PUT /api/v1/moderator/books/{id}
     * Edycja istniejącej książki.
     */
    @PutMapping("/books/{id}")
    public ResponseEntity<?> editBook(@PathVariable Integer id,
                                      @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findById(id).orElse(null);

        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        if (body.get("title") instanceof String title) {
            if (title.isBlank()) {
                return ResponseEntity.badRequest().body("title cannot be blank");
            }
            book.setTitle(title.trim());
        }

        if (body.containsKey("authorId")) {
            Integer authorId = getInteger(body.get("authorId"));

            if (authorId == null) {
                book.setAuthor(null);
            } else {
                Author author = authorRepository.findById(authorId).orElse(null);
                if (author == null) {
                    return ResponseEntity.badRequest().body("Author not found");
                }
                book.setAuthor(author);
            }
        }

        if (body.containsKey("publisherId")) {
            Integer publisherId = getInteger(body.get("publisherId"));

            if (publisherId == null) {
                book.setPublisher(null);
            } else {
                Publisher publisher = publisherRepository.findById(publisherId).orElse(null);
                if (publisher == null) {
                    return ResponseEntity.badRequest().body("Publisher not found");
                }
                book.setPublisher(publisher);
            }
        }

        if (body.containsKey("seriesId")) {
            Integer seriesId = getInteger(body.get("seriesId"));

            if (seriesId == null) {
                book.setSeries(null);
            } else {
                Series series = seriesRepository.findById(seriesId).orElse(null);
                if (series == null) {
                    return ResponseEntity.badRequest().body("Series not found");
                }
                book.setSeries(series);
            }
        }

        Integer publicationYear = getInteger(body.get("publicationYear"));
        if (publicationYear != null) {
            book.setPublicationYear(publicationYear);
        }

        Integer pageCount = getInteger(body.get("pageCount"));
        if (pageCount != null) {
            book.setPageCount(pageCount);
        }

        if (body.containsKey("mood")) {
            if (body.get("mood") == null) {
                book.setMood(null);
            } else if (body.get("mood") instanceof String mood) {
                book.setMood(mood.trim());
            }
        }

        if (body.containsKey("genreIds")) {
            Set<Genre> genres = getGenresFromBody(body.get("genreIds"));

            if (genres == null) {
                return ResponseEntity.badRequest().body("genreIds must be a list of numbers");
            }

            book.setGenres(genres);
        }

        return ResponseEntity.ok(bookRepository.save(book));
    }

    // ── Authors ───────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/moderator/authors
     * Lista autorów dla panelu moderatora.
     */
    @GetMapping("/authors")
    public ResponseEntity<List<Author>> getAllAuthors() {
        return ResponseEntity.ok(authorRepository.findAll());
    }

    /**
     * PUT /api/v1/moderator/authors/{id}
     * Edycja autora.
     */
    @PutMapping("/authors/{id}")
    public ResponseEntity<?> editAuthor(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> body) {
        Author author = authorRepository.findById(id).orElse(null);

        if (author == null) {
            return ResponseEntity.notFound().build();
        }

        if (body.get("firstName") instanceof String firstName) {
            author.setFirstName(firstName.trim());
        }

        if (body.get("lastName") instanceof String lastName) {
            author.setLastName(lastName.trim());
        }

        if (body.containsKey("biography")) {
            author.setBiography((String) body.get("biography"));
        }

        if (body.containsKey("nationality")) {
            author.setNationality((String) body.get("nationality"));
        }

        return ResponseEntity.ok(authorRepository.save(author));
    }

    // ── Series ────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/moderator/series
     * Dodanie nowej serii.
     */
    @PostMapping("/series")
    public ResponseEntity<?> addSeries(@RequestBody Map<String, Object> body) {
        String name = body.get("name") instanceof String
                ? ((String) body.get("name")).trim()
                : null;

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("name is required");
        }

        Series series = new Series();
        series.setName(name);

        Integer volumeCount = getInteger(body.get("volumeCount"));
        if (volumeCount != null) {
            series.setVolumeCount(volumeCount.shortValue());
        }

        Integer authorId = getInteger(body.get("authorId"));
        if (authorId != null) {
            Author author = authorRepository.findById(authorId).orElse(null);
            if (author == null) {
                return ResponseEntity.badRequest().body("Author not found");
            }
            series.setAuthor(author);
        }

        return ResponseEntity.ok(seriesRepository.save(series));
    }

    /**
     * PUT /api/v1/moderator/series/{id}
     * Edycja istniejącej serii.
     */
    @PutMapping("/series/{id}")
    public ResponseEntity<?> editSeries(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> body) {
        Series series = seriesRepository.findById(id).orElse(null);

        if (series == null) {
            return ResponseEntity.notFound().build();
        }

        if (body.get("name") instanceof String name) {
            if (name.isBlank()) {
                return ResponseEntity.badRequest().body("name cannot be blank");
            }
            series.setName(name.trim());
        }

        Integer volumeCount = getInteger(body.get("volumeCount"));
        if (volumeCount != null) {
            series.setVolumeCount(volumeCount.shortValue());
        }

        if (body.containsKey("authorId")) {
            Integer authorId = getInteger(body.get("authorId"));

            if (authorId == null) {
                series.setAuthor(null);
            } else {
                Author author = authorRepository.findById(authorId).orElse(null);
                if (author == null) {
                    return ResponseEntity.badRequest().body("Author not found");
                }
                series.setAuthor(author);
            }
        }

        return ResponseEntity.ok(seriesRepository.save(series));
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/moderator/reports?status=pending
     * Lista zgłoszeń recenzji, opcjonalnie filtrowana po statusie.
     */
    @GetMapping("/reports")
    public ResponseEntity<List<ReviewReport>> getReports(
            @RequestParam(required = false) String status
    ) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(reportRepository.findByStatus(status.trim()));
        }

        return ResponseEntity.ok(reportRepository.findAll());
    }

    /**
     * PUT /api/v1/moderator/reports/{id}/resolve
     * Oznacza zgłoszenie jako rozwiązane.
     */
    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Integer id) {
        ReviewReport report = reportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        report.setStatus("resolved");

        return ResponseEntity.ok(reportRepository.save(report));
    }

    /**
     * PUT /api/v1/moderator/reports/{id}/dismiss
     * Oznacza zgłoszenie jako odrzucone.
     */
    @PutMapping("/reports/{id}/dismiss")
    public ResponseEntity<?> dismissReport(@PathVariable Integer id) {
        ReviewReport report = reportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        report.setStatus("dismissed");

        return ResponseEntity.ok(reportRepository.save(report));
    }

    /**
     * DELETE /api/v1/moderator/reviews/{id}
     * Usuwa recenzję, np. po uznaniu zgłoszenia za zasadne.
     */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<ReviewReport> reports = reportRepository.findByReview_Id(id);
        reports.forEach(r -> r.setStatus("resolved"));
        reportRepository.saveAll(reports);

        reviewRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }

    // ── Helper methods ───────────────────────────────────────────────────────

    private Integer getInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    private Set<Genre> getGenresFromBody(Object value) {
        if (value == null) {
            return new HashSet<>();
        }

        if (!(value instanceof List<?> rawList)) {
            return null;
        }

        Set<Integer> genreIds = new HashSet<>();

        for (Object item : rawList) {
            Integer genreId = getInteger(item);

            if (genreId == null) {
                return null;
            }

            genreIds.add(genreId);
        }

        return new HashSet<>(genreRepository.findAllById(genreIds));
    }
}