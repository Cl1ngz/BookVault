package com.bookvault.library.controller;

import com.bookvault.library.dto.AuthorDto;
import com.bookvault.library.model.Author;
import com.bookvault.library.model.Book;
import com.bookvault.library.repository.AuthorRepository;
import com.bookvault.library.repository.BookRepository;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody AuthorDto authorDto) {
        Author author = new Author();

        author.setFirstName(authorDto.getFirstName());
        author.setLastName(authorDto.getLastName());
        author.setBirthDate(authorDto.getBirthDate());
        author.setNationality(authorDto.getNationality());
        author.setBiography(authorDto.getBiography());
        author.setEmail(authorDto.getEmail());

        Author savedAuthor = authorRepository.save(author);
        return ResponseEntity.ok(savedAuthor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(
            @PathVariable Integer id,
            @Valid @RequestBody AuthorDto authorDto
    ) {
        return authorRepository.findById(id)
                .map(author -> {
                    author.setFirstName(authorDto.getFirstName());
                    author.setLastName(authorDto.getLastName());
                    author.setBirthDate(authorDto.getBirthDate());
                    author.setNationality(authorDto.getNationality());
                    author.setBiography(authorDto.getBiography());
                    author.setEmail(authorDto.getEmail());

                    Author updatedAuthor = authorRepository.save(author);
                    return ResponseEntity.ok(updatedAuthor);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Integer id) {
        if (!authorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        authorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}