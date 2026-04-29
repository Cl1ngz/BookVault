package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Review;
import com.bookvault.library.model.ReviewReport;
import com.bookvault.library.repository.ReviewReportRepository;
import com.bookvault.library.repository.ReviewRepository;
import com.bookvault.library.repository.ReaderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final ReaderRepository readerRepository;
    private final JwtUtils jwtUtils;

    // POST /api/v1/reports  — must be logged in as a reader
    // Body: { "reviewId": 1, "reason": "spam" }
    @PostMapping
    public ResponseEntity<?> reportReview(@RequestBody Map<String, Object> body,
                                          HttpServletRequest request) {
        // 1. Extract token and verify it belongs to a registered reader
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("You must be logged in to report a review");
        }

        String token = header.substring(7);
        if (!jwtUtils.isValid(token)) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        String email = jwtUtils.extractEmail(token);
        com.bookvault.library.model.Reader reporter = readerRepository.findByEmail(email).orElse(null);
        if (reporter == null) {
            return ResponseEntity.status(403).body("Only registered readers can report reviews");
        }

        // 2. Validate request body
        Integer reviewId = (Integer) body.get("reviewId");
        String reason = (String) body.get("reason");

        if (reviewId == null || reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body("reviewId and reason are required");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();

        // 3. Prevent duplicate reports from the same reader
        boolean alreadyReported = reportRepository
                .existsByReviewIdAndReporterIdAndReporterType(reviewId, reporter.getId(), "reader");
        if (alreadyReported) {
            return ResponseEntity.badRequest().body("You have already reported this review");
        }

        // 4. Save — reporter identity comes from JWT, not from body
        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporterType("reader");
        report.setReporterId(reporter.getId());
        report.setReason(reason);

        return ResponseEntity.ok(reportRepository.save(report));
    }

    // GET /api/v1/reports?status=pending
    @GetMapping
    public ResponseEntity<List<ReviewReport>> getReports(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(reportRepository.findByStatus(status));
        }
        return ResponseEntity.ok(reportRepository.findAll());
    }
}
