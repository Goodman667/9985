package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.entity.EmotionAlert;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmotionWaveDetectionService {
    
    @Autowired
    private AssessmentRecordRepository assessmentRecordRepository;
    
    @Autowired
    private AlertingService alertingService;
    
    public Map<String, Object> analyzeEmotionWave(String userId) {
        List<AssessmentRecord> records = assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        Map<String, Object> result = new HashMap<>();
        
        if (records.isEmpty()) {
            result.put("status", "NO_DATA");
            return result;
        }
        
        List<Map<String, Object>> dataPoints = new ArrayList<>();
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (AssessmentRecord record : records) {
            Map<String, Object> point = new HashMap<>();
            point.put("timestamp", record.getCreatedAt().toString());
            point.put("score", record.getTotalScore());
            point.put("level", record.getLevel());
            point.put("sentimentScore", record.getSentimentScore());
            point.put("voiceEmotionScore", record.getVoiceEmotionScore());
            
            dataPoints.add(point);
            stats.addValue(record.getTotalScore());
        }
        
        result.put("dataPoints", dataPoints);
        result.put("mean", stats.getMean());
        result.put("stdDev", stats.getStandardDeviation());
        result.put("min", stats.getMin());
        result.put("max", stats.getMax());
        
        detectEmotionSpikes(userId, records, stats);
        
        List<String> patterns = detectPatterns(records);
        result.put("patterns", patterns);
        
        Map<String, Object> riskLevel = assessCurrentRisk(records);
        result.put("currentRisk", riskLevel);
        
        return result;
    }
    
    private void detectEmotionSpikes(String userId, List<AssessmentRecord> records, DescriptiveStatistics stats) {
        if (records.size() < 2) return;
        
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        double threshold = mean + (2 * stdDev);
        
        AssessmentRecord latest = records.get(records.size() - 1);
        
        if (latest.getTotalScore() > threshold) {
            String severity = determineSeverity(latest.getTotalScore(), mean, stdDev);
            String message = String.format("检测到情绪高峰：当前评分 %d 超过正常波动范围（平均值：%.1f）", 
                latest.getTotalScore(), mean);
            String recommendation = generateRecommendation(severity);
            
            alertingService.createAlert(
                userId, 
                "EMOTION_SPIKE", 
                severity, 
                (double) latest.getTotalScore(),
                "WAVE_DETECTION",
                message,
                recommendation,
                latest
            );
        }
        
        if (records.size() >= 3) {
            AssessmentRecord prev1 = records.get(records.size() - 2);
            AssessmentRecord prev2 = records.get(records.size() - 3);
            
            if (latest.getTotalScore() > prev1.getTotalScore() && 
                prev1.getTotalScore() > prev2.getTotalScore() &&
                latest.getTotalScore() - prev2.getTotalScore() >= 5) {
                
                String message = "检测到持续恶化趋势：连续三次评估分数上升";
                String recommendation = "建议立即联系心理健康专业人士，寻求专业支持。";
                
                alertingService.createAlert(
                    userId,
                    "WORSENING_TREND",
                    "HIGH",
                    (double) latest.getTotalScore(),
                    "TREND_ANALYSIS",
                    message,
                    recommendation,
                    latest
                );
            }
        }
    }
    
    private String determineSeverity(int score, double mean, double stdDev) {
        double zScore = (score - mean) / stdDev;
        
        if (zScore > 3) return "CRITICAL";
        if (zScore > 2.5) return "HIGH";
        if (zScore > 2) return "MEDIUM";
        return "LOW";
    }
    
    private String generateRecommendation(String severity) {
        switch (severity) {
            case "CRITICAL":
                return "强烈建议立即寻求专业心理健康支持。如有自伤或自杀想法，请拨打心理危机热线：400-161-9995。";
            case "HIGH":
                return "您的情绪出现明显波动，建议尽快联系心理咨询师或医生进行评估。同时可以尝试深呼吸练习、冥想或轻度运动来缓解。";
            case "MEDIUM":
                return "建议进行放松练习，如正念冥想、渐进式肌肉放松或散步。保持规律作息和充足睡眠。";
            default:
                return "继续保持自我关注，定期记录情绪状态。如感到不适，随时可以进行评估。";
        }
    }
    
    private List<String> detectPatterns(List<AssessmentRecord> records) {
        List<String> patterns = new ArrayList<>();
        
        if (records.size() < 7) {
            return patterns;
        }
        
        List<Integer> recentScores = records.stream()
            .skip(Math.max(0, records.size() - 7))
            .map(AssessmentRecord::getTotalScore)
            .collect(Collectors.toList());
        
        boolean increasing = true;
        boolean decreasing = true;
        
        for (int i = 1; i < recentScores.size(); i++) {
            if (recentScores.get(i) <= recentScores.get(i - 1)) {
                increasing = false;
            }
            if (recentScores.get(i) >= recentScores.get(i - 1)) {
                decreasing = false;
            }
        }
        
        if (increasing) {
            patterns.add("持续恶化趋势");
        }
        if (decreasing) {
            patterns.add("持续改善趋势");
        }
        
        double avg = recentScores.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = recentScores.stream()
            .mapToDouble(s -> Math.pow(s - avg, 2))
            .average().orElse(0);
        
        if (variance > 20) {
            patterns.add("情绪波动较大");
        } else if (variance < 5) {
            patterns.add("情绪相对稳定");
        }
        
        return patterns;
    }
    
    private Map<String, Object> assessCurrentRisk(List<AssessmentRecord> records) {
        Map<String, Object> risk = new HashMap<>();
        
        if (records.isEmpty()) {
            risk.put("level", "UNKNOWN");
            return risk;
        }
        
        AssessmentRecord latest = records.get(records.size() - 1);
        int score = latest.getTotalScore();
        
        String level;
        String description;
        
        if (score >= 20) {
            level = "SEVERE";
            description = "严重抑郁症状，需要立即专业干预";
        } else if (score >= 15) {
            level = "MODERATE_SEVERE";
            description = "中度偏重抑郁症状，建议寻求专业帮助";
        } else if (score >= 10) {
            level = "MODERATE";
            description = "中度抑郁症状，建议关注并考虑咨询";
        } else if (score >= 5) {
            level = "MILD";
            description = "轻度抑郁症状，建议持续监测";
        } else {
            level = "MINIMAL";
            description = "症状最小或无症状";
        }
        
        risk.put("level", level);
        risk.put("score", score);
        risk.put("description", description);
        risk.put("timestamp", latest.getCreatedAt().toString());
        
        return risk;
    }
    
    public List<Map<String, Object>> getEmotionTimeline(String userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<AssessmentRecord> records = assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        return records.stream()
            .filter(r -> r.getCreatedAt().isAfter(startDate))
            .map(r -> {
                Map<String, Object> point = new HashMap<>();
                point.put("timestamp", r.getCreatedAt().toString());
                point.put("score", r.getTotalScore());
                point.put("level", r.getLevel());
                return point;
            })
            .collect(Collectors.toList());
    }
}
