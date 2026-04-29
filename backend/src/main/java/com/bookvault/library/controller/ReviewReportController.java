package com.bookvault.library.controller;

import com.bookvault.library.model.Review;
import com.bookvault.library.model.ReviewReport;
import com.bookvault.library.repository.ReviewReportRepository;
import com.bookvault.library.repository.ReviewRepository;
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

    // POST /api/v1/reports
    // Body: { "reviewId": 1, "reporterType": "reader", "reporterId": 5, "reason": "spam" }
    @PostMapping
    public ResponseEntity<?> reportReview(@RequestBody Map<String, Object> body) {
        Integer reviewId = (Integer) body.get("reviewId");
        String reporterType = (String) body.get("reporterType");
        Integer reporterId = (Integer) body.get("reporterId");
        String reason = (String) body.get("reason");

        if (reviewId == null || reporterType == null || reporterId == null || reason == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }
        if (!reporterType.equals("reader") && !reporterType.equals("author")) {
            return ResponseEntity.badRequest().body("reporterType must be 'reader' or 'author'");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();

        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporterType(reporterType);
        report.setReporterId(reporterId);
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
