package com.example.biblioteka.controller;

import com.example.biblioteka.model.Wydawnictwo;
import com.example.biblioteka.repository.WydawnictwoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wydawnictwa") // Oddzielny endpoint dla wydawnictw
@RequiredArgsConstructor
public class WydawnictwoController {

    private final WydawnictwoRepository wydawnictwoRepository;

    @GetMapping
    public List<Wydawnictwo> getAllPublishers() {
        return wydawnictwoRepository.findAll(); // Pobiera dane z tabeli 'wydawnictwa'
    }
}