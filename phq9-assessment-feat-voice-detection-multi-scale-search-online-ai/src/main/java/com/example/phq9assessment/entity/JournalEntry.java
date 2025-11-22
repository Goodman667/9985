package com.example.phq9assessment.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "entry_type")
    private String entryType;
    
    @Column(name = "mood_score")
    private Double moodScore;
    
    @Lob
    @Column(name = "voice_features", columnDefinition = "TEXT")
    private String voiceFeatures;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Lob
    @Column(name = "cognitive_patterns_json", columnDefinition = "TEXT")
    private String cognitivePatternsJson;
    
    @Lob
    @Column(name = "cbt_suggestions", columnDefinition = "TEXT")
    private String cbtSuggestions;

    public JournalEntry() {
        this.createdAt = LocalDateTime.now();
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public Double getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Double moodScore) {
        this.moodScore = moodScore;
    }

    public String getVoiceFeatures() {
        return voiceFeatures;
    }

    public void setVoiceFeatures(String voiceFeatures) {
        this.voiceFeatures = voiceFeatures;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCognitivePatternsJson() {
        return cognitivePatternsJson;
    }

    public void setCognitivePatternsJson(String cognitivePatternsJson) {
        this.cognitivePatternsJson = cognitivePatternsJson;
    }

    public String getCbtSuggestions() {
        return cbtSuggestions;
    }

    public void setCbtSuggestions(String cbtSuggestions) {
        this.cbtSuggestions = cbtSuggestions;
    }
}
