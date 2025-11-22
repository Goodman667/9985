package com.example.phq9assessment.repository;

import com.example.phq9assessment.entity.CognitivePattern;
import com.example.phq9assessment.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CognitivePatternRepository extends JpaRepository<CognitivePattern, Long> {
    
    List<CognitivePattern> findByJournalEntry(JournalEntry journalEntry);
    
    List<CognitivePattern> findByPatternType(String patternType);
}
