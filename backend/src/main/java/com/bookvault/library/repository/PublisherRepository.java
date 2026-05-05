package com.bookvault.library.repository;

import com.bookvault.library.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {

    List<Publisher> findByNameContainingIgnoreCase(String name);

    List<Publisher> findByOwnerContainingIgnoreCase(String owner);

    List<Publisher> findByFoundationYear(Integer year);
}