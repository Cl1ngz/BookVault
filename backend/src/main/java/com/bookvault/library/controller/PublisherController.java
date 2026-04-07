package com.bookvault.library.controller;

import com.bookvault.library.model.Publisher;
import com.bookvault.library.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publisher") // Oddzielny endpoint dla wydawnictw
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherRepository publisherRepository;

    @GetMapping
    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll(); // Pobiera dane z tabeli 'wydawnictwa'
    }
}