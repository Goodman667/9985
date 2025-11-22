package com.example.phq9assessment.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cognitive_patterns")
public class CognitivePattern {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;
    
    @Column(name = "pattern_type")
    private String patternType;
    
    @Lob
    @Column(name = "evidence_text", columnDefinition = "TEXT")
    private String evidenceText;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Lob
    @Column(name = "cbt_challenge", columnDefinition = "TEXT")
    private String cbtChallenge;
    
    @Lob
    @Column(name = "reframing_suggestion", columnDefinition = "TEXT")
    private String reframingSuggestion;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public CognitivePattern() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public String getEvidenceText() {
        return evidenceText;
    }

    public void setEvidenceText(String evidenceText) {
        this.evidenceText = evidenceText;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getCbtChallenge() {
        return cbtChallenge;
    }

    public void setCbtChallenge(String cbtChallenge) {
        this.cbtChallenge = cbtChallenge;
    }

    public String getReframingSuggestion() {
        return reframingSuggestion;
    }

    public void setReframingSuggestion(String reframingSuggestion) {
        this.reframingSuggestion = reframingSuggestion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
