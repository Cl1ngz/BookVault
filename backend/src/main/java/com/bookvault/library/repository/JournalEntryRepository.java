package com.bookvault.library.repository;

import com.bookvault.library.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Integer> {
    List<JournalEntry> findByReadingLog_IdOrderByEntryDateDescCreatedAtDesc(Integer readingLogId);
    List<JournalEntry> findByReadingLog_Reader_IdOrderByEntryDateDescCreatedAtDesc(Integer readerId);
}
