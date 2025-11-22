package com.example.phq9assessment.repository;

import com.example.phq9assessment.entity.LifeQualityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LifeQualityMetricsRepository extends JpaRepository<LifeQualityMetrics, Long> {
    
    List<LifeQualityMetrics> findByUserIdOrderByRecordedAtDesc(String userId);
    
    List<LifeQualityMetrics> findByUserIdAndRecordedAtBetweenOrderByRecordedAtDesc(String userId, LocalDateTime start, LocalDateTime end);
}
