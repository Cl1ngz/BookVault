package com.bookvault.library.repository;

import com.bookvault.library.model.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Integer> {
    List<ReviewReport> findByStatus(String status);
    List<ReviewReport> findByReview_Id(Integer reviewId);
}
