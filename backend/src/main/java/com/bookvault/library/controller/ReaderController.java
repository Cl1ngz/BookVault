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
    public ResponseEntity<?> getReaderById(
            @PathVariable Integer id,
            org.springframework.security.core.Authentication authentication
    ) {
        return readerRepository.findById(id)
                .map(r -> {
                    boolean isOwner = r.getEmail() != null
                            && r.getEmail().equalsIgnoreCase(authentication.getName());

                    boolean isModerator = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR"));

                    if (!isOwner && !isModerator) {
                        return ResponseEntity.status(403).body("You can only access your own profile");
                    }

                    return ResponseEntity.ok(Map.of(
                            "id", r.getId(),
                            "username", r.getUsername(),
                            "email", r.getEmail() != null ? r.getEmail() : "",
                            "nationality", r.getNationality() != null ? r.getNationality() : "",
                            "role", r.getRole() != null ? r.getRole() : "USER"
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}