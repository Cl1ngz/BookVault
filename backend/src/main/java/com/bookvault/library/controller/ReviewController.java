package com.bookvault.library.controller;

import com.bookvault.library.dto.ReviewDto;
import com.bookvault.library.model.Review;
import com.bookvault.library.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Integer bookId
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(rating, bookId));
    }

    @PostMapping
    public ResponseEntity<?> addReview(
            @Valid @RequestBody ReviewDto reviewDto,
            Authentication authentication
    ) {
        return reviewService.addReview(reviewDto, authentication);
    }
}