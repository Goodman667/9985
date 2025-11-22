package com.example.phq9assessment.repository;

import com.example.phq9assessment.entity.EmotionAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmotionAlertRepository extends JpaRepository<EmotionAlert, Long> {
    
    List<EmotionAlert> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<EmotionAlert> findByUserIdAndIsReadOrderByCreatedAtDesc(String userId, Boolean isRead);
    
    List<EmotionAlert> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(String userId, LocalDateTime start, LocalDateTime end);
    
    List<EmotionAlert> findBySeverityOrderByCreatedAtDesc(String severity);
}
