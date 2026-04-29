package com.bookvault.library.repository;

import com.bookvault.library.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {

    // Wyszukiwanie wydawnictwa po nazwie (np. "PWN", "Helion")
    List<Publisher> findByNameContainingIgnoreCase(String name);

    // Wyszukiwanie po nazwisku właściciela
    List<Publisher> findByOwnerContainingIgnoreCase(String owner);

    // Wyszukiwanie po roku założenia
    List<Publisher> findByFoundationYear(Integer year);
}