package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.dto.ReadingGoalDto;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.ReadingGoal;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReadingGoalRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reading-goals")
@RequiredArgsConstructor
public class ReadingGoalController {

    private final ReadingGoalRepository readingGoalRepository;
    private final ReaderRepository readerRepository;
    private final JwtUtils jwtUtils;

    private Reader authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        String token = header.substring(7);

        if (!jwtUtils.isValid(token)) {
            return null;
        }

        return readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> getGoals(HttpServletRequest request) {
        Reader reader = authenticate(request);

        if (reader == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return ResponseEntity.ok(readingGoalRepository.findByReader_IdOrderByYearDesc(reader.getId()));
    }

    @PostMapping
    public ResponseEntity<?> upsertGoal(
            @Valid @RequestBody ReadingGoalDto readingGoalDto,
            HttpServletRequest request
    ) {
        Reader reader = authenticate(request);

        if (reader == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<ReadingGoal> existing = readingGoalRepository.findByReader_IdAndYear(
                reader.getId(),
                readingGoalDto.getYear()
        );

        ReadingGoal goal = existing.orElse(new ReadingGoal());

        goal.setReader(reader);
        goal.setYear(readingGoalDto.getYear());
        goal.setTargetBooks(readingGoalDto.getTargetBooks());

        return ResponseEntity.ok(readingGoalRepository.save(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {
        Reader reader = authenticate(request);

        if (reader == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        ReadingGoal goal = readingGoalRepository.findById(id).orElse(null);

        if (goal == null) {
            return ResponseEntity.notFound().build();
        }

        if (!goal.getReader().getId().equals(reader.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        readingGoalRepository.delete(goal);

        return ResponseEntity.ok().build();
    }
}