package com.example.phq9assessment.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_records")
public class AssessmentRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "q1")
    private int q1;
    
    @Column(name = "q2")
    private int q2;
    
    @Column(name = "q3")
    private int q3;
    
    @Column(name = "q4")
    private int q4;
    
    @Column(name = "q5")
    private int q5;
    
    @Column(name = "q6")
    private int q6;
    
    @Column(name = "q7")
    private int q7;
    
    @Column(name = "q8")
    private int q8;
    
    @Column(name = "q9")
    private int q9;
    
    @Column(name = "total_score")
    private int totalScore;
    
    @Column(name = "level")
    private String level;
    
    @Column(name = "sentiment_text", length = 2000)
    private String sentimentText;
    
    @Column(name = "sentiment_score")
    private Double sentimentScore;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "ml_risk_score")
    private Double mlRiskScore;
    
    @Column(name = "anomaly_detected")
    private Boolean anomalyDetected;
    
    @Column(name = "questionnaire_code")
    private String questionnaireCode;
    
    @Lob
    @Column(name = "voice_features", columnDefinition = "TEXT")
    private String voiceFeatures;
    
    @Column(name = "voice_emotion_score")
    private Double voiceEmotionScore;
    
    @Column(name = "answers_json", length = 2000)
    private String answersJson;
    
    @Lob
    @Column(name = "camera_data", columnDefinition = "TEXT")
    private String cameraData;

    public AssessmentRecord() {
        this.createdAt = LocalDateTime.now();
        this.questionnaireCode = "PHQ-9";
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

    public int getQ1() {
        return q1;
    }

    public void setQ1(int q1) {
        this.q1 = q1;
    }

    public int getQ2() {
        return q2;
    }

    public void setQ2(int q2) {
        this.q2 = q2;
    }

    public int getQ3() {
        return q3;
    }

    public void setQ3(int q3) {
        this.q3 = q3;
    }

    public int getQ4() {
        return q4;
    }

    public void setQ4(int q4) {
        this.q4 = q4;
    }

    public int getQ5() {
        return q5;
    }

    public void setQ5(int q5) {
        this.q5 = q5;
    }

    public int getQ6() {
        return q6;
    }

    public void setQ6(int q6) {
        this.q6 = q6;
    }

    public int getQ7() {
        return q7;
    }

    public void setQ7(int q7) {
        this.q7 = q7;
    }

    public int getQ8() {
        return q8;
    }

    public void setQ8(int q8) {
        this.q8 = q8;
    }

    public int getQ9() {
        return q9;
    }

    public void setQ9(int q9) {
        this.q9 = q9;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSentimentText() {
        return sentimentText;
    }

    public void setSentimentText(String sentimentText) {
        this.sentimentText = sentimentText;
    }

    public Double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getMlRiskScore() {
        return mlRiskScore;
    }

    public void setMlRiskScore(Double mlRiskScore) {
        this.mlRiskScore = mlRiskScore;
    }

    public Boolean getAnomalyDetected() {
        return anomalyDetected;
    }

    public void setAnomalyDetected(Boolean anomalyDetected) {
        this.anomalyDetected = anomalyDetected;
    }

    public String getQuestionnaireCode() {
        return questionnaireCode;
    }

    public void setQuestionnaireCode(String questionnaireCode) {
        this.questionnaireCode = questionnaireCode;
    }

    public String getVoiceFeatures() {
        return voiceFeatures;
    }

    public void setVoiceFeatures(String voiceFeatures) {
        this.voiceFeatures = voiceFeatures;
    }

    public Double getVoiceEmotionScore() {
        return voiceEmotionScore;
    }

    public void setVoiceEmotionScore(Double voiceEmotionScore) {
        this.voiceEmotionScore = voiceEmotionScore;
    }

    public String getAnswersJson() {
        return answersJson;
    }

    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
    }

    public String getCameraData() {
        return cameraData;
    }

    public void setCameraData(String cameraData) {
        this.cameraData = cameraData;
    }
}
