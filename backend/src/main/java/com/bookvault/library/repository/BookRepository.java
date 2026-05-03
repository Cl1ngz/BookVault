package com.bookvault.library.repository;

import com.bookvault.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findDistinctByGenres_NameContainingIgnoreCase(String genreName);
    List<Book> findBySeries_NameContainingIgnoreCase(String seriesName);
    List<Book> findByAuthor_LastNameContainingIgnoreCase(String lastName);
    List<Book> findBySeries_Id(Integer seriesId);
    List<Book> findByAuthor_Id(Integer authorId);
}