package com.example.biblioteka.repository;

import com.example.biblioteka.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Integer> {
    // Rozszerzenie JpaRepository daje gotowe metody:
    // findAll(), findById(), save(), deleteById() itd.
}