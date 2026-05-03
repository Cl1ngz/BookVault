package com.bookvault.library.controller;

import com.bookvault.library.model.Author;
import com.bookvault.library.model.Book;
import com.bookvault.library.repository.AuthorRepository;
import com.bookvault.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String nationality
    ) {
        if (lastName != null && !lastName.isBlank()) {
            return ResponseEntity.ok(authorRepository.findByLastNameContainingIgnoreCase(lastName.trim()));
        }
        if (firstName != null && !firstName.isBlank()) {
            return ResponseEntity.ok(authorRepository.findByFirstNameContainingIgnoreCase(firstName.trim()));
        }
        if (nationality != null && !nationality.isBlank()) {
            return ResponseEntity.ok(authorRepository.findByNationalityContainingIgnoreCase(nationality.trim()));
        }
        return ResponseEntity.ok(authorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Integer id) {
        return authorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable Integer id) {
        return ResponseEntity.ok(bookRepository.findByAuthor_Id(id));
    }
}