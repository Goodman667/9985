package com.example.phq9assessment.repository;

import com.example.phq9assessment.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    
    List<JournalEntry> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<JournalEntry> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(String userId, LocalDateTime start, LocalDateTime end);
    
    List<JournalEntry> findByUserIdAndEntryTypeOrderByCreatedAtDesc(String userId, String entryType);
}
