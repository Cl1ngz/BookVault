package com.bookvault.library.service;

import com.bookvault.library.dto.BookDto;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;

    public List<Book> getAllBooks(String title, String genre, String series, String author) {
        if (title != null && !title.isBlank()) {
            return bookRepository.findByTitleContainingIgnoreCase(title.trim());
        }
        if (genre != null && !genre.isBlank()) {
            return bookRepository.findDistinctByGenres_NameContainingIgnoreCase(genre.trim());
        }
        if (series != null && !series.isBlank()) {
            return bookRepository.findBySeries_NameContainingIgnoreCase(series.trim());
        }
        if (author != null && !author.isBlank()) {
            return bookRepository.findByAuthor_LastNameContainingIgnoreCase(author.trim());
        }
        return bookRepository.findAll();
    }

    public ResponseEntity<Book> getBookById(Integer id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Book> createBook(BookDto bookDto) {
        Book book = new Book();
        fillBookFromDto(book, bookDto);
        return ResponseEntity.ok(bookRepository.save(book));
    }

    public ResponseEntity<Book> updateBook(Integer id, BookDto bookDto) {
        return bookRepository.findById(id)
                .map(book -> {
                    fillBookFromDto(book, bookDto);
                    return ResponseEntity.ok(bookRepository.save(book));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteBook(Integer id) {
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
                    .orElseThrow(() -> new IllegalArgumentException("Author not found with ID: " + bookDto.getAuthorId()));
            book.setAuthor(author);
        } else {
            book.setAuthor(null);
        }

        if (bookDto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                    .orElseThrow(() -> new IllegalArgumentException("Publisher not found with ID: " + bookDto.getPublisherId()));
            book.setPublisher(publisher);
        } else {
            book.setPublisher(null);
        }

        if (bookDto.getSeriesId() != null) {
            Series series = seriesRepository.findById(bookDto.getSeriesId())
                    .orElseThrow(() -> new IllegalArgumentException("Series not found with ID: " + bookDto.getSeriesId()));
            book.setSeries(series);
        } else {
            book.setSeries(null);
        }

        Set<Genre> genres = new HashSet<>();
        if (bookDto.getGenreIds() != null) {
            for (Integer genreId : bookDto.getGenreIds()) {
                Genre genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new IllegalArgumentException("Genre not found with ID: " + genreId));
                genres.add(genre);
            }
        }
        book.setGenres(genres);
    }
}

