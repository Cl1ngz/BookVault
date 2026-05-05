package com.bookvault.library.repository;

import com.bookvault.library.model.ReadingGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, Integer> {
    List<ReadingGoal> findByReader_IdOrderByYearDesc(Integer readerId);
    Optional<ReadingGoal> findByReader_IdAndYear(Integer readerId, Integer year);
}

