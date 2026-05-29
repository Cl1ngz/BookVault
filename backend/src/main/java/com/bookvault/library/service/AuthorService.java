package com.bookvault.library.service;

import com.bookvault.library.dto.AuthorDto;
import com.bookvault.library.model.Author;
import com.bookvault.library.model.Book;
import com.bookvault.library.repository.AuthorRepository;
import com.bookvault.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public List<Author> getAllAuthors(String firstName, String lastName, String nationality) {
        if (lastName != null && !lastName.isBlank()) {
            return authorRepository.findByLastNameContainingIgnoreCase(lastName.trim());
        }
        if (firstName != null && !firstName.isBlank()) {
            return authorRepository.findByFirstNameContainingIgnoreCase(firstName.trim());
        }
        if (nationality != null && !nationality.isBlank()) {
            return authorRepository.findByNationalityContainingIgnoreCase(nationality.trim());
        }
        return authorRepository.findAll();
    }

    public ResponseEntity<Author> getAuthorById(Integer id) {
        return authorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<Book>> getBooksByAuthor(Integer id) {
        return ResponseEntity.ok(bookRepository.findByAuthor_Id(id));
    }

    public ResponseEntity<Author> createAuthor(AuthorDto authorDto) {
        Author author = new Author();
        author.setFirstName(authorDto.getFirstName());
        author.setLastName(authorDto.getLastName());
        author.setBirthDate(authorDto.getBirthDate());
        author.setNationality(authorDto.getNationality());
        author.setBiography(authorDto.getBiography());
        author.setEmail(authorDto.getEmail());
        return ResponseEntity.ok(authorRepository.save(author));
    }

    public ResponseEntity<Author> updateAuthor(Integer id, AuthorDto authorDto) {
        return authorRepository.findById(id)
                .map(author -> {
                    author.setFirstName(authorDto.getFirstName());
                    author.setLastName(authorDto.getLastName());
                    author.setBirthDate(authorDto.getBirthDate());
                    author.setNationality(authorDto.getNationality());
                    author.setBiography(authorDto.getBiography());
                    author.setEmail(authorDto.getEmail());
                    return ResponseEntity.ok(authorRepository.save(author));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteAuthor(Integer id) {
        if (!authorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        authorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

