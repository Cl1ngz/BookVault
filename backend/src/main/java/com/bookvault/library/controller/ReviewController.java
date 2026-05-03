package com.bookvault.library.controller;

import com.bookvault.library.model.Review;
import com.bookvault.library.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer bookId
    ) {
        if (bookId != null) {
            return ResponseEntity.ok(reviewRepository.findByBook_Id(bookId));
        }
        if (rating != null) {
            return ResponseEntity.ok(reviewRepository.findByRating(rating));
        }

        return ResponseEntity.ok(reviewRepository.findAll());
    }
}