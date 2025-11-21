package com.example.phq9assessment.controller;

import com.example.phq9assessment.model.AssessmentResult;
import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.entity.Questionnaire;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import com.example.phq9assessment.service.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class AssessmentController {

    private final Gson gson = new Gson();
    
    @Autowired
    private AssessmentRecordRepository assessmentRecordRepository;
    
    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;
    
    @Autowired
    private MachineLearningService machineLearningService;
    
    @Autowired
    private AnomalyDetectionService anomalyDetectionService;
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private QuestionnaireService questionnaireService;
    
    @Autowired
    private VoiceDetectionService voiceDetectionService;
    
    @Autowired
    private OnlineAIService onlineAIService;

    @GetMapping("/")
    public String showForm(Model model) {
        questionnaireService.initializeDefaultQuestionnaires();
        List<Questionnaire> questionnaires = questionnaireService.getAllActiveQuestionnaires();
        model.addAttribute("questionnaires", questionnaires);
        model.addAttribute("selectedQuestionnaire", "PHQ-9");
        return "index";
    }
    
    @GetMapping("/search-questionnaires")
    @ResponseBody
    public List<Questionnaire> searchQuestionnaires(@RequestParam(required = false) String keyword) {
        return questionnaireService.searchQuestionnaires(keyword);
    }

    @PostMapping("/submit")
    public String handleSubmit(
            @RequestParam("q1") int q1, @RequestParam("q2") int q2,
            @RequestParam("q3") int q3, @RequestParam("q4") int q4,
            @RequestParam("q5") int q5, @RequestParam("q6") int q6,
            @RequestParam("q7") int q7, @RequestParam("q8") int q8,
            @RequestParam("q9") int q9,
            @RequestParam(value = "sentimentText", required = false, defaultValue = "") String sentimentText,
            @RequestParam(value = "voiceAudio", required = false) String voiceAudio,
            @RequestParam(value = "questionnaireCode", required = false, defaultValue = "PHQ-9") String questionnaireCode,
            HttpSession session,
            Model model) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            session.setAttribute("userId", userId);
        }

        int totalScore = q1 + q2 + q3 + q4 + q5 + q6 + q7 + q8 + q9;
        int[] answers = {q1, q2, q3, q4, q5, q6, q7, q8, q9};
        
        VoiceDetectionService.VoiceAnalysisResult voiceResult = null;
        if (voiceAudio != null && !voiceAudio.trim().isEmpty()) {
            voiceResult = voiceDetectionService.analyzeVoiceFeatures(voiceAudio);
        }

        String level = "minimal";
        if (totalScore >= 5 && totalScore <= 9) {
            level = "mild";
        } else if (totalScore >= 10 && totalScore <= 14) {
            level = "moderate";
        } else if (totalScore >= 15 && totalScore <= 19) {
            level = "moderately-severe";
        } else if (totalScore >= 20) {
            level = "severe";
        }

        String levelText;
        switch(level) {
            case "minimal": levelText = "无抑郁症状"; break;
            case "mild": levelText = "轻度抑郁"; break;
            case "moderate": levelText = "中度抑郁"; break;
            case "moderately-severe": levelText = "中重度抑郁"; break;
            case "severe": levelText = "重度抑郁"; break;
            default: levelText = "未知"; break;
        }

        SentimentAnalysisService.SentimentAnalysisResult sentimentResult = 
            sentimentAnalysisService.analyzeSentiment(sentimentText);
        
        OnlineAIService.AIEnhancementResult aiEnhancement = 
            onlineAIService.enhanceSentimentAnalysis(sentimentText);

        double mlRiskScore = machineLearningService.calculateRiskScore(
            answers, sentimentText, sentimentResult.getScore()
        );
        
        if (voiceResult != null) {
            mlRiskScore = (mlRiskScore * 0.7) + (Math.abs(voiceResult.getEmotionScore()) * 0.3);
        }

        String mlRiskLevel;
        if (mlRiskScore < 0.2) {
            mlRiskLevel = "低风险";
        } else if (mlRiskScore < 0.4) {
            mlRiskLevel = "较低风险";
        } else if (mlRiskScore < 0.6) {
            mlRiskLevel = "中等风险";
        } else if (mlRiskScore < 0.8) {
            mlRiskLevel = "较高风险";
        } else {
            mlRiskLevel = "高风险";
        }

        AnomalyDetectionService.AnomalyDetectionResult anomalyResult = 
            anomalyDetectionService.detectAnomalies(answers);

        List<RecommendationService.Recommendation> recommendations = 
            recommendationService.generateRecommendations(
                answers, totalScore, sentimentResult.getSentiment()
            );

        MachineLearningService.ClusterResult clusterResult = 
            machineLearningService.clusterUser(answers, mlRiskScore);

        List<AssessmentRecord> historicalRecords = 
            assessmentRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        MachineLearningService.TrendAnalysis trendAnalysis = 
            machineLearningService.analyzeTrend(historicalRecords);

        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setQ1(q1);
        record.setQ2(q2);
        record.setQ3(q3);
        record.setQ4(q4);
        record.setQ5(q5);
        record.setQ6(q6);
        record.setQ7(q7);
        record.setQ8(q8);
        record.setQ9(q9);
        record.setTotalScore(totalScore);
        record.setLevel(level);
        record.setSentimentText(sentimentText);
        record.setSentimentScore(sentimentResult.getScore());
        record.setMlRiskScore(mlRiskScore);
        record.setAnomalyDetected(anomalyResult.isAnomalous());
        record.setQuestionnaireCode(questionnaireCode);
        
        if (voiceResult != null) {
            record.setVoiceEmotionScore(voiceResult.getEmotionScore());
            record.setVoiceFeatures(gson.toJson(voiceResult.getFeatures()));
        }
        
        assessmentRecordRepository.save(record);

        StringBuilder suggestionBuilder = new StringBuilder();
        suggestionBuilder.append("根据您的具体回答，我们为您生成了以下个性化建议：\n\n");

        if (q1 >= 2 || q2 >= 2) {
            suggestionBuilder.append("• 【情绪与动力】您在情绪或做事的动力上遇到较多挑战。尝试进行一些能带来即时成就感的微小活动，如整理书桌、完成一个小任务。规律的日照和轻运动也很有帮助。\n");
        }
        if (q3 >= 2 || q4 >= 2) {
            suggestionBuilder.append("• 【精力与睡眠】您似乎感到精力不济或有睡眠困扰。建议您建立固定的睡前放松程序，如洗个热水澡、听一些舒缓的音乐，并避免睡前使用电子产品。\n");
        }
        if (q5 >= 2) {
            suggestionBuilder.append("• 【食欲变化】食欲的改变是情绪状态的直接反映。尽量保持规律的饮食，即使没有胃口，也吃一些清淡、易消化的食物。可以尝试少量多餐。\n");
        }
        if (q6 >= 2) {
            suggestionBuilder.append("• 【自我评价】请记住，您当前的感受是疾病的一部分，而不是您本身的事实。尝试对自己宽容一些，避免自我批评。与信任的朋友或家人聊一聊可能会让您感觉好一些。\n");
        }
        if (q7 >= 2) {
            suggestionBuilder.append("• 【专注力】专注力下降是常见症状。在做重要事情时，可以尝试\"番茄工作法\"（工作25分钟，休息5分钟），将大任务分解成小步骤来完成。\n");
        }

        if (totalScore <= 4) {
            suggestionBuilder.append("\n🎉 您的整体状态很健康！继续保持积极的生活方式和乐观的心态。");
        } else if (totalScore <= 14) {
            suggestionBuilder.append("\n💡 您的状况值得关注。除了上述建议，建议您主动学习更多关于情绪管理的知识，并考虑与专业心理咨询师进行一次交流，获得更深入的指导。");
        } else {
            suggestionBuilder.append("\n🚨 您的状况需要专业支持。我们强烈建议您尽快寻求心理医生或精神科医生的帮助，进行专业的诊断和治疗。这不是个人意志能轻易解决的，寻求医疗帮助是明智且必要的。");
        }

        boolean highRisk = (q9 > 0);

        AssessmentResult result = new AssessmentResult();
        result.setTotalScore(totalScore);
        result.setLevel(level);
        result.setLevelText(levelText);
        result.setSuggestion(suggestionBuilder.toString());
        result.setHighRisk(highRisk);
        result.setMlRiskScore(mlRiskScore);
        result.setMlRiskLevel(mlRiskLevel);
        result.setSentimentAnalysis(sentimentResult);
        result.setRecommendations(recommendations);
        result.setTrendAnalysis(trendAnalysis);
        result.setClusterResult(clusterResult);
        result.setAnomalyDetection(anomalyResult);
        result.setHasHistoricalData(historicalRecords.size() > 0);
        result.setHistoricalScores(
            historicalRecords.stream()
                .map(AssessmentRecord::getTotalScore)
                .collect(Collectors.toList())
        );

        result.setVoiceAnalysis(voiceResult);
        result.setAiEnhancement(aiEnhancement);
        result.setQuestionnaireCode(questionnaireCode);

        model.addAttribute("result", result);

        return "index";
    }
}
