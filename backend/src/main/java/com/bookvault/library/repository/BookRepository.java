package com.bookvault.library.repository;

import com.bookvault.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    // 1. Szukanie po tytule (title)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // 2. Szukanie po nazwie gatunku (genres -> name)
    // Używamy findDistinct, bo jedna książka może pasować do wielu gatunków
    List<Book> findDistinctByGenres_NameContainingIgnoreCase(String genreName);

    // 3. Szukanie po nazwie serii (series -> name)
    List<Book> findBySeries_NameContainingIgnoreCase(String seriesName);

    // 4. Szukanie po nazwisku autora (author -> lastName)
    List<Book> findByAuthor_LastNameContainingIgnoreCase(String lastName);
}