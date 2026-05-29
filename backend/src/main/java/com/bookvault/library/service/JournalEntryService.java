package com.bookvault.library.service;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.dto.JournalEntryUpdateDto;
import com.bookvault.library.model.JournalEntry;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.ReadingLog;
import com.bookvault.library.repository.JournalEntryRepository;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReadingLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final ReadingLogRepository readingLogRepository;
    private final ReaderRepository readerRepository;
    private final JwtUtils jwtUtils;

    public Reader authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        if (!jwtUtils.isValid(token)) return null;
        return readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
    }

    public ResponseEntity<?> getEntries(Integer readingLogId, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        if (readingLogId != null) {
            ReadingLog log = readingLogRepository.findById(readingLogId).orElse(null);
            if (log == null) return ResponseEntity.notFound().build();
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

    public ResponseEntity<?> updateEntry(Integer id, JournalEntryUpdateDto journalEntryDto, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        JournalEntry entry = journalEntryRepository.findById(id).orElse(null);
        if (entry == null) return ResponseEntity.notFound().build();

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

    public ResponseEntity<?> deleteEntry(Integer id, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        JournalEntry entry = journalEntryRepository.findById(id).orElse(null);
        if (entry == null) return ResponseEntity.notFound().build();

        if (!entry.getReadingLog().getReader().getId().equals(reader.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        journalEntryRepository.delete(entry);
        return ResponseEntity.ok().build();
    }
}

