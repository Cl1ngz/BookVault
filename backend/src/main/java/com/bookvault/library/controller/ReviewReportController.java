package com.bookvault.library.controller;

import com.bookvault.library.dto.ReviewReportDto;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.Review;
import com.bookvault.library.model.ReviewReport;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReviewReportRepository;
import com.bookvault.library.repository.ReviewRepository;
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

    private final ReviewReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final ReaderRepository readerRepository;

    // POST /api/v1/reports
    // Body: { "reviewId": 1, "reason": "spam" }
    @PostMapping
    public ResponseEntity<?> reportReview(
            @Valid @RequestBody ReviewReportDto reportDto,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You must be logged in to report a review");
        }

        String email = authentication.getName();

        Reader reporter = readerRepository.findByEmailIgnoreCase(email).orElse(null);

        if (reporter == null) {
            return ResponseEntity.status(403).body("Only registered readers can report reviews");
        }

        Review review = reviewRepository.findById(reportDto.getReviewId()).orElse(null);

        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        boolean alreadyReported = reportRepository
                .existsByReviewIdAndReporterIdAndReporterType(
                        reportDto.getReviewId(),
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
        report.setReason(reportDto.getReason().trim());

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