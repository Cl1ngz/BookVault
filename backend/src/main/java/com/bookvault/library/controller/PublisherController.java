package com.bookvault.library.controller;

import com.bookvault.library.model.Publisher;
import com.bookvault.library.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publisher") // Liczba pojedyncza - konsekwentnie!
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherRepository publisherRepository;

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

        // 4. Domyślnie - zwróć wszystko
        return ResponseEntity.ok(publisherRepository.findAll());
    }
}