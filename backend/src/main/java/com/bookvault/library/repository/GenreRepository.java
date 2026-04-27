package com.bookvault.library.repository;

import com.bookvault.library.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    // Do sprawdzania unikalności (dokładna nazwa)
    Optional<Genre> findByNameIgnoreCase(String name);

    // Do wyszukiwarki (fragment nazwy, np. "fant" znajdzie "Fantasy")
    List<Genre> findByNameContainingIgnoreCase(String name);
}