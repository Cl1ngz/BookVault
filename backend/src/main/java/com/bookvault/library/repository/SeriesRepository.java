package com.bookvault.library.repository;

import com.bookvault.library.model.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Integer> {
}

