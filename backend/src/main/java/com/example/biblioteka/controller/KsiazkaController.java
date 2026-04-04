package com.example.biblioteka.controller;

import com.example.biblioteka.model.Ksiazka;
import com.example.biblioteka.repository.KsiazkaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ksiazki")
@RequiredArgsConstructor
public class KsiazkaController {

    private final KsiazkaRepository ksiazkaRepository;

    @GetMapping
    public List<Ksiazka> getAllBooks() {
        return ksiazkaRepository.findAll();
    }
}