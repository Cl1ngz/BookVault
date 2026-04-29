package com.bookvault.library.repository;

import com.bookvault.library.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Integer> {

    // Wyszukiwanie czytelnika po nazwisku (kluczowe dla obsługi biblioteki)
    List<Reader> findByLastNameContainingIgnoreCase(String lastName);

    // Wyszukiwanie czytelnika po imieniu
    List<Reader> findByFirstNameContainingIgnoreCase(String firstName);

    // Wyszukiwanie po narodowości (przydatne do statystyk)
    List<Reader> findByNationalityContainingIgnoreCase(String nationality);
}