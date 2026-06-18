package com.bookvault.library.service;

import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "MODERATOR", "ADMIN");

    private final ReaderRepository readerRepository;

    public List<Reader> getAllReaders() {
        return readerRepository.findAllByOrderByIdAsc();
    }

    public ResponseEntity<?> setRole(Integer id, Map<String, String> body, Authentication authentication) {
        String newRole = body.get("role");
        if (newRole == null || !ALLOWED_ROLES.contains(newRole.toUpperCase())) {
            return ResponseEntity.badRequest().body("role must be one of: USER, MODERATOR, ADMIN");
        }

        Reader reader = readerRepository.findById(id).orElse(null);
        if (reader == null) return ResponseEntity.notFound().build();

        if ("ADMIN".equals(reader.getRole()) && !reader.getEmail().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(403).body("Cannot change another admin's role");
        }

        reader.setRole(newRole.toUpperCase());
        return ResponseEntity.ok(readerRepository.save(reader));
    }

    public ResponseEntity<?> banReader(Integer id, Map<String, Object> body) {
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

    public ResponseEntity<?> deleteReader(Integer id, Authentication authentication) {
        Reader reader = readerRepository.findById(id).orElse(null);
        if (reader == null) return ResponseEntity.notFound().build();

        if ("ADMIN".equals(reader.getRole())) {
            return ResponseEntity.status(403).body("Cannot delete an admin account");
        }

        if (reader.getEmail() != null && reader.getEmail().equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(403).body("Cannot delete your own account here");
        }

        readerRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User " + reader.getUsername() + " deleted"));
    }
}