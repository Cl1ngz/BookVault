package com.bookvault.library.repository;

import com.bookvault.library.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByRating(Double rating);

    List<Review> findByBook_Id(Integer bookId);
}