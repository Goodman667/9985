package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.entity.EmotionAlert;
import com.example.phq9assessment.repository.EmotionAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertingService {
    
    @Autowired
    private EmotionAlertRepository emotionAlertRepository;
    
    public EmotionAlert createAlert(String userId, String alertType, String severity, 
                                   Double emotionScore, String triggerSource, 
                                   String message, String recommendation,
                                   AssessmentRecord assessmentRecord) {
        EmotionAlert alert = new EmotionAlert();
        alert.setUserId(userId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setEmotionScore(emotionScore);
        alert.setTriggerSource(triggerSource);
        alert.setAlertMessage(message);
        alert.setRecommendation(recommendation);
        alert.setAssessmentRecord(assessmentRecord);
        
        return emotionAlertRepository.save(alert);
    }
    
    public List<EmotionAlert> getUnreadAlerts(String userId) {
        return emotionAlertRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }
    
    public List<EmotionAlert> getAllAlerts(String userId) {
        return emotionAlertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public EmotionAlert markAsRead(Long alertId) {
        EmotionAlert alert = emotionAlertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            alert.setIsRead(true);
            alert.setAcknowledgedAt(LocalDateTime.now());
            return emotionAlertRepository.save(alert);
        }
        return null;
    }
    
    public List<EmotionAlert> getAlertsByDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return emotionAlertRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end);
    }
    
    public long getUnreadCount(String userId) {
        return emotionAlertRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false).size();
    }
}
