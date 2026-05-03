package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Book;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.Review;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReviewRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtUtils jwtUtils;

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
    public ResponseEntity<?> addReview(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        // Auth check
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            return ResponseEntity.status(401).body("You must be logged in to post a review");

        String token = header.substring(7);
        if (!jwtUtils.isValid(token))
            return ResponseEntity.status(401).body("Invalid or expired token");

        Reader reader = readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
        if (reader == null)
            return ResponseEntity.status(403).body("Reader not found");

        // Validate body
        Integer bookId = (Integer) body.get("bookId");
        Double rating = body.get("rating") instanceof Number
                ? ((Number) body.get("rating")).doubleValue() : null;
        String content = (String) body.get("content");

        if (bookId == null || rating == null)
            return ResponseEntity.badRequest().body("bookId and rating are required");
        if (rating < 0.25 || rating > 5.0)
            return ResponseEntity.badRequest().body("rating must be between 0.25 and 5.0");

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();

        Review review = new Review();
        review.setBook(book);
        review.setReader(reader);
        review.setRating(rating);
        review.setContent(content);
        return ResponseEntity.ok(reviewRepository.save(review));
    }
}