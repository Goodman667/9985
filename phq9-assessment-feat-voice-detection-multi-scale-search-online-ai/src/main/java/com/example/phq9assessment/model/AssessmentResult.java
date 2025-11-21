package com.example.phq9assessment.model;

import com.example.phq9assessment.service.MachineLearningService;
import com.example.phq9assessment.service.RecommendationService;
import com.example.phq9assessment.service.SentimentAnalysisService;
import com.example.phq9assessment.service.AnomalyDetectionService;
import com.example.phq9assessment.service.VoiceDetectionService;
import com.example.phq9assessment.service.OnlineAIService;

import java.util.List;

public class AssessmentResult {
    private int totalScore;
    private String level;
    private String levelText;
    private String suggestion;
    private boolean highRisk;
    
    private Double mlRiskScore;
    private String mlRiskLevel;
    private SentimentAnalysisService.SentimentAnalysisResult sentimentAnalysis;
    private List<RecommendationService.Recommendation> recommendations;
    private MachineLearningService.TrendAnalysis trendAnalysis;
    private MachineLearningService.ClusterResult clusterResult;
    private AnomalyDetectionService.AnomalyDetectionResult anomalyDetection;
    
    private boolean hasHistoricalData;
    private List<Integer> historicalScores;
    
    private VoiceDetectionService.VoiceAnalysisResult voiceAnalysis;
    private OnlineAIService.AIEnhancementResult aiEnhancement;
    private String questionnaireCode;

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

    public String getLevelText() {
        return levelText;
    }

    public void setLevelText(String levelText) {
        this.levelText = levelText;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public boolean isHighRisk() {
        return highRisk;
    }

    public void setHighRisk(boolean highRisk) {
        this.highRisk = highRisk;
    }

    public Double getMlRiskScore() {
        return mlRiskScore;
    }

    public void setMlRiskScore(Double mlRiskScore) {
        this.mlRiskScore = mlRiskScore;
    }

    public String getMlRiskLevel() {
        return mlRiskLevel;
    }

    public void setMlRiskLevel(String mlRiskLevel) {
        this.mlRiskLevel = mlRiskLevel;
    }

    public SentimentAnalysisService.SentimentAnalysisResult getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void setSentimentAnalysis(SentimentAnalysisService.SentimentAnalysisResult sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }

    public List<RecommendationService.Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationService.Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public MachineLearningService.TrendAnalysis getTrendAnalysis() {
        return trendAnalysis;
    }

    public void setTrendAnalysis(MachineLearningService.TrendAnalysis trendAnalysis) {
        this.trendAnalysis = trendAnalysis;
    }

    public MachineLearningService.ClusterResult getClusterResult() {
        return clusterResult;
    }

    public void setClusterResult(MachineLearningService.ClusterResult clusterResult) {
        this.clusterResult = clusterResult;
    }

    public AnomalyDetectionService.AnomalyDetectionResult getAnomalyDetection() {
        return anomalyDetection;
    }

    public void setAnomalyDetection(AnomalyDetectionService.AnomalyDetectionResult anomalyDetection) {
        this.anomalyDetection = anomalyDetection;
    }

    public boolean isHasHistoricalData() {
        return hasHistoricalData;
    }

    public void setHasHistoricalData(boolean hasHistoricalData) {
        this.hasHistoricalData = hasHistoricalData;
    }

    public List<Integer> getHistoricalScores() {
        return historicalScores;
    }

    public void setHistoricalScores(List<Integer> historicalScores) {
        this.historicalScores = historicalScores;
    }

    public VoiceDetectionService.VoiceAnalysisResult getVoiceAnalysis() {
        return voiceAnalysis;
    }

    public void setVoiceAnalysis(VoiceDetectionService.VoiceAnalysisResult voiceAnalysis) {
        this.voiceAnalysis = voiceAnalysis;
    }

    public OnlineAIService.AIEnhancementResult getAiEnhancement() {
        return aiEnhancement;
    }

    public void setAiEnhancement(OnlineAIService.AIEnhancementResult aiEnhancement) {
        this.aiEnhancement = aiEnhancement;
    }

    public String getQuestionnaireCode() {
        return questionnaireCode;
    }

    public void setQuestionnaireCode(String questionnaireCode) {
        this.questionnaireCode = questionnaireCode;
    }
}
