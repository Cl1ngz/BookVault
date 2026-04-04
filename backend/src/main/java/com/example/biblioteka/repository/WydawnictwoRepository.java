package com.example.biblioteka.repository;

import com.example.biblioteka.model.Wydawnictwo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WydawnictwoRepository extends JpaRepository<Wydawnictwo, Integer> {
    // Dzięki temu można  wyszukać wydawnictwo po nazwie
}