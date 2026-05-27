package com.bookvault.library.repository;

import com.bookvault.library.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    Optional<Genre> findByNameIgnoreCase(String name);

    List<Genre> findByNameContainingIgnoreCase(String name);

    @Query(value = """
            SELECT g.id_gatunku, g.nazwa
            FROM biblioteka.gatunki g
            LEFT JOIN biblioteka.ksiazka_gatunek kg ON g.id_gatunku = kg.id_gatunku
            GROUP BY g.id_gatunku, g.nazwa
            ORDER BY COUNT(kg.id_ksiazki) DESC
            """, nativeQuery = true)
    List<Genre> findAllOrderByBookCountDesc();
}