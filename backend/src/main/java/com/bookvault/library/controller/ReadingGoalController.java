package com.bookvault.library.controller;

import com.bookvault.library.dto.ReadingGoalDto;
import com.bookvault.library.service.ReadingGoalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reading-goals")
@RequiredArgsConstructor
public class ReadingGoalController {

    private final ReadingGoalService readingGoalService;

    @GetMapping
    public ResponseEntity<?> getGoals(HttpServletRequest request) {
        return readingGoalService.getGoals(request);
    }

    @PostMapping
    public ResponseEntity<?> upsertGoal(
            @Valid @RequestBody ReadingGoalDto readingGoalDto,
            HttpServletRequest request
    ) {
        return readingGoalService.upsertGoal(readingGoalDto, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Integer id, HttpServletRequest request) {
        return readingGoalService.deleteGoal(id, request);
    }
}