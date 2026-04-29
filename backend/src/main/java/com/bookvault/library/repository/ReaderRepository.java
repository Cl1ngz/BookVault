package com.bookvault.library.repository;

import com.bookvault.library.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Integer> {

    // Search by username
    List<Reader> findByUsernameContainingIgnoreCase(String username);

    // Search by nationality (useful for stats)
    List<Reader> findByNationalityContainingIgnoreCase(String nationality);
}
