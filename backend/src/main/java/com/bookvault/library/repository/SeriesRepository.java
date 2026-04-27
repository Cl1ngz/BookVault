package com.bookvault.library.repository;

import com.bookvault.library.model.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Integer> {

    // Wyszukiwanie serii po jej nazwie (np. "Wiedźmin", "Harry Potter")
    List<Series> findByNameContainingIgnoreCase(String name);

    // Wyszukiwanie serii po nazwisku autora (author -> lastName)
    // To pozwoli znaleźć wszystkie serie napisane przez konkretnego autora
    List<Series> findByAuthor_LastNameContainingIgnoreCase(String lastName);
}