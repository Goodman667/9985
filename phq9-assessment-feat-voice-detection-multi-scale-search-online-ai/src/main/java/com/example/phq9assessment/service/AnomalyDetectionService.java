package com.example.phq9assessment.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnomalyDetectionService {
    
    public AnomalyDetectionResult detectAnomalies(int[] answers) {
        List<String> anomalies = new ArrayList<>();
        boolean isAnomalous = false;
        
        if (areAllSame(answers)) {
            anomalies.add("检测到所有问题的答案相同，这可能表示未认真作答");
            isAnomalous = true;
        }
        
        if (hasStrictPattern(answers)) {
            anomalies.add("检测到答案存在规律性模式，建议重新评估");
            isAnomalous = true;
        }
        
        if (hasContradictoryAnswers(answers)) {
            anomalies.add("检测到答案之间存在矛盾，建议仔细核对");
            isAnomalous = true;
        }
        
        if (hasExtremeResponse(answers)) {
            anomalies.add("检测到极端响应模式，建议进行专业评估");
            isAnomalous = true;
        }
        
        return new AnomalyDetectionResult(isAnomalous, anomalies);
    }
    
    private boolean areAllSame(int[] answers) {
        if (answers.length == 0) return false;
        int first = answers[0];
        for (int answer : answers) {
            if (answer != first) {
                return false;
            }
        }
        return true;
    }
    
    private boolean hasStrictPattern(int[] answers) {
        if (answers.length < 3) return false;
        
        boolean ascending = true;
        boolean descending = true;
        
        for (int i = 1; i < answers.length; i++) {
            if (answers[i] != answers[i-1] + 1) {
                ascending = false;
            }
            if (answers[i] != answers[i-1] - 1) {
                descending = false;
            }
        }
        
        return ascending || descending;
    }
    
    private boolean hasContradictoryAnswers(int[] answers) {
        if (answers[0] == 0 && answers[1] == 0 && answers[2] == 0) {
            int highScoreCount = 0;
            for (int i = 3; i < answers.length; i++) {
                if (answers[i] >= 2) {
                    highScoreCount++;
                }
            }
            if (highScoreCount >= 4) {
                return true;
            }
        }
        
        if (answers[8] >= 2) {
            int lowScoreCount = 0;
            for (int i = 0; i < 8; i++) {
                if (answers[i] == 0) {
                    lowScoreCount++;
                }
            }
            if (lowScoreCount >= 6) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasExtremeResponse(int[] answers) {
        int maxCount = 0;
        for (int answer : answers) {
            if (answer == 3) {
                maxCount++;
            }
        }
        return maxCount >= 7;
    }
    
    public static class AnomalyDetectionResult {
        private boolean isAnomalous;
        private List<String> anomalies;
        
        public AnomalyDetectionResult(boolean isAnomalous, List<String> anomalies) {
            this.isAnomalous = isAnomalous;
            this.anomalies = anomalies;
        }
        
        public boolean isAnomalous() {
            return isAnomalous;
        }
        
        public List<String> getAnomalies() {
            return anomalies;
        }
    }
}
