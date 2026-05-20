package com.bookvault.library.controller;

import com.bookvault.library.model.Review;
import com.bookvault.library.model.ReviewReport;
import com.bookvault.library.repository.ReviewReportRepository;
import com.bookvault.library.repository.ReviewRepository;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final ReaderRepository readerRepository;

    // POST /api/v1/reports  — must be logged in as a reader
    // Body: { "reviewId": 1, "reason": "spam" }
    @PostMapping
    public ResponseEntity<?> reportReview(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You must be logged in to report a review");
        }

        String email = authentication.getName();

        com.bookvault.library.model.Reader reporter =
                readerRepository.findByEmailIgnoreCase(email).orElse(null);

        if (reporter == null) {
            return ResponseEntity.status(403).body("Only registered readers can report reviews");
        }

        Integer reviewId = null;
        Object reviewIdObj = body.get("reviewId");

        if (reviewIdObj instanceof Number) {
            reviewId = ((Number) reviewIdObj).intValue();
        }

        String reason = body.get("reason") instanceof String
                ? (String) body.get("reason")
                : null;

        if (reviewId == null || reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body("reviewId and reason are required");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        boolean alreadyReported = reportRepository
                .existsByReviewIdAndReporterIdAndReporterType(
                        reviewId,
                        reporter.getId(),
                        "reader"
                );

        if (alreadyReported) {
            return ResponseEntity.badRequest().body("You have already reported this review");
        }

        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporterType("reader");
        report.setReporterId(reporter.getId());
        report.setReason(reason);

        return ResponseEntity.ok(reportRepository.save(report));
    }

    // GET /api/v1/reports?status=pending
    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<ReviewReport>> getReports(
            @RequestParam(required = false) String status
    ) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(reportRepository.findByStatus(status.trim()));
        }

        return ResponseEntity.ok(reportRepository.findAll());
    }
}
