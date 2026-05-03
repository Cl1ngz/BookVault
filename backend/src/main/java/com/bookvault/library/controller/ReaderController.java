package com.bookvault.library.controller;

import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderRepository readerRepository;

    // Only moderators can list all readers
    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<Reader>> getAllReaders(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nationality
    ) {
        if (username != null && !username.isBlank()) {
            return ResponseEntity.ok(readerRepository.findByUsernameContainingIgnoreCase(username.trim()));
        }
        if (nationality != null && !nationality.isBlank()) {
            return ResponseEntity.ok(readerRepository.findByNationalityContainingIgnoreCase(nationality.trim()));
        }
        return ResponseEntity.ok(readerRepository.findAll());
    }

    // Any logged-in user can fetch their own profile by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getReaderById(@PathVariable Integer id) {
        return readerRepository.findById(id)
                .map(r -> ResponseEntity.ok(Map.of(
                        "id", r.getId(),
                        "username", r.getUsername(),
                        "nationality", r.getNationality() != null ? r.getNationality() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}