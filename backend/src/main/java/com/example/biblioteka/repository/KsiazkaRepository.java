package com.example.biblioteka.repository;

import com.example.biblioteka.model.Ksiazka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KsiazkaRepository extends JpaRepository<Ksiazka, Integer> {
}