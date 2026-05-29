package com.bookvault.library.service;

import com.bookvault.library.model.Book;
import com.bookvault.library.model.Publisher;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    public List<Publisher> getAllPublishers(String name, String owner, Integer year) {
        if (name != null && !name.isBlank()) {
            return publisherRepository.findByNameContainingIgnoreCase(name.trim());
        }
        if (owner != null && !owner.isBlank()) {
            return publisherRepository.findByOwnerContainingIgnoreCase(owner.trim());
        }
        if (year != null) {
            return publisherRepository.findByFoundationYear(year);
        }
        return publisherRepository.findAll();
    }

    public ResponseEntity<Publisher> getPublisherById(Integer id) {
        return publisherRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<Book>> getBooksByPublisher(Integer id) {
        return ResponseEntity.ok(bookRepository.findByPublisher_Id(id));
    }
}

