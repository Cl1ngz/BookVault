package com.bookvault.library.controller;

import com.bookvault.library.dto.ReaderDto;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderRepository readerRepository;

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getReaderById(
            @PathVariable Integer id,
            org.springframework.security.core.Authentication authentication
    ) {
        return readerRepository.findById(id)
                .map(reader -> {
                    boolean isOwner = reader.getEmail() != null
                            && reader.getEmail().equalsIgnoreCase(authentication.getName());

                    boolean isModerator = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR"));

                    if (!isOwner && !isModerator) {
                        return ResponseEntity.status(403).body("You can only access your own profile");
                    }

                    ReaderDto readerDto = new ReaderDto(
                            reader.getId(),
                            reader.getUsername(),
                            reader.getEmail(),
                            reader.getBirthDate(),
                            reader.getNationality(),
                            reader.getRole()
                    );

                    return ResponseEntity.ok(readerDto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}