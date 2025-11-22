package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SleepMoodCorrelationAnalyzer {
    
    @Autowired
    private AssessmentRecordRepository assessmentRecordRepository;
    
    public Map<String, Object> analyzeSleepMoodCorrelation(String userId) {
        List<AssessmentRecord> allRecords = assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        Map<String, Object> result = new HashMap<>();
        
        List<AssessmentRecord> sleepRecords = allRecords.stream()
            .filter(r -> "PSQI".equals(r.getQuestionnaireCode()))
            .collect(Collectors.toList());
        
        List<AssessmentRecord> moodRecords = allRecords.stream()
            .filter(r -> "PHQ-9".equals(r.getQuestionnaireCode()) || "GAD-7".equals(r.getQuestionnaireCode()))
            .collect(Collectors.toList());
        
        if (sleepRecords.isEmpty() || moodRecords.isEmpty()) {
            result.put("status", "INSUFFICIENT_DATA");
            result.put("message", "需要PSQI和PHQ-9/GAD-7评估数据");
            return result;
        }
        
        List<Map<String, Object>> pairedData = pairSleepMoodData(sleepRecords, moodRecords);
        
        if (pairedData.size() < 3) {
            result.put("status", "INSUFFICIENT_PAIRS");
            result.put("message", "需要至少3对匹配的评估数据");
            return result;
        }
        
        double[] sleepScores = pairedData.stream()
            .mapToDouble(p -> (Double) p.get("sleepScore"))
            .toArray();
        
        double[] moodScores = pairedData.stream()
            .mapToDouble(p -> (Double) p.get("moodScore"))
            .toArray();
        
        PearsonsCorrelation correlation = new PearsonsCorrelation();
        double correlationCoefficient = correlation.correlation(sleepScores, moodScores);
        
        result.put("correlationCoefficient", Math.round(correlationCoefficient * 1000) / 1000.0);
        result.put("correlationStrength", interpretCorrelation(correlationCoefficient));
        result.put("pairedDataCount", pairedData.size());
        result.put("pairedData", pairedData);
        
        Map<String, Object> sleepQualityBreakdown = analyzeSleepQualityDimensions(sleepRecords);
        result.put("sleepQualityBreakdown", sleepQualityBreakdown);
        
        Map<String, Object> optimalSchedule = generateOptimalSleepSchedule(pairedData);
        result.put("optimalSleepSchedule", optimalSchedule);
        
        List<String> recommendations = generateSleepRecommendations(pairedData, correlationCoefficient);
        result.put("recommendations", recommendations);
        
        Map<String, Object> impactEstimation = estimateSleepImprovementImpact(pairedData, correlationCoefficient);
        result.put("improvementImpact", impactEstimation);
        
        return result;
    }
    
    private List<Map<String, Object>> pairSleepMoodData(List<AssessmentRecord> sleepRecords, 
                                                         List<AssessmentRecord> moodRecords) {
        List<Map<String, Object>> paired = new ArrayList<>();
        
        for (AssessmentRecord sleepRecord : sleepRecords) {
            AssessmentRecord closestMoodRecord = findClosestRecord(sleepRecord, moodRecords);
            
            if (closestMoodRecord != null) {
                Map<String, Object> pair = new HashMap<>();
                pair.put("date", sleepRecord.getCreatedAt().toLocalDate().toString());
                pair.put("sleepScore", (double) sleepRecord.getTotalScore());
                pair.put("moodScore", (double) closestMoodRecord.getTotalScore());
                pair.put("sleepLevel", sleepRecord.getLevel());
                pair.put("moodLevel", closestMoodRecord.getLevel());
                pair.put("moodType", closestMoodRecord.getQuestionnaireCode());
                
                paired.add(pair);
            }
        }
        
        return paired;
    }
    
    private AssessmentRecord findClosestRecord(AssessmentRecord target, List<AssessmentRecord> candidates) {
        if (candidates.isEmpty()) return null;
        
        AssessmentRecord closest = candidates.get(0);
        long minDiff = Math.abs(
            java.time.Duration.between(target.getCreatedAt(), closest.getCreatedAt()).toHours()
        );
        
        for (AssessmentRecord candidate : candidates) {
            long diff = Math.abs(
                java.time.Duration.between(target.getCreatedAt(), candidate.getCreatedAt()).toHours()
            );
            
            if (diff < minDiff) {
                minDiff = diff;
                closest = candidate;
            }
        }
        
        return minDiff <= 72 ? closest : null;
    }
    
    private String interpretCorrelation(double coefficient) {
        double abs = Math.abs(coefficient);
        
        if (abs >= 0.7) return "强相关";
        if (abs >= 0.5) return "中等相关";
        if (abs >= 0.3) return "弱相关";
        return "几乎无相关";
    }
    
    private Map<String, Object> analyzeSleepQualityDimensions(List<AssessmentRecord> sleepRecords) {
        Map<String, Object> breakdown = new HashMap<>();
        
        if (sleepRecords.isEmpty()) return breakdown;
        
        AssessmentRecord latest = sleepRecords.get(sleepRecords.size() - 1);
        
        Map<String, Object> dimensions = new HashMap<>();
        dimensions.put("入睡困难", latest.getQ1());
        dimensions.put("夜间觉醒", latest.getQ2());
        dimensions.put("早醒", latest.getQ3());
        dimensions.put("睡眠效率", latest.getQ4());
        dimensions.put("睡眠质量", latest.getQ5());
        dimensions.put("日间功能", latest.getQ6());
        dimensions.put("睡眠时长", latest.getQ7());
        
        breakdown.put("dimensions", dimensions);
        breakdown.put("totalScore", latest.getTotalScore());
        breakdown.put("overallQuality", latest.getLevel());
        
        List<String> problemAreas = new ArrayList<>();
        if (latest.getQ1() >= 2) problemAreas.add("入睡困难");
        if (latest.getQ2() >= 2) problemAreas.add("夜间觉醒");
        if (latest.getQ3() >= 2) problemAreas.add("早醒");
        if (latest.getQ4() >= 2) problemAreas.add("睡眠效率低");
        if (latest.getQ5() >= 2) problemAreas.add("睡眠质量差");
        if (latest.getQ6() >= 2) problemAreas.add("日间功能受损");
        if (latest.getQ7() >= 2) problemAreas.add("睡眠时长不足");
        
        breakdown.put("problemAreas", problemAreas);
        
        return breakdown;
    }
    
    private Map<String, Object> generateOptimalSleepSchedule(List<Map<String, Object>> pairedData) {
        Map<String, Object> schedule = new HashMap<>();
        
        List<Map<String, Object>> goodSleep = pairedData.stream()
            .filter(p -> (Double) p.get("sleepScore") < 5)
            .collect(Collectors.toList());
        
        schedule.put("recommendedBedtime", "22:00 - 23:00");
        schedule.put("recommendedWakeTime", "06:00 - 07:00");
        schedule.put("recommendedSleepDuration", "7-9小时");
        
        List<String> scheduleAdvice = new ArrayList<>();
        scheduleAdvice.add("保持固定的睡眠时间，包括周末");
        scheduleAdvice.add("睡前1-2小时避免使用电子设备");
        scheduleAdvice.add("创造舒适的睡眠环境：暗、静、凉");
        scheduleAdvice.add("避免睡前3小时摄入咖啡因");
        scheduleAdvice.add("建立放松的睡前程序，如阅读或冥想");
        
        schedule.put("advice", scheduleAdvice);
        
        return schedule;
    }
    
    private List<String> generateSleepRecommendations(List<Map<String, Object>> pairedData, 
                                                      double correlation) {
        List<String> recommendations = new ArrayList<>();
        
        if (Math.abs(correlation) >= 0.3) {
            recommendations.add("1. 睡眠质量与情绪状态显著相关，改善睡眠可能有助于改善情绪");
        }
        
        recommendations.add("2. 认知行为疗法-失眠（CBT-I）是改善睡眠的有效方法");
        recommendations.add("3. 建立规律的睡眠-觉醒节律，帮助调节生物钟");
        recommendations.add("4. 白天增加光照暴露，特别是早晨的自然光");
        recommendations.add("5. 适度的体育锻炼，但避免睡前3小时内剧烈运动");
        recommendations.add("6. 减少白天小睡，或限制在20-30分钟内");
        recommendations.add("7. 管理睡前的担忧和焦虑，可以使用'担忧时间'技术");
        recommendations.add("8. 如果睡眠问题持续，考虑咨询睡眠专家");
        
        double avgSleepScore = pairedData.stream()
            .mapToDouble(p -> (Double) p.get("sleepScore"))
            .average()
            .orElse(0);
        
        if (avgSleepScore >= 10) {
            recommendations.add("9. 您的睡眠质量较差，强烈建议寻求专业的睡眠评估和治疗");
        } else if (avgSleepScore >= 5) {
            recommendations.add("9. 您的睡眠质量有改善空间，建议开始实施睡眠卫生措施");
        }
        
        return recommendations;
    }
    
    private Map<String, Object> estimateSleepImprovementImpact(List<Map<String, Object>> pairedData,
                                                               double correlation) {
        Map<String, Object> estimation = new HashMap<>();
        
        if (pairedData.isEmpty()) return estimation;
        
        double avgSleepScore = pairedData.stream()
            .mapToDouble(p -> (Double) p.get("sleepScore"))
            .average()
            .orElse(0);
        
        double avgMoodScore = pairedData.stream()
            .mapToDouble(p -> (Double) p.get("moodScore"))
            .average()
            .orElse(0);
        
        double sleepImprovement = Math.min(avgSleepScore * 0.5, 5);
        
        double estimatedMoodImprovement = Math.abs(correlation) * sleepImprovement * 0.8;
        
        estimation.put("currentAvgSleepScore", Math.round(avgSleepScore * 10) / 10.0);
        estimation.put("currentAvgMoodScore", Math.round(avgMoodScore * 10) / 10.0);
        estimation.put("estimatedSleepImprovement", Math.round(sleepImprovement * 10) / 10.0);
        estimation.put("estimatedMoodImprovement", Math.round(estimatedMoodImprovement * 10) / 10.0);
        
        String impact;
        if (estimatedMoodImprovement >= 3) {
            impact = "改善睡眠可能对情绪有显著积极影响";
        } else if (estimatedMoodImprovement >= 1.5) {
            impact = "改善睡眠可能对情绪有中等积极影响";
        } else {
            impact = "改善睡眠可能对情绪有轻微积极影响";
        }
        
        estimation.put("impactDescription", impact);
        
        return estimation;
    }
}
