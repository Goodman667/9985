package com.example.phq9assessment.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {
    
    public List<Recommendation> generateRecommendations(int[] answers, int totalScore, 
                                                       String sentimentResult) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        if (answers[2] >= 2 || answers[3] >= 2) {
            recommendations.add(new Recommendation(
                "sleep",
                "睡眠改善计划",
                "建立规律的睡眠时间表，睡前1小时避免屏幕，尝试渐进性肌肉放松练习",
                "https://example.com/sleep-guide",
                "high"
            ));
        }
        
        if (answers[0] >= 2 || answers[1] >= 2) {
            recommendations.add(new Recommendation(
                "activity",
                "行为激活疗法",
                "每天安排至少一项曾经喜欢的活动，即使现在不想做，也要尝试开始行动",
                "https://example.com/behavioral-activation",
                "high"
            ));
        }
        
        if (answers[6] >= 2) {
            recommendations.add(new Recommendation(
                "mindfulness",
                "正念冥想练习",
                "每天进行10-15分钟的正念冥想，帮助改善注意力和减少焦虑",
                "https://example.com/mindfulness",
                "medium"
            ));
        }
        
        if (answers[5] >= 2) {
            recommendations.add(new Recommendation(
                "cbt",
                "认知行为疗法（CBT）资源",
                "学习识别和挑战负面思维模式，推荐使用CBT自助练习本",
                "https://example.com/cbt-resources",
                "high"
            ));
        }
        
        if (totalScore >= 10) {
            recommendations.add(new Recommendation(
                "professional",
                "寻求专业帮助",
                "您的症状需要专业评估，建议预约心理咨询师或精神科医生",
                "https://example.com/find-therapist",
                "critical"
            ));
        }
        
        if (answers[4] >= 2) {
            recommendations.add(new Recommendation(
                "nutrition",
                "营养与饮食建议",
                "保持规律饮食，增加富含Omega-3、维生素D和B族维生素的食物",
                "https://example.com/nutrition-guide",
                "medium"
            ));
        }
        
        if (answers[7] >= 1 || answers[8] >= 1) {
            recommendations.add(new Recommendation(
                "exercise",
                "运动疗法",
                "每周进行至少150分钟的中等强度运动，如快走、慢跑、游泳等",
                "https://example.com/exercise-plan",
                "high"
            ));
        }
        
        if ("negative".equals(sentimentResult)) {
            recommendations.add(new Recommendation(
                "journal",
                "情绪日记",
                "每天写下您的想法和感受，这有助于识别触发因素和情绪模式",
                "https://example.com/mood-journal",
                "medium"
            ));
        }
        
        if (answers[8] > 0) {
            recommendations.add(new Recommendation(
                "crisis",
                "⚠️ 危机干预资源",
                "如果您有自伤想法，请立即联系：全国24小时心理援助热线：400-161-9995",
                "tel:400-161-9995",
                "critical"
            ));
        }
        
        recommendations.add(new Recommendation(
            "support",
            "社会支持网络",
            "与信任的朋友、家人保持联系，考虑加入抑郁症支持小组",
            "https://example.com/support-groups",
            "medium"
        ));
        
        return recommendations;
    }
    
    public static class Recommendation {
        private String type;
        private String title;
        private String description;
        private String link;
        private String priority;
        
        public Recommendation(String type, String title, String description, 
                            String link, String priority) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.link = link;
            this.priority = priority;
        }
        
        public String getType() {
            return type;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getLink() {
            return link;
        }
        
        public String getPriority() {
            return priority;
        }
        
        public String getPriorityText() {
            switch (priority) {
                case "critical": return "紧急";
                case "high": return "高";
                case "medium": return "中";
                case "low": return "低";
                default: return "一般";
            }
        }
        
        public String getPriorityClass() {
            switch (priority) {
                case "critical": return "priority-critical";
                case "high": return "priority-high";
                case "medium": return "priority-medium";
                case "low": return "priority-low";
                default: return "priority-normal";
            }
        }
    }
}
