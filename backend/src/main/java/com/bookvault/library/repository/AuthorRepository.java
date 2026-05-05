package com.bookvault.library.repository;

import com.bookvault.library.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {

    List<Author> findByLastNameContainingIgnoreCase(String lastName);

    List<Author> findByFirstNameContainingIgnoreCase(String firstName);

    List<Author> findByNationalityContainingIgnoreCase(String nationality);
}