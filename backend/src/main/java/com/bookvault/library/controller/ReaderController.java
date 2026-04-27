package com.bookvault.library.controller;

import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reader") // Liczba pojedyncza, żeby uniknąć konfliktów
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderRepository readerRepository;

    @GetMapping
    public ResponseEntity<List<Reader>> getAllReaders(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String nationality
    ) {
        // 1. Wyszukiwanie po nazwisku (najczęstszy scenariusz)
        if (lastName != null && !lastName.isBlank()) {
            return ResponseEntity.ok(readerRepository.findByLastNameContainingIgnoreCase(lastName.trim()));
        }

        // 2. Wyszukiwanie po imieniu
        if (firstName != null && !firstName.isBlank()) {
            return ResponseEntity.ok(readerRepository.findByFirstNameContainingIgnoreCase(firstName.trim()));
        }

        // 3. Wyszukiwanie po narodowości
        if (nationality != null && !nationality.isBlank()) {
            return ResponseEntity.ok(readerRepository.findByNationalityContainingIgnoreCase(nationality.trim()));
        }

        // 4. Jeśli brak filtrów - zwróć wszystkich
        return ResponseEntity.ok(readerRepository.findAll());
    }
}