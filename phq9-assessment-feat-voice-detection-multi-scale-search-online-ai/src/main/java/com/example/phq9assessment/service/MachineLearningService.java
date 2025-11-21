package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.AssessmentRecord;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineLearningService {
    
    public double calculateRiskScore(int[] answers, String sentimentText, Double sentimentScore) {
        double baseRiskScore = calculateBaseRisk(answers);
        
        double patternRisk = detectRiskPattern(answers);
        
        double sentimentRisk = 0.0;
        if (sentimentScore != null && sentimentScore < -0.3) {
            sentimentRisk = Math.abs(sentimentScore) * 0.3;
        }
        
        double consistencyRisk = calculateConsistencyRisk(answers);
        
        double totalRisk = (baseRiskScore * 0.5) + (patternRisk * 0.25) + 
                          (sentimentRisk * 0.15) + (consistencyRisk * 0.1);
        
        return Math.min(1.0, Math.max(0.0, totalRisk));
    }
    
    private double calculateBaseRisk(int[] answers) {
        int totalScore = 0;
        for (int answer : answers) {
            totalScore += answer;
        }
        
        return Math.min(1.0, totalScore / 27.0);
    }
    
    private double detectRiskPattern(int[] answers) {
        double riskScore = 0.0;
        
        if (answers[8] > 0) {
            riskScore += 0.4;
        }
        
        int highScoreCount = 0;
        for (int i = 0; i < 9; i++) {
            if (answers[i] >= 2) {
                highScoreCount++;
            }
        }
        if (highScoreCount >= 5) {
            riskScore += 0.3;
        }
        
        if (answers[0] >= 2 && answers[1] >= 2) {
            riskScore += 0.2;
        }
        
        if (answers[5] >= 2 && answers[8] > 0) {
            riskScore += 0.3;
        }
        
        return Math.min(1.0, riskScore);
    }
    
    private double calculateConsistencyRisk(int[] answers) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int answer : answers) {
            stats.addValue(answer);
        }
        
        double variance = stats.getVariance();
        double mean = stats.getMean();
        
        if (variance < 0.5 && mean > 1.5) {
            return 0.2;
        }
        
        return 0.0;
    }
    
    public TrendAnalysis analyzeTrend(List<AssessmentRecord> historicalRecords) {
        if (historicalRecords == null || historicalRecords.size() < 2) {
            return new TrendAnalysis("insufficient_data", 0.0, null);
        }
        
        double[] scores = new double[historicalRecords.size()];
        for (int i = 0; i < historicalRecords.size(); i++) {
            scores[i] = historicalRecords.get(i).getTotalScore();
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics(scores);
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        
        String trend = "stable";
        double slope = 0.0;
        
        if (scores.length >= 3) {
            slope = calculateSlope(scores);
            
            if (slope > 1.0) {
                trend = "worsening";
            } else if (slope < -1.0) {
                trend = "improving";
            } else {
                trend = "stable";
            }
        }
        
        Double prediction = null;
        if (scores.length >= 3) {
            prediction = scores[scores.length - 1] + slope;
            prediction = Math.max(0, Math.min(27, prediction));
        }
        
        return new TrendAnalysis(trend, slope, prediction);
    }
    
    private double calculateSlope(double[] values) {
        int n = values.length;
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = i;
        }
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += values[i];
            sumXY += x[i] * values[i];
            sumX2 += x[i] * x[i];
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
    
    public ClusterResult clusterUser(int[] answers, double riskScore) {
        int totalScore = 0;
        for (int answer : answers) {
            totalScore += answer;
        }
        
        String cluster;
        String intervention;
        
        if (totalScore <= 4) {
            cluster = "low_risk";
            intervention = "继续保持健康的生活方式，定期进行自我评估";
        } else if (totalScore <= 9) {
            cluster = "mild_risk";
            intervention = "建议学习情绪管理技巧，增加社交活动，保持规律作息";
        } else if (totalScore <= 14) {
            cluster = "moderate_risk";
            intervention = "建议咨询心理健康专业人士，考虑心理咨询或认知行为疗法";
        } else if (totalScore <= 19) {
            cluster = "high_risk";
            intervention = "强烈建议寻求专业心理治疗，可能需要药物治疗配合心理治疗";
        } else {
            cluster = "severe_risk";
            intervention = "需要立即寻求专业医疗帮助，可能需要住院治疗或密集的门诊治疗";
        }
        
        if (answers[8] > 0) {
            intervention = "⚠️ 检测到自伤风险，请立即联系心理危机热线或前往最近的急诊室。" + intervention;
        }
        
        return new ClusterResult(cluster, intervention);
    }
    
    public static class TrendAnalysis {
        private String trend;
        private double slope;
        private Double predictedNextScore;
        
        public TrendAnalysis(String trend, double slope, Double predictedNextScore) {
            this.trend = trend;
            this.slope = slope;
            this.predictedNextScore = predictedNextScore;
        }
        
        public String getTrend() {
            return trend;
        }
        
        public double getSlope() {
            return slope;
        }
        
        public Double getPredictedNextScore() {
            return predictedNextScore;
        }
        
        public String getTrendText() {
            switch (trend) {
                case "improving": return "改善中";
                case "worsening": return "恶化中";
                case "stable": return "稳定";
                default: return "数据不足";
            }
        }
    }
    
    public static class ClusterResult {
        private String cluster;
        private String intervention;
        
        public ClusterResult(String cluster, String intervention) {
            this.cluster = cluster;
            this.intervention = intervention;
        }
        
        public String getCluster() {
            return cluster;
        }
        
        public String getIntervention() {
            return intervention;
        }
        
        public String getClusterText() {
            switch (cluster) {
                case "low_risk": return "低风险群体";
                case "mild_risk": return "轻度风险群体";
                case "moderate_risk": return "中度风险群体";
                case "high_risk": return "高风险群体";
                case "severe_risk": return "严重风险群体";
                default: return "未分类";
            }
        }
    }
}
