package com.bookvault.library.controller;

import com.bookvault.library.model.Book;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.Review;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Integer bookId
    ) {
        if (bookId != null) return ResponseEntity.ok(reviewRepository.findByBook_Id(bookId));
        if (rating != null) return ResponseEntity.ok(reviewRepository.findByRating(rating));
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> addReview(
            @RequestBody Map<String, Object> body,
            org.springframework.security.core.Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You must be logged in to post a review");
        }

        String email = authentication.getName();

        Reader reader = readerRepository.findByEmailIgnoreCase(email).orElse(null);
        if (reader == null) {
            return ResponseEntity.status(403).body("Reader not found");
        }

        Integer bookId = null;
        Object bookIdObj = body.get("bookId");

        if (bookIdObj instanceof Number) {
            bookId = ((Number) bookIdObj).intValue();
        }

        Double rating = null;
        Object ratingObj = body.get("rating");

        if (ratingObj instanceof Number) {
            rating = ((Number) ratingObj).doubleValue();
        }

        String content = body.get("content") instanceof String
                ? (String) body.get("content")
                : null;

        if (bookId == null || rating == null) {
            return ResponseEntity.badRequest().body("bookId and rating are required");
        }

        if (rating < 0.25 || rating > 5.0) {
            return ResponseEntity.badRequest().body("rating must be between 0.25 and 5.0");
        }

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        Review review = new Review();
        review.setBook(book);
        review.setReader(reader);
        review.setRating(rating);
        review.setContent(content);

        return ResponseEntity.ok(reviewRepository.save(review));
    }
}