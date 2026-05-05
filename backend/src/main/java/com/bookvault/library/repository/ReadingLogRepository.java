package com.bookvault.library.repository;

import com.bookvault.library.model.ReadingLog;
import com.bookvault.library.model.ReadingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, Integer> {
    List<ReadingLog> findByReader_Id(Integer readerId);
    List<ReadingLog> findByReader_IdAndStatus(Integer readerId, ReadingStatus status);
    Optional<ReadingLog> findByReader_IdAndBook_Id(Integer readerId, Integer bookId);
    long countByReader_IdAndStatus(Integer readerId, ReadingStatus status);

    @Query("SELECT COUNT(rl) FROM ReadingLog rl WHERE rl.reader.id = :readerId AND rl.status = :status AND YEAR(rl.finishedAt) = :year")
    long countByReader_IdAndStatusAndFinishedAtYear(@Param("readerId") Integer readerId,
                                                     @Param("status") ReadingStatus status,
                                                     @Param("year") int year);
}


