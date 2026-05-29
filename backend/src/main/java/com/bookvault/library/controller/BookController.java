package com.bookvault.library.controller;

import com.bookvault.library.dto.BookDto;
import com.bookvault.library.model.Author;
import com.bookvault.library.model.Book;
import com.bookvault.library.model.Genre;
import com.bookvault.library.model.Publisher;
import com.bookvault.library.model.Series;
import com.bookvault.library.repository.AuthorRepository;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.GenreRepository;
import com.bookvault.library.repository.PublisherRepository;
import com.bookvault.library.repository.SeriesRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String author
    ) {
        if (title != null && !title.isBlank()) {
            return ResponseEntity.ok(bookRepository.findByTitleContainingIgnoreCase(title.trim()));
        }

        if (genre != null && !genre.isBlank()) {
            return ResponseEntity.ok(bookRepository.findDistinctByGenres_NameContainingIgnoreCase(genre.trim()));
        }

        if (series != null && !series.isBlank()) {
            return ResponseEntity.ok(bookRepository.findBySeries_NameContainingIgnoreCase(series.trim()));
        }

        if (author != null && !author.isBlank()) {
            return ResponseEntity.ok(bookRepository.findByAuthor_LastNameContainingIgnoreCase(author.trim()));
        }

        return ResponseEntity.ok(bookRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookDto bookDto) {
        Book book = new Book();

        fillBookFromDto(book, bookDto);

        Book savedBook = bookRepository.save(book);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Integer id,
            @Valid @RequestBody BookDto bookDto
    ) {
        return bookRepository.findById(id)
                .map(book -> {
                    fillBookFromDto(book, bookDto);
                    Book updatedBook = bookRepository.save(book);
                    return ResponseEntity.ok(updatedBook);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void fillBookFromDto(Book book, BookDto bookDto) {
        book.setTitle(bookDto.getTitle());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPageCount(bookDto.getPageCount());
        book.setMood(bookDto.getMood());

        if (bookDto.getAuthorId() != null) {
            Author author = authorRepository.findById(bookDto.getAuthorId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono autora o ID: " + bookDto.getAuthorId()));
            book.setAuthor(author);
        } else {
            book.setAuthor(null);
        }

        if (bookDto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wydawnictwa o ID: " + bookDto.getPublisherId()));
            book.setPublisher(publisher);
        } else {
            book.setPublisher(null);
        }

        if (bookDto.getSeriesId() != null) {
            Series series = seriesRepository.findById(bookDto.getSeriesId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono serii o ID: " + bookDto.getSeriesId()));
            book.setSeries(series);
        } else {
            book.setSeries(null);
        }

        Set<Genre> genres = new HashSet<>();

        if (bookDto.getGenreIds() != null) {
            for (Integer genreId : bookDto.getGenreIds()) {
                Genre genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono gatunku o ID: " + genreId));
                genres.add(genre);
            }
        }

        book.setGenres(genres);
    }
}