package com.example.phq9assessment.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class OnlineAIService {
    
    @Value("${ai.online.enabled:false}")
    private boolean onlineEnabled;
    
    @Value("${ai.provider:baidu}")
    private String provider;
    
    @Value("${ai.baidu.api.key:}")
    private String baiduApiKey;
    
    @Value("${ai.baidu.api.secret:}")
    private String baiduApiSecret;
    
    @Value("${ai.openai.api.key:}")
    private String openaiApiKey;
    
    @Value("${ai.openai.api.endpoint:https://api.openai.com/v1}")
    private String openaiEndpoint;
    
    private final Gson gson = new Gson();
    private final SentimentAnalysisService sentimentAnalysisService;

    public OnlineAIService(SentimentAnalysisService sentimentAnalysisService) {
        this.sentimentAnalysisService = sentimentAnalysisService;
    }

    public AIEnhancementResult enhanceSentimentAnalysis(String text) {
        AIEnhancementResult result = new AIEnhancementResult();
        
        if (!onlineEnabled || text == null || text.trim().isEmpty()) {
            return getFallbackAnalysis(text);
        }

        try {
            if ("openai".equalsIgnoreCase(provider) && !openaiApiKey.isEmpty()) {
                return callOpenAIAPI(text);
            } else if ("baidu".equalsIgnoreCase(provider) && !baiduApiKey.isEmpty()) {
                return callBaiduAPI(text);
            }
        } catch (Exception e) {
            System.err.println("在线AI调用失败: " + e.getMessage());
        }
        
        return getFallbackAnalysis(text);
    }

    private AIEnhancementResult callOpenAIAPI(String text) {
        AIEnhancementResult result = new AIEnhancementResult();
        
        try {
            URL url = new URL(openaiEndpoint + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + openaiApiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");
            requestBody.add("messages", gson.toJsonTree(new Object[]{
                new Message("system", "你是一个专业的心理健康评估助手。请分析用户的文本，识别关键的心理健康指标。"),
                new Message("user", "请分析这段文本的情感状态和心理健康相关信息：" + text)
            }));
            requestBody.addProperty("temperature", 0.7);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonObject response = gson.fromJson(br, JsonObject.class);
                String aiResponse = response.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
                
                result.setEnhancedAnalysis(aiResponse);
                result.setOnlineAnalysis(true);
                result.setProvider("OpenAI");
                result.setSuccessful(true);
                result.setSummary(aiResponse.length() > 200 ? aiResponse.substring(0, 200) + "..." : aiResponse);
                result.setRiskLevel("参考本地评估");
                result.setConfidence(0.6);
            } else {
                result.setSuccessful(false);
            }
        } catch (Exception e) {
            result.setSuccessful(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }

    private AIEnhancementResult callBaiduAPI(String text) {
        AIEnhancementResult result = new AIEnhancementResult();
        
        try {
            URL url = new URL("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + 
                            baiduApiKey + "&client_secret=" + baiduApiSecret);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonObject tokenResponse = gson.fromJson(br, JsonObject.class);
                String accessToken = tokenResponse.get("access_token").getAsString();
                
                String analysisResult = callBaiduNLPAPI(accessToken, text);
                result.setEnhancedAnalysis(analysisResult);
                result.setOnlineAnalysis(true);
                result.setProvider("Baidu");
                result.setSuccessful(true);
                try {
                    JsonObject parsed = gson.fromJson(analysisResult, JsonObject.class);
                    if (parsed != null && parsed.has("items")) {
                        JsonObject item = parsed.getAsJsonArray("items").get(0).getAsJsonObject();
                        int sentiment = item.has("sentiment") ? item.get("sentiment").getAsInt() : 1;
                        double confidence = item.has("confidence") ? item.get("confidence").getAsDouble() : 0.5;
                        String risk;
                        if (sentiment == 0) {
                            risk = "偏负面";
                        } else if (sentiment == 2) {
                            risk = "偏正面";
                        } else {
                            risk = "中性";
                        }
                        result.setRiskLevel(risk);
                        result.setConfidence(confidence);
                        result.setSummary("百度NLP情感分类：" + risk + "，置信度：" + String.format("%.2f", confidence));
                    }
                } catch (Exception ignore) {}
            } else {
                result.setSuccessful(false);
            }
        } catch (Exception e) {
            result.setSuccessful(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }

    private String callBaiduNLPAPI(String accessToken, String text) throws Exception {
        URL url = new URL("https://aip.baidubce.com/rpc/2.0/nlp/v1/sentiment_classify?access_token=" + accessToken);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JsonObject body = new JsonObject();
        body.addProperty("text", text);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(gson.toJson(body).getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JsonObject response = gson.fromJson(br, JsonObject.class);
        return response.toString();
    }

    private AIEnhancementResult getFallbackAnalysis(String text) {
        AIEnhancementResult result = new AIEnhancementResult();
        result.setOnlineAnalysis(false);
        result.setProvider("Local (Fallback)");
        
        SentimentAnalysisService.SentimentAnalysisResult localAnalysis = sentimentAnalysisService.analyzeSentiment(text);
        double score = localAnalysis.getScore();
        String sentiment = localAnalysis.getSentiment();
        List<String> signals = new ArrayList<>();
        signals.add("情感倾向：" + sentiment);
        if (!localAnalysis.getNegativeWords().isEmpty()) {
            signals.add("消极词汇：" + String.join("、", localAnalysis.getNegativeWords()));
        }
        if (!localAnalysis.getPositiveWords().isEmpty()) {
            signals.add("积极词汇：" + String.join("、", localAnalysis.getPositiveWords()));
        }
        if (!localAnalysis.getKeywords().isEmpty()) {
            signals.add("关键词：" + String.join("、", localAnalysis.getKeywords()));
        }

        List<String> actions = new ArrayList<>();
        if (score < -0.3) {
            actions.add("建议进行放松训练，如腹式呼吸或冥想");
            actions.add("保持规律作息并寻求可信赖的支持");
        } else if (score > 0.3) {
            actions.add("保持积极的生活习惯，继续记录积极事件");
        } else {
            actions.add("关注身心状态，适度安排运动和休息");
        }

        result.setEnhancedAnalysis("本地分析结果 - 情感分数: " + String.format("%.2f", score));
        result.setSummary("本地分析：" + sentiment + " (" + String.format("%.2f", score) + ")");
        result.setRiskLevel(score < -0.3 ? "偏负面" : (score > 0.3 ? "偏正面" : "中性"));
        result.setConfidence(0.7);
        result.setSignals(signals);
        result.setActions(actions);
        result.setSuccessful(true);
        
        return result;
    }

    public static class AIEnhancementResult {
        private String enhancedAnalysis;
        private boolean onlineAnalysis;
        private String provider;
        private boolean successful;
        private String errorMessage;
        private String summary;
        private String riskLevel;
        private Double confidence;
        private List<String> signals;
        private List<String> actions;

        public String getEnhancedAnalysis() {
            return enhancedAnalysis;
        }

        public void setEnhancedAnalysis(String enhancedAnalysis) {
            this.enhancedAnalysis = enhancedAnalysis;
        }

        public boolean isOnlineAnalysis() {
            return onlineAnalysis;
        }

        public void setOnlineAnalysis(boolean onlineAnalysis) {
            this.onlineAnalysis = onlineAnalysis;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public List<String> getSignals() {
            return signals;
        }

        public void setSignals(List<String> signals) {
            this.signals = signals;
        }

        public List<String> getActions() {
            return actions;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }
    }

    private static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
