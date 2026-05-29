package com.bookvault.library.service;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.dto.ReadingLogCreateDto;
import com.bookvault.library.dto.ReadingLogUpdateDto;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReadingLogService {

    private final ReadingLogRepository readingLogRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JwtUtils jwtUtils;

    public Reader authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        if (!jwtUtils.isValid(token)) return null;
        return readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
    }

    private void createStatusEntry(ReadingLog log, ReadingStatus status, Integer pages) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setReadingLog(log);
        journalEntry.setEntryType(JournalEntryType.STATUS_CHANGE);
        journalEntry.setStatus(status);
        journalEntry.setEntryDate(LocalDate.now());
        if (pages != null && pages > 0) {
            journalEntry.setCumulativePages(pages);
        }
        journalEntryRepository.save(journalEntry);
    }

    private void createProgressEntry(ReadingLog log, int pages) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setReadingLog(log);
        journalEntry.setEntryType(JournalEntryType.PROGRESS_UPDATE);
        journalEntry.setCumulativePages(pages);
        journalEntry.setEntryDate(LocalDate.now());
        journalEntryRepository.save(journalEntry);
    }

    public ResponseEntity<?> getShelf(String status, Integer bookId, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        if (bookId != null) {
            return ResponseEntity.ok(
                    readingLogRepository.findByReader_IdAndBook_Id(reader.getId(), bookId).orElse(null)
            );
        }

        if (status != null) {
            try {
                ReadingStatus readingStatus = ReadingStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(readingLogRepository.findByReader_IdAndStatus(reader.getId(), readingStatus));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status value");
            }
        }

        return ResponseEntity.ok(readingLogRepository.findByReader_Id(reader.getId()));
    }

    public ResponseEntity<?> addToShelf(ReadingLogCreateDto readingLogDto, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        Integer bookId = readingLogDto.getBookId();
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();

        if (readingLogRepository.findByReader_IdAndBook_Id(reader.getId(), bookId).isPresent()) {
            return ResponseEntity.status(409).body("Book already on your shelf");
        }

        ReadingStatus status = readingLogDto.getStatus() != null ? readingLogDto.getStatus() : ReadingStatus.TO_READ;

        ReadingLog log = new ReadingLog();
        log.setReader(reader);
        log.setBook(book);
        log.setStatus(status);
        log.setPagesRead(0);

        if (status == ReadingStatus.READING) {
            log.setStartedAt(LocalDate.now());
        }
        if (status == ReadingStatus.FINISHED || status == ReadingStatus.DNF) {
            log.setFinishedAt(LocalDate.now());
        }

        ReadingLog saved = readingLogRepository.save(log);

        if (status != ReadingStatus.TO_READ) {
            createStatusEntry(saved, status, saved.getPagesRead());
        }

        return ResponseEntity.ok(saved);
    }

    public ResponseEntity<?> updateShelfEntry(Integer id, ReadingLogUpdateDto readingLogDto, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        ReadingLog log = readingLogRepository.findById(id).orElse(null);
        if (log == null) return ResponseEntity.notFound().build();

        if (!log.getReader().getId().equals(reader.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        ReadingStatus oldStatus = log.getStatus();
        int oldPages = log.getPagesRead() != null ? log.getPagesRead() : 0;
        ReadingStatus newStatus = oldStatus;

        if (readingLogDto.getStatus() != null) {
            newStatus = readingLogDto.getStatus();
            log.setStatus(newStatus);

            if (newStatus == ReadingStatus.READING && oldStatus != ReadingStatus.READING && log.getStartedAt() == null) {
                log.setStartedAt(LocalDate.now());
            }

            if ((newStatus == ReadingStatus.FINISHED || newStatus == ReadingStatus.DNF) && log.getFinishedAt() == null) {
                log.setFinishedAt(LocalDate.now());
            }
        }

        if (readingLogDto.getPagesRead() != null) log.setPagesRead(readingLogDto.getPagesRead());
        if (readingLogDto.getStartedAt() != null) log.setStartedAt(readingLogDto.getStartedAt());
        if (readingLogDto.getFinishedAt() != null) log.setFinishedAt(readingLogDto.getFinishedAt());

        int newPages = log.getPagesRead() != null ? log.getPagesRead() : 0;
        ReadingLog saved = readingLogRepository.save(log);

        if (!newStatus.equals(oldStatus)) {
            createStatusEntry(saved, newStatus,
                    (newStatus == ReadingStatus.FINISHED || newStatus == ReadingStatus.DNF) ? newPages : null);
        } else if (readingLogDto.getPagesRead() != null && newPages != oldPages) {
            createProgressEntry(saved, newPages);
        }

        return ResponseEntity.ok(saved);
    }

    public ResponseEntity<?> removeFromShelf(Integer id, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        ReadingLog log = readingLogRepository.findById(id).orElse(null);
        if (log == null) return ResponseEntity.notFound().build();

        if (!log.getReader().getId().equals(reader.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        readingLogRepository.delete(log);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getStats(HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        int currentYear = LocalDate.now().getYear();
        Map<String, Long> stats = new HashMap<>();
        stats.put("toRead", readingLogRepository.countByReader_IdAndStatus(reader.getId(), ReadingStatus.TO_READ));
        stats.put("reading", readingLogRepository.countByReader_IdAndStatus(reader.getId(), ReadingStatus.READING));
        stats.put("finished", readingLogRepository.countByReader_IdAndStatus(reader.getId(), ReadingStatus.FINISHED));
        stats.put("dnf", readingLogRepository.countByReader_IdAndStatus(reader.getId(), ReadingStatus.DNF));
        stats.put("finishedThisYear", readingLogRepository.countByReader_IdAndStatusAndFinishedAtYear(
                reader.getId(), ReadingStatus.FINISHED, currentYear
        ));

        return ResponseEntity.ok(stats);
    }
}

