package com.bookvault.library.service;

import com.bookvault.library.dto.ReviewReportDto;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.Review;
import com.bookvault.library.model.ReviewReport;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReviewReportRepository;
import com.bookvault.library.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewReportService {

    private final ReviewReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final ReaderRepository readerRepository;

    public ResponseEntity<?> reportReview(ReviewReportDto reportDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You must be logged in to report a review");
        }

        Reader reporter = readerRepository.findByEmailIgnoreCase(authentication.getName()).orElse(null);
        if (reporter == null) return ResponseEntity.status(403).body("Only registered readers can report reviews");

        Review review = reviewRepository.findById(reportDto.getReviewId()).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();

        boolean alreadyReported = reportRepository.existsByReviewIdAndReporterIdAndReporterType(
                reportDto.getReviewId(), reporter.getId(), "reader"
        );
        if (alreadyReported) return ResponseEntity.badRequest().body("You have already reported this review");

        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporterType("reader");
        report.setReporterId(reporter.getId());
        report.setReason(reportDto.getReason().trim());

        return ResponseEntity.ok(reportRepository.save(report));
    }

    public List<ReviewReport> getReports(String status) {
        if (status != null && !status.isBlank()) {
            return reportRepository.findByStatus(status.trim());
        }
        return reportRepository.findAll();
    }
}

