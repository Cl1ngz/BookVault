package com.bookvault.library.controller;

import com.bookvault.library.dto.JournalEntryUpdateDto;
import com.bookvault.library.service.JournalEntryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/journal")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @GetMapping
    public ResponseEntity<?> getEntries(
            @RequestParam(required = false) Integer readingLogId,
            HttpServletRequest request
    ) {
        return journalEntryService.getEntries(readingLogId, request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntry(
            @PathVariable Integer id,
            @Valid @RequestBody JournalEntryUpdateDto journalEntryDto,
            HttpServletRequest request
    ) {
        return journalEntryService.updateEntry(id, journalEntryDto, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Integer id, HttpServletRequest request) {
        return journalEntryService.deleteEntry(id, request);
    }
}