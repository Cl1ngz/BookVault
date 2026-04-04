package com.example.biblioteka.controller;

import com.example.biblioteka.model.Autor;
import com.example.biblioteka.repository.AutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/autorzy")
@RequiredArgsConstructor // Automatycznie wstrzykuje repozytorium przez konstruktor
public class AutorController {

    private final AutorRepository autorRepository;

    @GetMapping
    public List<Autor> getAllAuthors() {
        return autorRepository.findAll(); // Pobiera wszystkich autorów z bazy
    }
}