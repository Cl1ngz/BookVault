package com.bookvault.library.controller;

import com.bookvault.library.dto.ReviewReportDto;
import com.bookvault.library.model.ReviewReport;
import com.bookvault.library.service.ReviewReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    // POST /api/v1/reports
    // Body: { "reviewId": 1, "reason": "spam" }
    @PostMapping
    public ResponseEntity<?> reportReview(
            @Valid @RequestBody ReviewReportDto reportDto,
            Authentication authentication
    ) {
        return reviewReportService.reportReview(reportDto, authentication);
    }

    // GET /api/v1/reports?status=pending
    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<ReviewReport>> getReports(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(reviewReportService.getReports(status));
    }
}