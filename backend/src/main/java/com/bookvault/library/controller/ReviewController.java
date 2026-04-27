package com.bookvault.library.controller;

import com.bookvault.library.model.Review;
import com.bookvault.library.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}