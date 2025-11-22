package com.example.phq9assessment.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "completed_behavioral_tasks")
public class CompletedBehavioralTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Lob
    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;
    
    @Column(name = "difficulty_level")
    private String difficultyLevel;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "completed")
    private Boolean completed;
    
    @Column(name = "completion_rating")
    private Integer completionRating;
    
    @Lob
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "mood_before")
    private Double moodBefore;
    
    @Column(name = "mood_after")
    private Double moodAfter;

    public CompletedBehavioralTask() {
        this.assignedAt = LocalDateTime.now();
        this.completed = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getCompletionRating() {
        return completionRating;
    }

    public void setCompletionRating(Integer completionRating) {
        this.completionRating = completionRating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Double getMoodBefore() {
        return moodBefore;
    }

    public void setMoodBefore(Double moodBefore) {
        this.moodBefore = moodBefore;
    }

    public Double getMoodAfter() {
        return moodAfter;
    }

    public void setMoodAfter(Double moodAfter) {
        this.moodAfter = moodAfter;
    }
}
