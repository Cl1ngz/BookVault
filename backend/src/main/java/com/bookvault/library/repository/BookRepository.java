package com.bookvault.library.repository;

import com.bookvault.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
	List<Book> findDistinctByTytulContainingIgnoreCase(String tytul);

	List<Book> findDistinctByGenresNazwaIgnoreCase(String nazwa);

	@Query(
			value = """
					SELECT DISTINCT k.*
					FROM biblioteka.ksiazki k
					JOIN biblioteka.serie s ON s.id_serii = k.id_serii
					WHERE LOWER(s.nazwa) LIKE LOWER(CONCAT('%', :seriesName, '%'))
					""",
			nativeQuery = true
	)
	List<Book> findDistinctBySeriesNameContainingIgnoreCase(@Param("seriesName") String seriesName);
}