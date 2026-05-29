package com.bookvault.library.controller;

import com.bookvault.library.model.Reader;
import com.bookvault.library.service.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<Reader>> getAllReaders(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nationality
    ) {
        return ResponseEntity.ok(readerService.getAllReaders(username, nationality));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReaderById(@PathVariable Integer id, Authentication authentication) {
        return readerService.getReaderById(id, authentication);
    }
}