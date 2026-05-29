package com.bookvault.library.service;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.dto.ReadingGoalDto;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.ReadingGoal;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReadingGoalRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReadingGoalService {

    private final ReadingGoalRepository readingGoalRepository;
    private final ReaderRepository readerRepository;
    private final JwtUtils jwtUtils;

    public Reader authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        if (!jwtUtils.isValid(token)) return null;
        return readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
    }

    public ResponseEntity<?> getGoals(HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(readingGoalRepository.findByReader_IdOrderByYearDesc(reader.getId()));
    }

    public ResponseEntity<?> upsertGoal(ReadingGoalDto readingGoalDto, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        Optional<ReadingGoal> existing = readingGoalRepository.findByReader_IdAndYear(
                reader.getId(), readingGoalDto.getYear()
        );

        ReadingGoal goal = existing.orElse(new ReadingGoal());
        goal.setReader(reader);
        goal.setYear(readingGoalDto.getYear());
        goal.setTargetBooks(readingGoalDto.getTargetBooks());

        return ResponseEntity.ok(readingGoalRepository.save(goal));
    }

    public ResponseEntity<?> deleteGoal(Integer id, HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        ReadingGoal goal = readingGoalRepository.findById(id).orElse(null);
        if (goal == null) return ResponseEntity.notFound().build();

        if (!goal.getReader().getId().equals(reader.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        readingGoalRepository.delete(goal);
        return ResponseEntity.ok().build();
    }
}

