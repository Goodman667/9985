package com.example.phq9assessment.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_alerts")
public class EmotionAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "alert_type")
    private String alertType;
    
    @Column(name = "severity")
    private String severity;
    
    @Column(name = "emotion_score")
    private Double emotionScore;
    
    @Column(name = "trigger_source")
    private String triggerSource;
    
    @Lob
    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;
    
    @Lob
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;
    
    @Column(name = "is_read")
    private Boolean isRead;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
    
    @ManyToOne
    @JoinColumn(name = "assessment_record_id")
    private AssessmentRecord assessmentRecord;

    public EmotionAlert() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
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

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Double getEmotionScore() {
        return emotionScore;
    }

    public void setEmotionScore(Double emotionScore) {
        this.emotionScore = emotionScore;
    }

    public String getTriggerSource() {
        return triggerSource;
    }

    public void setTriggerSource(String triggerSource) {
        this.triggerSource = triggerSource;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public AssessmentRecord getAssessmentRecord() {
        return assessmentRecord;
    }

    public void setAssessmentRecord(AssessmentRecord assessmentRecord) {
        this.assessmentRecord = assessmentRecord;
    }
}
