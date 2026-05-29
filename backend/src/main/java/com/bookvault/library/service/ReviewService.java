package com.bookvault.library.service;

import com.bookvault.library.dto.ReviewDto;
import com.bookvault.library.model.Book;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.Review;
import com.bookvault.library.repository.BookRepository;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    public List<Review> getAllReviews(Double rating, Integer bookId) {
        if (bookId != null) return reviewRepository.findByBook_Id(bookId);
        if (rating != null) return reviewRepository.findByRating(rating);
        return reviewRepository.findAll();
    }

    public ResponseEntity<?> addReview(ReviewDto reviewDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("You must be logged in to post a review");
        }

        Reader reader = readerRepository.findByEmailIgnoreCase(authentication.getName()).orElse(null);
        if (reader == null) return ResponseEntity.status(403).body("Reader not found");

        Book book = bookRepository.findById(reviewDto.getBookId()).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();

        Review review = new Review();
        review.setBook(book);
        review.setReader(reader);
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());

        return ResponseEntity.ok(reviewRepository.save(review));
    }
}

