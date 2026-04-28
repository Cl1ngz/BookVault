package com.bookvault.library.controller;

import com.bookvault.library.model.Author;
import com.bookvault.library.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors") // Liczba pojedyncza - zgodnie z ustaleniami
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorRepository authorRepository;

    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String nationality
    ) {
        // 1. Wyszukiwanie po nazwisku
        if (lastName != null && !lastName.isBlank()) {
            return ResponseEntity.ok(authorRepository.findByLastNameContainingIgnoreCase(lastName.trim()));
        }

        // 2. Wyszukiwanie po imieniu
        if (firstName != null && !firstName.isBlank()) {
            return ResponseEntity.ok(authorRepository.findByFirstNameContainingIgnoreCase(firstName.trim()));
        }

        // 3. Wyszukiwanie po narodowości
        if (nationality != null && !nationality.isBlank()) {
            return ResponseEntity.ok(authorRepository.findByNationalityContainingIgnoreCase(nationality.trim()));
        }

        // 4. Domyślnie - zwróć wszystkich
        return ResponseEntity.ok(authorRepository.findAll());
    }
}