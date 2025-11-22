package com.example.phq9assessment.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "life_quality_metrics")
public class LifeQualityMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "sleep_quality")
    private Double sleepQuality;
    
    @Column(name = "social_interaction")
    private Double socialInteraction;
    
    @Column(name = "physical_activity")
    private Double physicalActivity;
    
    @Column(name = "work_productivity")
    private Double workProductivity;
    
    @Column(name = "satisfaction")
    private Double satisfaction;
    
    @Column(name = "relationships")
    private Double relationships;
    
    @Column(name = "self_care")
    private Double selfCare;
    
    @Column(name = "enjoyable_activities")
    private Double enjoyableActivities;
    
    @Column(name = "overall_score")
    private Double overallScore;
    
    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
    
    @ManyToOne
    @JoinColumn(name = "assessment_record_id")
    private AssessmentRecord assessmentRecord;

    public LifeQualityMetrics() {
        this.recordedAt = LocalDateTime.now();
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

    public Double getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(Double sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public Double getSocialInteraction() {
        return socialInteraction;
    }

    public void setSocialInteraction(Double socialInteraction) {
        this.socialInteraction = socialInteraction;
    }

    public Double getPhysicalActivity() {
        return physicalActivity;
    }

    public void setPhysicalActivity(Double physicalActivity) {
        this.physicalActivity = physicalActivity;
    }

    public Double getWorkProductivity() {
        return workProductivity;
    }

    public void setWorkProductivity(Double workProductivity) {
        this.workProductivity = workProductivity;
    }

    public Double getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Double satisfaction) {
        this.satisfaction = satisfaction;
    }

    public Double getRelationships() {
        return relationships;
    }

    public void setRelationships(Double relationships) {
        this.relationships = relationships;
    }

    public Double getSelfCare() {
        return selfCare;
    }

    public void setSelfCare(Double selfCare) {
        this.selfCare = selfCare;
    }

    public Double getEnjoyableActivities() {
        return enjoyableActivities;
    }

    public void setEnjoyableActivities(Double enjoyableActivities) {
        this.enjoyableActivities = enjoyableActivities;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public AssessmentRecord getAssessmentRecord() {
        return assessmentRecord;
    }

    public void setAssessmentRecord(AssessmentRecord assessmentRecord) {
        this.assessmentRecord = assessmentRecord;
    }
}
