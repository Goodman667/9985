package com.example.phq9assessment.repository;

import com.example.phq9assessment.entity.AssessmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {
    
    List<AssessmentRecord> findByUserIdOrderByCreatedAtDesc(String userId);
    
    @Query("SELECT a FROM AssessmentRecord a WHERE a.userId = ?1 AND a.createdAt > ?2 ORDER BY a.createdAt DESC")
    List<AssessmentRecord> findRecentByUserId(String userId, LocalDateTime since);
    
    @Query("SELECT AVG(a.totalScore) FROM AssessmentRecord a WHERE a.userId = ?1")
    Double getAverageScoreByUserId(String userId);
    
    @Query("SELECT a FROM AssessmentRecord a WHERE a.mlRiskScore > ?1")
    List<AssessmentRecord> findHighRiskAssessments(double threshold);
}
