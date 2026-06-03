package com.bookvault.library.service;

import com.bookvault.library.dto.AuthorUpdateDto;
import com.bookvault.library.dto.BookDto;
import com.bookvault.library.dto.BookUpdateDto;
import com.bookvault.library.dto.SeriesDto;
import com.bookvault.library.dto.SeriesUpdateDto;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ModeratorService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;
    private final ReviewReportRepository reportRepository;
    private final ReviewRepository reviewRepository;


    public ResponseEntity<?> addBook(BookDto bookDto) {
        Book book = new Book();

        book.setTitle(bookDto.getTitle());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPageCount(bookDto.getPageCount());
        book.setMood(bookDto.getMood());

        if (bookDto.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDto.getAuthorId()).orElse(null);
            if (author == null) return ResponseEntity.badRequest().body("Author not found");
            book.setAuthor(author);
        }

        if (bookDto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(bookDto.getPublisherId()).orElse(null);
            if (publisher == null) return ResponseEntity.badRequest().body("Publisher not found");
            book.setPublisher(publisher);
        }

        if (bookDto.getSeriesId() != null) {
            Series series = seriesRepository.findById(bookDto.getSeriesId()).orElse(null);
            if (series == null) return ResponseEntity.badRequest().body("Series not found");
            book.setSeries(series);
        }

        if (bookDto.getGenreIds() != null && !bookDto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            book.setGenres(genres);
        }

        return ResponseEntity.ok(bookRepository.save(book));
    }

    public ResponseEntity<?> editBook(Integer id, BookUpdateDto bookDto) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();

        if (bookDto.getTitle() != null) {
            book.setTitle(bookDto.getTitle().trim());
        }

        if (bookDto.getPublicationYear() != null) {
            book.setPublicationYear(bookDto.getPublicationYear());
        }

        if (bookDto.getPageCount() != null) {
            book.setPageCount(bookDto.getPageCount());
        }

        if (bookDto.getMood() != null) {
            book.setMood(bookDto.getMood().trim());
        }

        if (bookDto.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDto.getAuthorId()).orElse(null);
            if (author == null) return ResponseEntity.badRequest().body("Author not found");
            book.setAuthor(author);
        }

        if (bookDto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(bookDto.getPublisherId()).orElse(null);
            if (publisher == null) return ResponseEntity.badRequest().body("Publisher not found");
            book.setPublisher(publisher);
        }

        if (bookDto.getSeriesId() != null) {
            Series series = seriesRepository.findById(bookDto.getSeriesId()).orElse(null);
            if (series == null) return ResponseEntity.badRequest().body("Series not found");
            book.setSeries(series);
        }

        if (bookDto.getGenreIds() != null) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            book.setGenres(genres);
        }

        return ResponseEntity.ok(bookRepository.save(book));
    }


    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public ResponseEntity<?> editAuthor(Integer id, AuthorUpdateDto authorDto) {
        Author author = authorRepository.findById(id).orElse(null);
        if (author == null) return ResponseEntity.notFound().build();

        if (authorDto.getFirstName() != null) {
            author.setFirstName(authorDto.getFirstName().trim());
        }

        if (authorDto.getLastName() != null) {
            author.setLastName(authorDto.getLastName().trim());
        }

        if (authorDto.getBirthDate() != null) {
            author.setBirthDate(authorDto.getBirthDate());
        }

        if (authorDto.getBiography() != null) {
            author.setBiography(authorDto.getBiography());
        }

        if (authorDto.getNationality() != null) {
            author.setNationality(authorDto.getNationality().trim());
        }

        if (authorDto.getEmail() != null) {
            author.setEmail(authorDto.getEmail().trim());
        }

        return ResponseEntity.ok(authorRepository.save(author));
    }


    public ResponseEntity<?> addSeries(SeriesDto seriesDto) {
        Series series = new Series();
        series.setName(seriesDto.getName().trim());

        if (seriesDto.getVolumeCount() != null) {
            series.setVolumeCount(seriesDto.getVolumeCount().shortValue());
        }

        if (seriesDto.getAuthorId() != null) {
            Author author = authorRepository.findById(seriesDto.getAuthorId()).orElse(null);
            if (author == null) return ResponseEntity.badRequest().body("Author not found");
            series.setAuthor(author);
        }

        return ResponseEntity.ok(seriesRepository.save(series));
    }

    public ResponseEntity<?> editSeries(Integer id, SeriesUpdateDto seriesDto) {
        Series series = seriesRepository.findById(id).orElse(null);
        if (series == null) return ResponseEntity.notFound().build();

        if (seriesDto.getName() != null) {
            series.setName(seriesDto.getName().trim());
        }

        if (seriesDto.getVolumeCount() != null) {
            series.setVolumeCount(seriesDto.getVolumeCount().shortValue());
        }

        if (seriesDto.getAuthorId() != null) {
            Author author = authorRepository.findById(seriesDto.getAuthorId()).orElse(null);
            if (author == null) return ResponseEntity.badRequest().body("Author not found");
            series.setAuthor(author);
        }

        return ResponseEntity.ok(seriesRepository.save(series));
    }


    public List<ReviewReport> getReports(String status) {
        if (status != null && !status.isBlank()) {
            return reportRepository.findByStatus(status.trim());
        }
        return reportRepository.findAll();
    }

    public ResponseEntity<?> resolveReport(Integer id) {
        ReviewReport report = reportRepository.findById(id).orElse(null);
        if (report == null) return ResponseEntity.notFound().build();

        report.setStatus("resolved");
        return ResponseEntity.ok(reportRepository.save(report));
    }

    public ResponseEntity<?> dismissReport(Integer id) {
        ReviewReport report = reportRepository.findById(id).orElse(null);
        if (report == null) return ResponseEntity.notFound().build();

        report.setStatus("dismissed");
        return ResponseEntity.ok(reportRepository.save(report));
    }

    public ResponseEntity<?> deleteReview(Integer id) {
        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<ReviewReport> reports = reportRepository.findByReview_Id(id);
        reports.forEach(report -> report.setStatus("resolved"));
        reportRepository.saveAll(reports);

        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }
}

