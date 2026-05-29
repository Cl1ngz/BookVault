package com.bookvault.library.controller;

import com.bookvault.library.dto.ReviewDto;
import com.bookvault.library.model.Book;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.Review;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        if (bookId != null) {
            return ResponseEntity.ok(reviewRepository.findByBook_Id(bookId));
        }

        if (rating != null) {
            return ResponseEntity.ok(reviewRepository.findByRating(rating));
        }

        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> addReview(
            @Valid @RequestBody ReviewDto reviewDto,
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

        Book book = bookRepository.findById(reviewDto.getBookId()).orElse(null);

        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        Review review = new Review();

        review.setBook(book);
        review.setReader(reader);
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());

        return ResponseEntity.ok(reviewRepository.save(review));
    }
}