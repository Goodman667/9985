package com.example.phq9assessment.controller;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.entity.Question;
import com.example.phq9assessment.entity.Questionnaire;
import com.example.phq9assessment.model.AssessmentResult;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import com.example.phq9assessment.service.AnomalyDetectionService;
import com.example.phq9assessment.service.MachineLearningService;
import com.example.phq9assessment.service.OnlineAIService;
import com.example.phq9assessment.service.QuestionnaireService;
import com.example.phq9assessment.service.RecommendationService;
import com.example.phq9assessment.service.SentimentAnalysisService;
import com.example.phq9assessment.service.VoiceDetectionService;
import com.example.phq9assessment.service.OpenSmileService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class AssessmentController {

    private final Gson gson = new Gson();

    @Value("${ai.camera.enabled:true}")
    private boolean cameraEnabled;

    @Value("${ai.camera.update.interval:5000}")
    private int cameraUpdateInterval;

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

    @Autowired(required = false)
    private OpenSmileService openSmileService;

    @Autowired
    private OnlineAIService onlineAIService;

    @GetMapping("/")
    public String showForm(Model model) {
        questionnaireService.initializeDefaultQuestionnaires();
        model.addAttribute("questionnaires", questionnaireService.getAllActiveQuestionnaires());
        model.addAttribute("selectedQuestionnaire", "PHQ-9");
        model.addAttribute("cameraEnabled", cameraEnabled);
        model.addAttribute("cameraUpdateInterval", cameraUpdateInterval);
        return "index";
    }

    @GetMapping("/search-questionnaires")
    @ResponseBody
    public List<Questionnaire> searchQuestionnaires(@RequestParam(required = false) String keyword) {
        return questionnaireService.searchQuestionnaires(keyword);
    }

    @GetMapping("/questions")
    @ResponseBody
    public List<Question> getQuestions(@RequestParam String questionnaireCode) {
        return questionnaireService.getQuestionsForQuestionnaire(questionnaireCode);
    }

    @PostMapping("/submit")
    public String handleSubmit(
            @RequestParam(value = "sentimentText", required = false, defaultValue = "") String sentimentText,
            @RequestParam(value = "voiceAudio", required = false) String voiceAudio,
            @RequestParam(value = "cameraData", required = false) String cameraData,
            @RequestParam(value = "questionnaireCode", required = false, defaultValue = "PHQ-9") String questionnaireCode,
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        questionnaireService.initializeDefaultQuestionnaires();
        List<Question> questionnaireQuestions = questionnaireService.getQuestionsForQuestionnaire(questionnaireCode);
        if (questionnaireQuestions.isEmpty()) {
            questionnaireQuestions = questionnaireService.getQuestionsForQuestionnaire("PHQ-9");
            questionnaireCode = "PHQ-9";
        }

        int[] answers = extractAnswers(request, questionnaireQuestions);
        if (answers.length == 0) {
            model.addAttribute("questionnaires", questionnaireService.getAllActiveQuestionnaires());
            model.addAttribute("selectedQuestionnaire", questionnaireCode);
            model.addAttribute("cameraEnabled", cameraEnabled);
            model.addAttribute("cameraUpdateInterval", cameraUpdateInterval);
            model.addAttribute("errorMessage", "请完成问卷中的所有问题后再提交。");
            return "index";
        }

        int totalScore = Arrays.stream(answers).sum();
        int maxScore = questionnaireQuestions.stream()
                .mapToInt(q -> q.getMaxPoints() != null ? q.getMaxPoints() : 3)
                .sum();
        if (maxScore == 0) {
            maxScore = answers.length * 3;
        }

        int[] paddedAnswers = padAnswersForModel(answers, 9);

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            session.setAttribute("userId", userId);
        }

        VoiceDetectionService.VoiceAnalysisResult voiceResult = null;
        if (voiceAudio != null && !voiceAudio.trim().isEmpty()) {
            voiceResult = voiceDetectionService.analyzeVoiceFeatures(voiceAudio);
        }

        SentimentAnalysisService.SentimentAnalysisResult sentimentResult =
                sentimentAnalysisService.analyzeSentiment(sentimentText);

        OnlineAIService.AIEnhancementResult aiEnhancement =
                onlineAIService.enhanceSentimentAnalysis(sentimentText);

        double mlRiskScore = machineLearningService.calculateRiskScore(
                paddedAnswers, sentimentText, sentimentResult.getScore()
        );

        if (voiceResult != null) {
            mlRiskScore = (mlRiskScore * 0.7) + (Math.abs(voiceResult.getEmotionScore()) * 0.3);
        }

        AssessmentResult.CameraAnalysis cameraAnalysis = null;
        if (cameraData != null && !cameraData.trim().isEmpty()) {
            try {
                JsonObject cameraJson = gson.fromJson(cameraData, JsonObject.class);
                int activityLevel = cameraJson.has("activityLevel") ? cameraJson.get("activityLevel").getAsInt() : 0;
                int postureScore = cameraJson.has("postureScore") ? cameraJson.get("postureScore").getAsInt() : 100;
                int movementCount = cameraJson.has("movementCount") ? cameraJson.get("movementCount").getAsInt() : 0;

                String insight = generateCameraInsight(activityLevel, postureScore, movementCount);
                cameraAnalysis = new AssessmentResult.CameraAnalysis(activityLevel, postureScore, movementCount, insight);

                if (activityLevel < 20 && postureScore < 70) {
                    mlRiskScore = Math.min(1.0, mlRiskScore * 1.15);
                }
            } catch (Exception e) {
                System.err.println("解析摄像头数据失败: " + e.getMessage());
            }
        }

        String level = determineLevel(questionnaireCode, totalScore, maxScore);
        String levelText = mapLevelText(level);

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
                anomalyDetectionService.detectAnomalies(paddedAnswers);

        List<RecommendationService.Recommendation> recommendations =
                recommendationService.generateRecommendations(
                        paddedAnswers, totalScore, sentimentResult.getSentiment()
                );

        MachineLearningService.ClusterResult clusterResult =
                machineLearningService.clusterUser(paddedAnswers, mlRiskScore);

        List<AssessmentRecord> historicalRecords =
                assessmentRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);

        MachineLearningService.TrendAnalysis trendAnalysis =
                machineLearningService.analyzeTrend(historicalRecords);

        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setQ1(paddedAnswers[0]);
        record.setQ2(paddedAnswers[1]);
        record.setQ3(paddedAnswers[2]);
        record.setQ4(paddedAnswers[3]);
        record.setQ5(paddedAnswers[4]);
        record.setQ6(paddedAnswers[5]);
        record.setQ7(paddedAnswers[6]);
        record.setQ8(paddedAnswers[7]);
        record.setQ9(paddedAnswers[8]);
        record.setTotalScore(totalScore);
        record.setLevel(level);
        record.setSentimentText(sentimentText);
        record.setSentimentScore(sentimentResult.getScore());
        record.setMlRiskScore(mlRiskScore);
        record.setAnomalyDetected(anomalyResult.isAnomalous());
        record.setQuestionnaireCode(questionnaireCode);
        record.setAnswersJson(gson.toJson(answers));
        record.setCameraData(cameraData);

        if (voiceResult != null) {
            record.setVoiceEmotionScore(voiceResult.getEmotionScore());
            // 存储完整的语音分析结果，包含增强的声学特征
            record.setVoiceFeatures(gson.toJson(voiceResult));
        }

        assessmentRecordRepository.save(record);

        String suggestion = buildSuggestion(questionnaireCode, answers, totalScore, maxScore);
        boolean highRisk = isHighRisk(questionnaireCode, answers, totalScore, maxScore);

        AssessmentResult result = new AssessmentResult();
        result.setTotalScore(totalScore);
        result.setMaxScore(maxScore);
        result.setLevel(level);
        result.setLevelText(levelText);
        result.setSuggestion(suggestion);
        result.setHighRisk(highRisk);
        result.setMlRiskScore(mlRiskScore);
        result.setMlRiskLevel(mlRiskLevel);
        result.setSentimentAnalysis(sentimentResult);
        result.setRecommendations(recommendations);
        result.setTrendAnalysis(trendAnalysis);
        result.setClusterResult(clusterResult);
        result.setAnomalyDetection(anomalyResult);
        result.setHasHistoricalData(!historicalRecords.isEmpty());
        result.setHistoricalScores(
                historicalRecords.stream()
                        .map(AssessmentRecord::getTotalScore)
                        .collect(Collectors.toList())
        );
        result.setVoiceAnalysis(voiceResult);
        result.setAiEnhancement(aiEnhancement);
        result.setQuestionnaireCode(questionnaireCode);
        result.setCameraAnalysis(cameraAnalysis);

        model.addAttribute("result", result);
        model.addAttribute("questionnaires", questionnaireService.getAllActiveQuestionnaires());
        model.addAttribute("selectedQuestionnaire", questionnaireCode);
        model.addAttribute("cameraEnabled", cameraEnabled);
        model.addAttribute("cameraUpdateInterval", cameraUpdateInterval);

        return "index";
    }

    private int[] extractAnswers(HttpServletRequest request, List<Question> questions) {
        List<Integer> values = new ArrayList<>();
        if (questions != null && !questions.isEmpty()) {
            for (Question question : questions) {
                String paramName = "q" + question.getQuestionNumber();
                String value = request.getParameter(paramName);
                if (value == null) {
                    values.add(0);
                } else {
                    try {
                        values.add(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        values.add(0);
                    }
                }
            }
        } else {
            for (int i = 1; i <= 9; i++) {
                String value = request.getParameter("q" + i);
                if (value != null) {
                    try {
                        values.add(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        values.add(0);
                    }
                }
            }
        }
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    private int[] padAnswersForModel(int[] answers, int targetLength) {
        int[] padded = new int[targetLength];
        for (int i = 0; i < targetLength; i++) {
            padded[i] = i < answers.length ? answers[i] : 0;
        }
        return padded;
    }

    private String determineLevel(String questionnaireCode, int totalScore, int maxScore) {
        if ("PHQ-9".equalsIgnoreCase(questionnaireCode)) {
            if (totalScore >= 20) return "severe";
            if (totalScore >= 15) return "moderately-severe";
            if (totalScore >= 10) return "moderate";
            if (totalScore >= 5) return "mild";
            return "minimal";
        }
        if ("GAD-7".equalsIgnoreCase(questionnaireCode)) {
            if (totalScore >= 15) return "severe";
            if (totalScore >= 10) return "moderate";
            if (totalScore >= 5) return "mild";
            return "minimal";
        }

        double ratio = maxScore > 0 ? (double) totalScore / maxScore : 0.0;
        if (ratio >= 0.8) return "severe";
        if (ratio >= 0.6) return "moderately-severe";
        if (ratio >= 0.4) return "moderate";
        if (ratio >= 0.2) return "mild";
        return "minimal";
    }

    private String mapLevelText(String level) {
        switch (level) {
            case "minimal":
                return "无明显症状";
            case "mild":
                return "轻度风险";
            case "moderate":
                return "中度风险";
            case "moderately-severe":
                return "中重度风险";
            case "severe":
                return "重度风险";
            default:
                return "未知";
        }
    }

    private boolean isHighRisk(String questionnaireCode, int[] answers, int totalScore, int maxScore) {
        if ("PHQ-9".equalsIgnoreCase(questionnaireCode)) {
            return answers.length >= 9 && answers[8] > 0;
        }
        if ("GAD-7".equalsIgnoreCase(questionnaireCode)) {
            return totalScore >= 15;
        }
        double ratio = maxScore > 0 ? (double) totalScore / maxScore : 0.0;
        return ratio >= 0.75;
    }

    private String buildSuggestion(String questionnaireCode, int[] answers, int totalScore, int maxScore) {
        StringBuilder builder = new StringBuilder("根据您的具体回答，我们为您生成了以下个性化建议：\n\n");
        if ("PHQ-9".equalsIgnoreCase(questionnaireCode)) {
            int q1 = getAnswerValue(answers, 0);
            int q2 = getAnswerValue(answers, 1);
            int q3 = getAnswerValue(answers, 2);
            int q4 = getAnswerValue(answers, 3);
            int q5 = getAnswerValue(answers, 4);
            int q6 = getAnswerValue(answers, 5);
            int q7 = getAnswerValue(answers, 6);
            int q8 = getAnswerValue(answers, 7);
            int q9 = getAnswerValue(answers, 8);

            if (q1 >= 2 || q2 >= 2) {
                builder.append("• 【情绪与动力】尝试安排一些带来成就感的微型活动，保持与他人的联结。\n");
            }
            if (q3 >= 2 || q4 >= 2) {
                builder.append("• 【睡眠与精力】建立固定的作息时间，睡前安排放松仪式，例如深呼吸或冥想。\n");
            }
            if (q5 >= 2) {
                builder.append("• 【食欲变化】保持规律饮食，少量多餐，加入富含营养的食物。\n");
            }
            if (q6 >= 2) {
                builder.append("• 【自我评价】练习自我肯定，可以通过写下每日三件小确幸来降低自责感。\n");
            }
            if (q7 >= 2) {
                builder.append("• 【专注力】尝试番茄工作法，将任务拆分为可管理的小步骤，减少压力。\n");
            }
            if (q8 >= 2 || q9 >= 1) {
                builder.append("• 【安全提示】如出现自伤想法，请立即寻求专业帮助，并与信任的人保持联系。\n");
            }
        } else if ("GAD-7".equalsIgnoreCase(questionnaireCode)) {
            builder.append("• 【呼吸放松】每天进行两次腹式呼吸或渐进性肌肉放松练习。\n");
            builder.append("• 【担忧日记】将反复担忧的事情写下来，并与现实证据核对，帮助理性看待问题。\n");
            builder.append("• 【规律运动】每周至少三次中等强度运动，可有效缓解焦虑。\n");
        } else {
            builder.append("• 建议结合当前量表分数，记录日常状态，并考虑与心理健康专业人士沟通。\n");
        }

        double ratio = maxScore > 0 ? (double) totalScore / maxScore : 0.0;
        if (ratio <= 0.2) {
            builder.append("\n🎉 整体状态较稳定，继续保持积极的生活习惯和社交联系。");
        } else if (ratio <= 0.5) {
            builder.append("\n💡 建议适度关注自己的身心状态，可尝试情绪管理练习并寻求支持。");
        } else {
            builder.append("\n🚨 建议尽快寻求专业帮助，与心理咨询师或精神科医生讨论更深入的干预方案。");
        }

        return builder.toString();
    }

    private int getAnswerValue(int[] answers, int index) {
        return index < answers.length ? answers[index] : 0;
    }

    private String generateCameraInsight(int activityLevel, int postureScore, int movementCount) {
        StringBuilder insight = new StringBuilder();

        if (activityLevel < 20) {
            insight.append("活动水平偏低，可能提示疲劳或缺乏动力。");
        } else if (activityLevel < 50) {
            insight.append("活动水平适中，建议保持轻度运动。");
        } else {
            insight.append("活动水平良好，展现出较好的精力。");
        }

        if (postureScore < 70) {
            insight.append(" 姿态评分偏低，尝试抬头挺胸、放松肩颈，有助于改善情绪。");
        } else if (postureScore < 90) {
            insight.append(" 姿态较稳定，可继续保持舒适坐姿。");
        } else {
            insight.append(" 姿态表现出色，身体语言积极而开放。");
        }

        if (movementCount < 5) {
            insight.append(" 建议适度起身活动，缓解僵硬和低落。");
        } else if (movementCount > 20) {
            insight.append(" 动作较多，若伴随烦躁可尝试放慢节奏、做深呼吸。");
        }

        return insight.toString();
    }
    
    /**
     * 测试OpenSMILE配置
     */
    @GetMapping("/test-opensmile")
    @ResponseBody
    public String testOpenSmileConfiguration() {
        if (openSmileService == null) {
            return "OpenSMILE服务未注入（可能未启用）";
        }
        
        return openSmileService.testConfiguration();
    }
}
