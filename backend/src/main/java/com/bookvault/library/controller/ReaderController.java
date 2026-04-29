package com.bookvault.library.controller;

import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderRepository readerRepository;

    @GetMapping
    public ResponseEntity<List<Reader>> getAllReaders(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nationality
    ) {

        // search by username
        if (username != null && !username.isBlank()) {
            return ResponseEntity.ok(readerRepository.findByUsernameContainingIgnoreCase(username.trim()));
        }

        // by nacionalikty if provided
        if (nationality != null && !nationality.isBlank()) {
            return ResponseEntity.ok(readerRepository.findByNationalityContainingIgnoreCase(nationality.trim()));
        }


        return ResponseEntity.ok(readerRepository.findAll());
    }
}