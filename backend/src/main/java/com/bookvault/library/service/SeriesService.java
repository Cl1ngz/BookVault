package com.bookvault.library.service;

import com.bookvault.library.model.Book;
import com.bookvault.library.model.Series;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final BookRepository bookRepository;

    public List<Series> getAllSeries(String name, String authorLastName) {
        if (name != null && !name.isBlank()) {
            return seriesRepository.findByNameContainingIgnoreCase(name.trim());
        }
        if (authorLastName != null && !authorLastName.isBlank()) {
            return seriesRepository.findByAuthor_LastNameContainingIgnoreCase(authorLastName.trim());
        }
        return seriesRepository.findAll();
    }

    public ResponseEntity<Series> getSeriesById(Integer id) {
        return seriesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<Book>> getBooksBySeries(Integer id) {
        return ResponseEntity.ok(bookRepository.findBySeries_Id(id));
    }
}

