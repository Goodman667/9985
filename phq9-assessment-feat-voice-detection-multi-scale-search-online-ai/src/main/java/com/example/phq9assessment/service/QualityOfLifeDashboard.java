package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.entity.LifeQualityMetrics;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import com.example.phq9assessment.repository.LifeQualityMetricsRepository;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QualityOfLifeDashboard {
    
    @Autowired
    private LifeQualityMetricsRepository metricsRepository;
    
    @Autowired
    private AssessmentRecordRepository assessmentRecordRepository;
    
    private static final List<String> DIMENSIONS = Arrays.asList(
        "sleepQuality", "socialInteraction", "physicalActivity", "workProductivity",
        "satisfaction", "relationships", "selfCare", "enjoyableActivities"
    );
    
    private static final Map<String, String> DIMENSION_LABELS = new HashMap<>();
    
    static {
        DIMENSION_LABELS.put("sleepQuality", "睡眠质量");
        DIMENSION_LABELS.put("socialInteraction", "社交互动");
        DIMENSION_LABELS.put("physicalActivity", "身体活动");
        DIMENSION_LABELS.put("workProductivity", "工作效率");
        DIMENSION_LABELS.put("satisfaction", "生活满意度");
        DIMENSION_LABELS.put("relationships", "人际关系");
        DIMENSION_LABELS.put("selfCare", "自我照顾");
        DIMENSION_LABELS.put("enjoyableActivities", "愉快活动");
    }
    
    public LifeQualityMetrics recordMetrics(String userId, Map<String, Double> dimensionScores, String notes) {
        LifeQualityMetrics metrics = new LifeQualityMetrics();
        metrics.setUserId(userId);
        metrics.setSleepQuality(dimensionScores.get("sleepQuality"));
        metrics.setSocialInteraction(dimensionScores.get("socialInteraction"));
        metrics.setPhysicalActivity(dimensionScores.get("physicalActivity"));
        metrics.setWorkProductivity(dimensionScores.get("workProductivity"));
        metrics.setSatisfaction(dimensionScores.get("satisfaction"));
        metrics.setRelationships(dimensionScores.get("relationships"));
        metrics.setSelfCare(dimensionScores.get("selfCare"));
        metrics.setEnjoyableActivities(dimensionScores.get("enjoyableActivities"));
        metrics.setNotes(notes);
        
        double overallScore = dimensionScores.values().stream()
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
        
        metrics.setOverallScore(Math.round(overallScore * 10) / 10.0);
        
        List<AssessmentRecord> recentAssessments = 
            assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        if (!recentAssessments.isEmpty()) {
            AssessmentRecord latest = recentAssessments.get(recentAssessments.size() - 1);
            metrics.setAssessmentRecord(latest);
        }
        
        return metricsRepository.save(metrics);
    }
    
    public Map<String, Object> getDashboardData(String userId) {
        List<LifeQualityMetrics> allMetrics = metricsRepository.findByUserIdOrderByRecordedAtDesc(userId);
        
        Map<String, Object> dashboard = new HashMap<>();
        
        if (allMetrics.isEmpty()) {
            dashboard.put("status", "NO_DATA");
            dashboard.put("message", "暂无生活质量数据");
            return dashboard;
        }
        
        LifeQualityMetrics latest = allMetrics.get(0);
        Map<String, Object> currentScores = extractDimensionScores(latest);
        dashboard.put("currentScores", currentScores);
        dashboard.put("overallScore", latest.getOverallScore());
        dashboard.put("recordedAt", latest.getRecordedAt().toString());
        
        Map<String, Object> trends = analyzeTrends(allMetrics);
        dashboard.put("trends", trends);
        
        Map<String, Object> correlations = analyzeCorrelations(userId, allMetrics);
        dashboard.put("correlations", correlations);
        
        List<String> keyInsights = generateKeyInsights(latest, trends, correlations);
        dashboard.put("keyInsights", keyInsights);
        
        List<String> recommendations = generateRecommendations(latest, trends, correlations);
        dashboard.put("recommendations", recommendations);
        
        Map<String, Object> milestones = trackMilestones(allMetrics);
        dashboard.put("milestones", milestones);
        
        List<Map<String, Object>> timeline = generateTimeline(allMetrics);
        dashboard.put("timeline", timeline);
        
        return dashboard;
    }
    
    private Map<String, Object> extractDimensionScores(LifeQualityMetrics metrics) {
        Map<String, Object> scores = new HashMap<>();
        
        scores.put("sleepQuality", metrics.getSleepQuality());
        scores.put("socialInteraction", metrics.getSocialInteraction());
        scores.put("physicalActivity", metrics.getPhysicalActivity());
        scores.put("workProductivity", metrics.getWorkProductivity());
        scores.put("satisfaction", metrics.getSatisfaction());
        scores.put("relationships", metrics.getRelationships());
        scores.put("selfCare", metrics.getSelfCare());
        scores.put("enjoyableActivities", metrics.getEnjoyableActivities());
        
        return scores;
    }
    
    private Map<String, Object> analyzeTrends(List<LifeQualityMetrics> metrics) {
        Map<String, Object> trends = new HashMap<>();
        
        if (metrics.size() < 2) {
            trends.put("status", "INSUFFICIENT_DATA");
            return trends;
        }
        
        LifeQualityMetrics latest = metrics.get(0);
        LifeQualityMetrics previous = metrics.get(1);
        
        Map<String, Double> changes = new HashMap<>();
        changes.put("sleepQuality", calculateChange(latest.getSleepQuality(), previous.getSleepQuality()));
        changes.put("socialInteraction", calculateChange(latest.getSocialInteraction(), previous.getSocialInteraction()));
        changes.put("physicalActivity", calculateChange(latest.getPhysicalActivity(), previous.getPhysicalActivity()));
        changes.put("workProductivity", calculateChange(latest.getWorkProductivity(), previous.getWorkProductivity()));
        changes.put("satisfaction", calculateChange(latest.getSatisfaction(), previous.getSatisfaction()));
        changes.put("relationships", calculateChange(latest.getRelationships(), previous.getRelationships()));
        changes.put("selfCare", calculateChange(latest.getSelfCare(), previous.getSelfCare()));
        changes.put("enjoyableActivities", calculateChange(latest.getEnjoyableActivities(), previous.getEnjoyableActivities()));
        
        trends.put("changes", changes);
        
        List<String> improving = new ArrayList<>();
        List<String> declining = new ArrayList<>();
        List<String> stable = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : changes.entrySet()) {
            String dimension = DIMENSION_LABELS.get(entry.getKey());
            Double change = entry.getValue();
            
            if (change == null) continue;
            
            if (change > 0.5) {
                improving.add(dimension);
            } else if (change < -0.5) {
                declining.add(dimension);
            } else {
                stable.add(dimension);
            }
        }
        
        trends.put("improving", improving);
        trends.put("declining", declining);
        trends.put("stable", stable);
        
        double overallChange = calculateChange(latest.getOverallScore(), previous.getOverallScore());
        trends.put("overallTrend", overallChange);
        
        return trends;
    }
    
    private Double calculateChange(Double current, Double previous) {
        if (current == null || previous == null) return null;
        return current - previous;
    }
    
    private Map<String, Object> analyzeCorrelations(String userId, List<LifeQualityMetrics> metrics) {
        Map<String, Object> correlations = new HashMap<>();
        
        List<AssessmentRecord> assessments = assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        if (metrics.size() < 3 || assessments.isEmpty()) {
            correlations.put("status", "INSUFFICIENT_DATA");
            return correlations;
        }
        
        List<Double> moodScores = new ArrayList<>();
        List<Map<String, Double>> dimensionScoresArray = new ArrayList<>();
        
        for (LifeQualityMetrics metric : metrics) {
            AssessmentRecord closestAssessment = findClosestAssessment(metric, assessments);
            if (closestAssessment != null) {
                moodScores.add((double) closestAssessment.getTotalScore());
                dimensionScoresArray.add(extractDimensionScoresAsMap(metric));
            }
        }
        
        if (moodScores.size() < 3) {
            correlations.put("status", "INSUFFICIENT_PAIRS");
            return correlations;
        }
        
        Map<String, Double> dimensionCorrelations = new HashMap<>();
        
        for (String dimension : DIMENSIONS) {
            List<Double> dimensionValues = new ArrayList<>();
            
            for (Map<String, Double> scores : dimensionScoresArray) {
                Double value = scores.get(dimension);
                if (value != null) {
                    dimensionValues.add(value);
                }
            }
            
            if (dimensionValues.size() >= 3 && dimensionValues.size() == moodScores.size()) {
                try {
                    PearsonsCorrelation correlation = new PearsonsCorrelation();
                    double[] dim = dimensionValues.stream().mapToDouble(Double::doubleValue).toArray();
                    double[] mood = moodScores.stream().mapToDouble(Double::doubleValue).toArray();
                    
                    double corr = correlation.correlation(dim, mood);
                    dimensionCorrelations.put(dimension, Math.round(corr * 1000) / 1000.0);
                } catch (Exception e) {
                }
            }
        }
        
        correlations.put("dimensionCorrelations", dimensionCorrelations);
        
        List<Map<String, Object>> topFactors = dimensionCorrelations.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(Math.abs(e2.getValue()), Math.abs(e1.getValue())))
            .limit(3)
            .map(e -> {
                Map<String, Object> factor = new HashMap<>();
                factor.put("dimension", DIMENSION_LABELS.get(e.getKey()));
                factor.put("dimensionKey", e.getKey());
                factor.put("correlation", e.getValue());
                factor.put("impact", interpretCorrelation(e.getValue()));
                return factor;
            })
            .collect(Collectors.toList());
        
        correlations.put("topInfluencingFactors", topFactors);
        
        return correlations;
    }
    
    private AssessmentRecord findClosestAssessment(LifeQualityMetrics metric, List<AssessmentRecord> assessments) {
        if (assessments.isEmpty()) return null;
        
        AssessmentRecord closest = assessments.get(0);
        long minDiff = Math.abs(
            java.time.Duration.between(metric.getRecordedAt(), closest.getCreatedAt()).toHours()
        );
        
        for (AssessmentRecord assessment : assessments) {
            long diff = Math.abs(
                java.time.Duration.between(metric.getRecordedAt(), assessment.getCreatedAt()).toHours()
            );
            
            if (diff < minDiff) {
                minDiff = diff;
                closest = assessment;
            }
        }
        
        return minDiff <= 168 ? closest : null;
    }
    
    private Map<String, Double> extractDimensionScoresAsMap(LifeQualityMetrics metrics) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("sleepQuality", metrics.getSleepQuality());
        scores.put("socialInteraction", metrics.getSocialInteraction());
        scores.put("physicalActivity", metrics.getPhysicalActivity());
        scores.put("workProductivity", metrics.getWorkProductivity());
        scores.put("satisfaction", metrics.getSatisfaction());
        scores.put("relationships", metrics.getRelationships());
        scores.put("selfCare", metrics.getSelfCare());
        scores.put("enjoyableActivities", metrics.getEnjoyableActivities());
        return scores;
    }
    
    private String interpretCorrelation(double correlation) {
        double abs = Math.abs(correlation);
        if (abs >= 0.7) return "强相关";
        if (abs >= 0.5) return "中等相关";
        if (abs >= 0.3) return "弱相关";
        return "几乎无相关";
    }
    
    private List<String> generateKeyInsights(LifeQualityMetrics latest, 
                                            Map<String, Object> trends,
                                            Map<String, Object> correlations) {
        List<String> insights = new ArrayList<>();
        
        Map<String, Object> scores = extractDimensionScores(latest);
        
        List<String> strongAreas = new ArrayList<>();
        List<String> weakAreas = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : scores.entrySet()) {
            if (entry.getValue() instanceof Double) {
                Double score = (Double) entry.getValue();
                String label = DIMENSION_LABELS.get(entry.getKey());
                
                if (score != null && score >= 8) {
                    strongAreas.add(label);
                } else if (score != null && score <= 4) {
                    weakAreas.add(label);
                }
            }
        }
        
        if (!strongAreas.isEmpty()) {
            insights.add("您的优势领域：" + String.join("、", strongAreas));
        }
        
        if (!weakAreas.isEmpty()) {
            insights.add("需要改善的领域：" + String.join("、", weakAreas));
        }
        
        @SuppressWarnings("unchecked")
        List<String> improving = (List<String>) trends.get("improving");
        if (improving != null && !improving.isEmpty()) {
            insights.add("正在改善的方面：" + String.join("、", improving));
        }
        
        @SuppressWarnings("unchecked")
        List<String> declining = (List<String>) trends.get("declining");
        if (declining != null && !declining.isEmpty()) {
            insights.add("需要关注的下降趋势：" + String.join("、", declining));
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topFactors = (List<Map<String, Object>>) correlations.get("topInfluencingFactors");
        if (topFactors != null && !topFactors.isEmpty()) {
            Map<String, Object> topFactor = topFactors.get(0);
            insights.add("对情绪影响最大的因素是：" + topFactor.get("dimension"));
        }
        
        return insights;
    }
    
    private List<String> generateRecommendations(LifeQualityMetrics latest,
                                                 Map<String, Object> trends,
                                                 Map<String, Object> correlations) {
        List<String> recommendations = new ArrayList<>();
        
        Map<String, Object> scores = extractDimensionScores(latest);
        
        for (Map.Entry<String, Object> entry : scores.entrySet()) {
            if (entry.getValue() instanceof Double) {
                Double score = (Double) entry.getValue();
                String dimension = entry.getKey();
                
                if (score != null && score <= 4) {
                    recommendations.addAll(getDimensionRecommendations(dimension));
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topFactors = (List<Map<String, Object>>) correlations.get("topInfluencingFactors");
        if (topFactors != null && !topFactors.isEmpty()) {
            Map<String, Object> topFactor = topFactors.get(0);
            String dimensionKey = (String) topFactor.get("dimensionKey");
            recommendations.add("优先改善 " + topFactor.get("dimension") + "，这可能对整体情绪有显著帮助");
        }
        
        return recommendations;
    }
    
    private List<String> getDimensionRecommendations(String dimension) {
        List<String> recs = new ArrayList<>();
        
        switch (dimension) {
            case "sleepQuality":
                recs.add("改善睡眠质量：保持规律作息，创造舒适睡眠环境");
                break;
            case "socialInteraction":
                recs.add("增加社交互动：每周至少与朋友或家人联系一次");
                break;
            case "physicalActivity":
                recs.add("提升身体活动：每天进行至少30分钟的运动");
                break;
            case "workProductivity":
                recs.add("优化工作效率：使用番茄工作法，合理安排任务优先级");
                break;
            case "satisfaction":
                recs.add("提升满意度：每天记录三件感恩的事情");
                break;
            case "relationships":
                recs.add("改善人际关系：主动关心他人，练习积极倾听");
                break;
            case "selfCare":
                recs.add("加强自我照顾：每天为自己安排愉快的小时光");
                break;
            case "enjoyableActivities":
                recs.add("增加愉快活动：每周尝试一项新的爱好或活动");
                break;
        }
        
        return recs;
    }
    
    private Map<String, Object> trackMilestones(List<LifeQualityMetrics> metrics) {
        Map<String, Object> milestones = new HashMap<>();
        
        if (metrics.isEmpty()) return milestones;
        
        int recordCount = metrics.size();
        milestones.put("totalRecords", recordCount);
        
        if (recordCount >= 10) {
            milestones.put("consistency", "恭喜！您已经坚持记录10次以上");
        } else if (recordCount >= 5) {
            milestones.put("consistency", "很好！继续保持记录习惯");
        }
        
        double highestOverall = metrics.stream()
            .mapToDouble(m -> m.getOverallScore() != null ? m.getOverallScore() : 0)
            .max()
            .orElse(0);
        
        milestones.put("highestOverallScore", highestOverall);
        
        if (metrics.size() >= 2) {
            double firstScore = metrics.get(metrics.size() - 1).getOverallScore();
            double latestScore = metrics.get(0).getOverallScore();
            double improvement = latestScore - firstScore;
            
            if (improvement > 2) {
                milestones.put("improvement", "总体生活质量显著提升！");
            } else if (improvement > 0) {
                milestones.put("improvement", "生活质量稳步改善中");
            }
        }
        
        return milestones;
    }
    
    private List<Map<String, Object>> generateTimeline(List<LifeQualityMetrics> metrics) {
        return metrics.stream()
            .sorted(Comparator.comparing(LifeQualityMetrics::getRecordedAt))
            .map(m -> {
                Map<String, Object> point = new HashMap<>();
                point.put("date", m.getRecordedAt().toLocalDate().toString());
                point.put("overallScore", m.getOverallScore());
                point.put("dimensions", extractDimensionScores(m));
                return point;
            })
            .collect(Collectors.toList());
    }
}
