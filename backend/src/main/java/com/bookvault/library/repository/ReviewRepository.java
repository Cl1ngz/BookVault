package com.bookvault.library.repository;

import com.bookvault.library.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    // Wyszukiwanie recenzji po konkretnej ocenie
    List<Review> findByRating(Integer rating);

    // Wyszukiwanie recenzji dla konkretnej książki (po jej ID)
    List<Review> findByBook_Id(Integer bookId);
}