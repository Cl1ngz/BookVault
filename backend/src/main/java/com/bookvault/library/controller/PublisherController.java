package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.model.Publisher;
import com.bookvault.library.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public ResponseEntity<List<Publisher>> getAllPublishers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(publisherService.getAllPublishers(name, owner, year));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publisher> getPublisherById(@PathVariable Integer id) {
        return publisherService.getPublisherById(id);
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getBooksByPublisher(@PathVariable Integer id) {
        return publisherService.getBooksByPublisher(id);
    }
}