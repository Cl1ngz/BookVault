package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.*;
import com.bookvault.library.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reading-log")
@RequiredArgsConstructor
public class ReadingLogController {

    private final ReadingLogRepository readingLogRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JwtUtils jwtUtils;

    private Reader authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        if (!jwtUtils.isValid(token)) return null;
        return readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
    }

    private void createStatusEntry(ReadingLog log, ReadingStatus status, Integer pages) {
        JournalEntry je = new JournalEntry();
        je.setReadingLog(log);
        je.setEntryType(JournalEntryType.STATUS_CHANGE);
        je.setStatus(status);
        je.setEntryDate(LocalDate.now());
        if (pages != null && pages > 0) je.setCumulativePages(pages);
        journalEntryRepository.save(je);
    }

    private void createProgressEntry(ReadingLog log, int pages) {
        JournalEntry je = new JournalEntry();
        je.setReadingLog(log);
        je.setEntryType(JournalEntryType.PROGRESS_UPDATE);
        je.setCumulativePages(pages);
        je.setEntryDate(LocalDate.now());
        journalEntryRepository.save(je);
    }

    @GetMapping
    public ResponseEntity<?> getShelf(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer bookId,
            HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        if (bookId != null)
            return ResponseEntity.ok(readingLogRepository.findByReader_IdAndBook_Id(reader.getId(), bookId).orElse(null));
        if (status != null) {
            try {
                ReadingStatus s = ReadingStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(readingLogRepository.findByReader_IdAndStatus(reader.getId(), s));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status value");
            }
        }
        return ResponseEntity.ok(readingLogRepository.findByReader_Id(reader.getId()));
    }

    @PostMapping
    public ResponseEntity<?> addToShelf(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        Integer bookId = (Integer) body.get("bookId");
        if (bookId == null) return ResponseEntity.badRequest().body("bookId is required");

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();

        if (readingLogRepository.findByReader_IdAndBook_Id(reader.getId(), bookId).isPresent())
            return ResponseEntity.status(409).body("Book already on your shelf");

        ReadingStatus status = ReadingStatus.TO_READ;
        if (body.get("status") != null) {
            try { status = ReadingStatus.valueOf(body.get("status").toString().toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        ReadingLog log = new ReadingLog();
        log.setReader(reader);
        log.setBook(book);
        log.setStatus(status);
        log.setPagesRead(0);
        if (status == ReadingStatus.READING) log.setStartedAt(LocalDate.now());
        if (status == ReadingStatus.FINISHED || status == ReadingStatus.DNF) log.setFinishedAt(LocalDate.now());

        ReadingLog saved = readingLogRepository.save(log);

        // Auto-create journal entry for non-TO_READ statuses
        if (status != ReadingStatus.TO_READ)
            createStatusEntry(saved, status, saved.getPagesRead());

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateShelfEntry(@PathVariable Integer id,
                                               @RequestBody Map<String, Object> body,
                                               HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        ReadingLog log = readingLogRepository.findById(id).orElse(null);
        if (log == null) return ResponseEntity.notFound().build();
        if (!log.getReader().getId().equals(reader.getId()))
            return ResponseEntity.status(403).body("Forbidden");

        ReadingStatus oldStatus = log.getStatus();
        int oldPages = log.getPagesRead() != null ? log.getPagesRead() : 0;

        ReadingStatus newStatus = oldStatus;
        if (body.get("status") != null) {
            try {
                newStatus = ReadingStatus.valueOf(body.get("status").toString().toUpperCase());
                log.setStatus(newStatus);
                if (newStatus == ReadingStatus.READING && oldStatus != ReadingStatus.READING
                        && log.getStartedAt() == null)
                    log.setStartedAt(LocalDate.now());
                if ((newStatus == ReadingStatus.FINISHED || newStatus == ReadingStatus.DNF)
                        && log.getFinishedAt() == null)
                    log.setFinishedAt(LocalDate.now());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status value");
            }
        }
        if (body.get("pagesRead") != null)
            log.setPagesRead(((Number) body.get("pagesRead")).intValue());
        if (body.get("startedAt") != null)
            log.setStartedAt(LocalDate.parse(body.get("startedAt").toString()));
        if (body.get("finishedAt") != null)
            log.setFinishedAt(LocalDate.parse(body.get("finishedAt").toString()));

        int newPages = log.getPagesRead() != null ? log.getPagesRead() : 0;
        ReadingLog saved = readingLogRepository.save(log);

        // Auto-create journal entry
        if (!newStatus.equals(oldStatus)) {
            // Status changed → create STATUS_CHANGE entry
            createStatusEntry(saved, newStatus,
                    (newStatus == ReadingStatus.FINISHED || newStatus == ReadingStatus.DNF) ? newPages : null);
        } else if (body.get("pagesRead") != null && newPages != oldPages) {
            // Only pages changed → PROGRESS_UPDATE
            createProgressEntry(saved, newPages);
        }

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromShelf(@PathVariable Integer id,
                                              HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        ReadingLog log = readingLogRepository.findById(id).orElse(null);
        if (log == null) return ResponseEntity.notFound().build();
        if (!log.getReader().getId().equals(reader.getId()))
            return ResponseEntity.status(403).body("Forbidden");

        readingLogRepository.delete(log);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
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
                reader.getId(), ReadingStatus.FINISHED, currentYear));
        return ResponseEntity.ok(stats);
    }
}
