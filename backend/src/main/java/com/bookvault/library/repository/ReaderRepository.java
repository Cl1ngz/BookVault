package com.bookvault.library.repository;

import com.bookvault.library.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Integer> {

    List<Reader> findByUsernameContainingIgnoreCase(String username);

    List<Reader> findByNationalityContainingIgnoreCase(String nationality);

    Optional<Reader> findByEmail(String email);

    Optional<Reader> findByEmailIgnoreCase(String email);

    Optional<Reader> findByShareToken(String shareToken);
}
