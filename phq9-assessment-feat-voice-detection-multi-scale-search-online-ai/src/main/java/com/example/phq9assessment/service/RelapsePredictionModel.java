package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelapsePredictionModel {
    
    @Autowired
    private AssessmentRecordRepository assessmentRecordRepository;
    
    @Autowired
    private AlertingService alertingService;
    
    public Map<String, Object> predictRelapseRisk(String userId, int forecastDays) {
        List<AssessmentRecord> records = assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        Map<String, Object> result = new HashMap<>();
        
        if (records.size() < 3) {
            result.put("status", "INSUFFICIENT_DATA");
            result.put("message", "需要至少3次评估记录才能进行预测");
            return result;
        }
        
        SimpleRegression regression = new SimpleRegression();
        LocalDateTime baseTime = records.get(0).getCreatedAt();
        
        for (AssessmentRecord record : records) {
            long daysSinceStart = ChronoUnit.DAYS.between(baseTime, record.getCreatedAt());
            regression.addData(daysSinceStart, record.getTotalScore());
        }
        
        double slope = regression.getSlope();
        double intercept = regression.getIntercept();
        double rSquared = regression.getRSquare();
        
        AssessmentRecord latest = records.get(records.size() - 1);
        long latestDay = ChronoUnit.DAYS.between(baseTime, latest.getCreatedAt());
        
        List<Map<String, Object>> predictions = new ArrayList<>();
        for (int i = 7; i <= forecastDays; i += 7) {
            double predictedScore = intercept + slope * (latestDay + i);
            predictedScore = Math.max(0, Math.min(27, predictedScore));
            
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("daysAhead", i);
            prediction.put("predictedScore", Math.round(predictedScore * 10) / 10.0);
            prediction.put("predictedLevel", scoreToLevel((int) Math.round(predictedScore)));
            prediction.put("date", latest.getCreatedAt().plusDays(i).toLocalDate().toString());
            
            predictions.add(prediction);
        }
        
        result.put("predictions", predictions);
        result.put("trend", slope > 0.1 ? "WORSENING" : (slope < -0.1 ? "IMPROVING" : "STABLE"));
        result.put("slope", Math.round(slope * 100) / 100.0);
        result.put("confidence", Math.round(rSquared * 100) / 100.0);
        
        Map<String, Object> riskAssessment = assessRelapseRisk(records, slope, predictions);
        result.put("riskAssessment", riskAssessment);
        
        Map<String, Object> riskFactors = identifyRiskFactors(records);
        result.put("riskFactors", riskFactors);
        
        List<String> preventionStrategies = generatePreventionStrategies(riskAssessment, riskFactors);
        result.put("preventionStrategies", preventionStrategies);
        
        String riskLevel = (String) riskAssessment.get("level");
        if ("HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel)) {
            alertingService.createAlert(
                userId,
                "RELAPSE_RISK",
                riskLevel,
                (Double) riskAssessment.get("score"),
                "PREDICTION_MODEL",
                "预测模型检测到较高的复发风险",
                String.join("\n", preventionStrategies),
                latest
            );
        }
        
        return result;
    }
    
    private String scoreToLevel(int score) {
        if (score >= 20) return "严重";
        if (score >= 15) return "中度偏重";
        if (score >= 10) return "中度";
        if (score >= 5) return "轻度";
        return "最小";
    }
    
    private Map<String, Object> assessRelapseRisk(List<AssessmentRecord> records, double slope, 
                                                   List<Map<String, Object>> predictions) {
        Map<String, Object> assessment = new HashMap<>();
        
        AssessmentRecord latest = records.get(records.size() - 1);
        int currentScore = latest.getTotalScore();
        
        double avgPredictedScore = predictions.stream()
            .mapToDouble(p -> (Double) p.get("predictedScore"))
            .average()
            .orElse(0);
        
        int volatility = calculateVolatility(records);
        
        double riskScore = 0;
        
        if (currentScore >= 15) riskScore += 40;
        else if (currentScore >= 10) riskScore += 25;
        else if (currentScore >= 5) riskScore += 10;
        
        if (slope > 0.5) riskScore += 30;
        else if (slope > 0.2) riskScore += 15;
        
        if (avgPredictedScore >= 15) riskScore += 20;
        else if (avgPredictedScore >= 10) riskScore += 10;
        
        if (volatility > 10) riskScore += 10;
        
        String level;
        String description;
        
        if (riskScore >= 70) {
            level = "CRITICAL";
            description = "极高复发风险，需要立即专业干预";
        } else if (riskScore >= 50) {
            level = "HIGH";
            description = "高复发风险，建议加强监测和支持";
        } else if (riskScore >= 30) {
            level = "MEDIUM";
            description = "中等复发风险，建议保持警惕";
        } else {
            level = "LOW";
            description = "低复发风险，继续保持良好状态";
        }
        
        assessment.put("level", level);
        assessment.put("score", riskScore);
        assessment.put("description", description);
        
        return assessment;
    }
    
    private int calculateVolatility(List<AssessmentRecord> records) {
        if (records.size() < 2) return 0;
        
        int maxDiff = 0;
        for (int i = 1; i < records.size(); i++) {
            int diff = Math.abs(records.get(i).getTotalScore() - records.get(i - 1).getTotalScore());
            maxDiff = Math.max(maxDiff, diff);
        }
        
        return maxDiff;
    }
    
    private Map<String, Object> identifyRiskFactors(List<AssessmentRecord> records) {
        Map<String, Object> factors = new HashMap<>();
        List<String> identifiedFactors = new ArrayList<>();
        
        if (records.size() < 3) {
            factors.put("factors", identifiedFactors);
            return factors;
        }
        
        List<AssessmentRecord> recent = records.stream()
            .skip(Math.max(0, records.size() - 5))
            .collect(Collectors.toList());
        
        double avgScore = recent.stream()
            .mapToInt(AssessmentRecord::getTotalScore)
            .average()
            .orElse(0);
        
        if (avgScore >= 10) {
            identifiedFactors.add("持续的中高抑郁症状");
        }
        
        boolean hasWorsening = false;
        for (int i = 1; i < recent.size(); i++) {
            if (recent.get(i).getTotalScore() > recent.get(i - 1).getTotalScore()) {
                hasWorsening = true;
                break;
            }
        }
        
        if (hasWorsening) {
            identifiedFactors.add("症状呈恶化趋势");
        }
        
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        if (month >= 10 || month <= 2) {
            identifiedFactors.add("季节性因素（秋冬季节）");
        }
        
        long sleepIssueCount = recent.stream()
            .filter(r -> "PSQI".equals(r.getQuestionnaireCode()) && r.getTotalScore() > 5)
            .count();
        
        if (sleepIssueCount >= 2) {
            identifiedFactors.add("睡眠问题");
        }
        
        factors.put("factors", identifiedFactors);
        factors.put("count", identifiedFactors.size());
        
        return factors;
    }
    
    private List<String> generatePreventionStrategies(Map<String, Object> riskAssessment, 
                                                      Map<String, Object> riskFactors) {
        List<String> strategies = new ArrayList<>();
        
        String riskLevel = (String) riskAssessment.get("level");
        
        if ("CRITICAL".equals(riskLevel) || "HIGH".equals(riskLevel)) {
            strategies.add("1. 立即联系心理健康专业人士，安排评估和治疗计划");
            strategies.add("2. 如果已有治疗师，及时告知当前状况的变化");
            strategies.add("3. 建立24小时支持系统，包括紧急联系人和危机热线");
        }
        
        strategies.add("4. 保持规律的作息时间，确保充足睡眠（7-9小时）");
        strategies.add("5. 每天进行至少30分钟的体育活动，如散步、慢跑或瑜伽");
        strategies.add("6. 实践正念冥想或深呼吸练习，每天10-15分钟");
        strategies.add("7. 保持社交连接，定期与朋友或家人交流");
        
        @SuppressWarnings("unchecked")
        List<String> identifiedFactors = (List<String>) riskFactors.get("factors");
        
        if (identifiedFactors.contains("睡眠问题")) {
            strategies.add("8. 针对睡眠问题：建立睡前放松程序，避免睡前使用电子设备");
        }
        
        if (identifiedFactors.contains("季节性因素（秋冬季节）")) {
            strategies.add("9. 针对季节性因素：增加日光暴露，考虑光疗");
        }
        
        strategies.add("10. 定期监测情绪状态，每周至少进行一次自我评估");
        strategies.add("11. 识别并避免已知的触发因素");
        strategies.add("12. 保持健康饮食，避免过量摄入咖啡因和酒精");
        
        return strategies;
    }
    
    public Map<String, Object> getHistoricalTrend(String userId) {
        List<AssessmentRecord> records = assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        Map<String, Object> result = new HashMap<>();
        
        if (records.isEmpty()) {
            result.put("status", "NO_DATA");
            return result;
        }
        
        List<Map<String, Object>> timeline = records.stream()
            .map(r -> {
                Map<String, Object> point = new HashMap<>();
                point.put("date", r.getCreatedAt().toLocalDate().toString());
                point.put("score", r.getTotalScore());
                point.put("level", r.getLevel());
                return point;
            })
            .collect(Collectors.toList());
        
        result.put("timeline", timeline);
        result.put("recordCount", records.size());
        
        return result;
    }
}
