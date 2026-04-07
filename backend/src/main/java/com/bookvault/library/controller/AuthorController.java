package com.bookvault.library.controller;

import com.bookvault.library.model.Author;
import com.bookvault.library.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/author")
@RequiredArgsConstructor // Automatycznie wstrzykuje repozytorium przez konstruktor
public class AuthorController {

    private final AuthorRepository authorRepository;

    @GetMapping
    public List<Author> getAllAuthors() {
        return authorRepository.findAll(); // Pobiera wszystkich autorów z bazy
    }
}