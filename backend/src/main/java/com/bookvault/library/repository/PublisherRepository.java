package com.bookvault.library.repository;

import com.bookvault.library.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    // Dzięki temu można  wyszukać wydawnictwo po nazwie
}