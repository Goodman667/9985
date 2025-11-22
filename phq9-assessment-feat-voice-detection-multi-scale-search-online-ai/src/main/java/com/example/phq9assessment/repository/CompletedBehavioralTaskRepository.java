package com.example.phq9assessment.repository;

import com.example.phq9assessment.entity.CompletedBehavioralTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompletedBehavioralTaskRepository extends JpaRepository<CompletedBehavioralTask, Long> {
    
    List<CompletedBehavioralTask> findByUserIdOrderByAssignedAtDesc(String userId);
    
    List<CompletedBehavioralTask> findByUserIdAndCompletedOrderByAssignedAtDesc(String userId, Boolean completed);
    
    List<CompletedBehavioralTask> findByUserIdAndAssignedAtBetweenOrderByAssignedAtDesc(String userId, LocalDateTime start, LocalDateTime end);
    
    List<CompletedBehavioralTask> findByUserIdAndCategoryOrderByAssignedAtDesc(String userId, String category);
}
