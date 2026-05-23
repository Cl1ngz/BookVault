package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.model.Publisher;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    @GetMapping
    public ResponseEntity<List<Publisher>> getAllPublishers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) Integer year
    ) {
        // 1. Wyszukiwanie po nazwie wydawnictwa
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(publisherRepository.findByNameContainingIgnoreCase(name.trim()));
        }

        // 2. Wyszukiwanie po właścicielu
        if (owner != null && !owner.isBlank()) {
            return ResponseEntity.ok(publisherRepository.findByOwnerContainingIgnoreCase(owner.trim()));
        }

        // 3. Wyszukiwanie po roku założenia (dokładne dopasowanie)
        if (year != null) {
            return ResponseEntity.ok(publisherRepository.findByFoundationYear(year));
        }

        // 4. Domyślnie - zwrć wszystko
        return ResponseEntity.ok(publisherRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publisher> getPublisherById(@PathVariable Integer id) {
        return publisherRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getBooksByPublisher(@PathVariable Integer id) {
        return ResponseEntity.ok(bookRepository.findByPublisher_Id(id));
    }
}