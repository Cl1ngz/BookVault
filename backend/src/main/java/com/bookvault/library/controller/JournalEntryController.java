package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.dto.JournalEntryUpdateDto;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/journal")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryRepository journalEntryRepository;
    private final ReadingLogRepository readingLogRepository;
    private final ReaderRepository readerRepository;
    private final JwtUtils jwtUtils;

    private Reader authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;

        String token = header.substring(7);

        if (!jwtUtils.isValid(token)) return null;

        return readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
    }

    /**
     * GET /journal              -> all activity entries for the reader newest first
     * GET /journal?readingLogId -> entries for a specific reading log
     */
    @GetMapping
    public ResponseEntity<?> getEntries(
            @RequestParam(required = false) Integer readingLogId,
            HttpServletRequest request
    ) {
        Reader reader = authenticate(request);

        if (reader == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (readingLogId != null) {
            ReadingLog log = readingLogRepository.findById(readingLogId).orElse(null);

            if (log == null) {
                return ResponseEntity.notFound().build();
            }

            if (!log.getReader().getId().equals(reader.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            return ResponseEntity.ok(
                    journalEntryRepository.findByReadingLog_IdOrderByEntryDateDescCreatedAtDesc(readingLogId)
            );
        }

        return ResponseEntity.ok(
                journalEntryRepository.findByReadingLog_Reader_IdOrderByEntryDateDescCreatedAtDesc(reader.getId())
        );
    }

    /**
     * PUT /journal/{id} - edit date and/or cumulativePages of an existing entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntry(
            @PathVariable Integer id,
            @Valid @RequestBody JournalEntryUpdateDto journalEntryDto,
            HttpServletRequest request
    ) {
        Reader reader = authenticate(request);

        if (reader == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        JournalEntry entry = journalEntryRepository.findById(id).orElse(null);

        if (entry == null) {
            return ResponseEntity.notFound().build();
        }

        if (!entry.getReadingLog().getReader().getId().equals(reader.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        if (journalEntryDto.getEntryDate() != null) {
            entry.setEntryDate(journalEntryDto.getEntryDate());
        }

        if (journalEntryDto.getCumulativePages() != null) {
            entry.setCumulativePages(journalEntryDto.getCumulativePages());
        }

        return ResponseEntity.ok(journalEntryRepository.save(entry));
    }

    /**
     * DELETE /journal/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {
        Reader reader = authenticate(request);

        if (reader == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        JournalEntry entry = journalEntryRepository.findById(id).orElse(null);

        if (entry == null) {
            return ResponseEntity.notFound().build();
        }

        if (!entry.getReadingLog().getReader().getId().equals(reader.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        journalEntryRepository.delete(entry);

        return ResponseEntity.ok().build();
    }
}