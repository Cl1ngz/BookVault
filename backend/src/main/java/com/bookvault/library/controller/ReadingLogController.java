package com.bookvault.library.controller;

import com.bookvault.library.dto.ReadingLogCreateDto;
import com.bookvault.library.dto.ReadingLogUpdateDto;
import com.bookvault.library.service.ReadingLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reading-log")
@RequiredArgsConstructor
public class ReadingLogController {

    private final ReadingLogService readingLogService;

    @GetMapping
    public ResponseEntity<?> getShelf(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer bookId,
            HttpServletRequest request
    ) {
        return readingLogService.getShelf(status, bookId, request);
    }

    @PostMapping
    public ResponseEntity<?> addToShelf(
            @Valid @RequestBody ReadingLogCreateDto readingLogDto,
            HttpServletRequest request
    ) {
        return readingLogService.addToShelf(readingLogDto, request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateShelfEntry(
            @PathVariable Integer id,
            @Valid @RequestBody ReadingLogUpdateDto readingLogDto,
            HttpServletRequest request
    ) {
        return readingLogService.updateShelfEntry(id, readingLogDto, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromShelf(@PathVariable Integer id, HttpServletRequest request) {
        return readingLogService.removeFromShelf(id, request);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        return readingLogService.getStats(request);
    }
}