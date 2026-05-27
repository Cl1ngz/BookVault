package com.bookvault.library.controller;

import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "MODERATOR", "ADMIN");

    private final ReaderRepository readerRepository;

    /**
     * GET /api/v1/admin/readers
     * Returns all users with their ban status.
     */
    @GetMapping("/readers")
    public ResponseEntity<List<Reader>> getAllReaders() {
        return ResponseEntity.ok(readerRepository.findAll());
    }

    /**
     * PUT /api/v1/admin/readers/{id}/role
     * Body: { "role": "MODERATOR" }
     * Changes the role of a user. Cannot demote/promote another ADMIN.
     */
    @PutMapping("/readers/{id}/role")
    public ResponseEntity<?> setRole(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            org.springframework.security.core.Authentication authentication
    ) {
        String newRole = body.get("role");
        if (newRole == null || !ALLOWED_ROLES.contains(newRole.toUpperCase())) {
            return ResponseEntity.badRequest().body("role must be one of: USER, MODERATOR, ADMIN");
        }

        Reader reader = readerRepository.findById(id).orElse(null);
        if (reader == null) return ResponseEntity.notFound().build();

        // Prevent changing another ADMIN's role (only the same admin can demote themselves)
        if ("ADMIN".equals(reader.getRole()) && !reader.getEmail().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(403).body("Cannot change another admin's role");
        }

        reader.setRole(newRole.toUpperCase());
        return ResponseEntity.ok(readerRepository.save(reader));
    }

    /**
     * PUT /api/v1/admin/readers/{id}/ban
     * Body: { "days": 7 }
     * Bans a user for the given number of days. days=0 lifts the ban.
     */
    @PutMapping("/readers/{id}/ban")
    public ResponseEntity<?> banReader(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body
    ) {
        Reader reader = readerRepository.findById(id).orElse(null);
        if (reader == null) return ResponseEntity.notFound().build();

        if ("ADMIN".equals(reader.getRole())) {
            return ResponseEntity.status(403).body("Cannot ban another admin");
        }

        Object daysObj = body.get("days");
        int days = daysObj instanceof Number ? ((Number) daysObj).intValue() : -1;

        if (days < 0) {
            return ResponseEntity.badRequest().body("days must be >= 0 (0 = unban)");
        }

        reader.setBannedUntil(days == 0 ? null : LocalDate.now().plusDays(days));
        Reader saved = readerRepository.save(reader);

        String message = days == 0
                ? "User " + saved.getUsername() + " has been unbanned"
                : "User " + saved.getUsername() + " banned until " + saved.getBannedUntil();

        return ResponseEntity.ok(Map.of(
                "message", message,
                "bannedUntil", saved.getBannedUntil() != null ? saved.getBannedUntil().toString() : ""
        ));
    }

    /**
     * DELETE /api/v1/admin/readers/{id}
     * Permanently deletes a user account. Cannot delete an ADMIN account.
     */
    @DeleteMapping("/readers/{id}")
    public ResponseEntity<?> deleteReader(
            @PathVariable Integer id,
            org.springframework.security.core.Authentication authentication
    ) {
        Reader reader = readerRepository.findById(id).orElse(null);
        if (reader == null) return ResponseEntity.notFound().build();

        if ("ADMIN".equals(reader.getRole())) {
            return ResponseEntity.status(403).body("Cannot delete an admin account");
        }

        // Prevent self-deletion
        if (reader.getEmail() != null && reader.getEmail().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(403).body("Cannot delete your own account here");
        }

        readerRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User " + reader.getUsername() + " deleted"));
    }
}

