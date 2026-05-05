package com.bookvault.library.repository;

import com.bookvault.library.model.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Integer> {

    List<Series> findByNameContainingIgnoreCase(String name);

    List<Series> findByAuthor_LastNameContainingIgnoreCase(String lastName);
}