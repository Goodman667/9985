package com.example.phq9assessment.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class VoiceDetectionService {
    
    @Value("${ai.voice.enabled:false}")
    private boolean voiceEnabled;
    
    @Value("${ai.voice.api.key:}")
    private String voiceApiKey;
    
    @Value("${ai.voice.api.endpoint:}")
    private String voiceApiEndpoint;
    
    @Autowired(required = false)
    private OpenSmileService openSmileService;
    
    private final Gson gson = new Gson();

    public VoiceAnalysisResult analyzeVoiceFeatures(String audioBase64) {
        VoiceAnalysisResult result = new VoiceAnalysisResult();
        
        if (!voiceEnabled || audioBase64 == null || audioBase64.trim().isEmpty()) {
            result.setEmotionScore(0.0);
            result.setEmotionCategory("neutral");
            result.setConfidence(0.0);
            result.setUsingOpenSmile(false);
            return result;
        }

        try {
            // 优先使用OpenSMILE进行专业特征提取
            if (openSmileService != null && openSmileService.isAvailable()) {
                System.out.println("VoiceDetectionService: OpenSMILE可用，开始特征提取");
                OpenSmileService.OpenSmileResult openSmileResult = openSmileService.extractFeatures(audioBase64);
                
                if (openSmileResult.isSuccess()) {
                    System.out.println("VoiceDetectionService: OpenSMILE特征提取成功，提取了 " + openSmileResult.getFeatureCount() + " 个特征");
                    // 使用OpenSMILE的结果
                    result.setEmotionScore(openSmileResult.getDepressionScore());
                    result.setEmotionCategory(openSmileResult.getDepressionLevel());
                    result.setConfidence(0.95); // OpenSMILE更可靠
                    result.setFeatures(openSmileResult.getFeatures());
                    result.setUsingOpenSmile(true);
                    result.setOpenSmileConfigType(openSmileResult.getConfigType());
                    result.setFeatureCount(openSmileResult.getFeatureCount());
                    
                    // 传播新的增强字段
                    result.setAcousticSummary(openSmileResult.getAcousticSummary());
                    result.setEmotionalIndicators(openSmileResult.getEmotionalIndicators());
                    result.setAudioStats(openSmileResult.getAudioStats());
                    result.setTopFeatures(openSmileResult.getTopFeatures());
                    
                    System.out.println("VoiceDetectionService: 声学摘要字段数: " + 
                        (openSmileResult.getAcousticSummary() != null ? openSmileResult.getAcousticSummary().size() : 0));
                    System.out.println("VoiceDetectionService: 情感指标字段数: " + 
                        (openSmileResult.getEmotionalIndicators() != null ? openSmileResult.getEmotionalIndicators().size() : 0));
                    return result;
                } else {
                    System.out.println("VoiceDetectionService: OpenSMILE特征提取失败: " + openSmileResult.getErrorMessage());
                }
            } else {
                System.out.println("VoiceDetectionService: OpenSMILE不可用 - " + 
                    (openSmileService == null ? "服务未注入" : "服务不可用"));
            }
            
            // Fallback到原有的简单特征提取方法
            System.out.println("VoiceDetectionService: 使用fallback方法进行简单特征提取");
            double emotionScore = calculateEmotionFromAudio(audioBase64);
            result.setEmotionScore(emotionScore);
            result.setEmotionCategory(categorizeEmotion(emotionScore));
            result.setConfidence(0.70); // 简单方法置信度较低
            Map<String, Double> basicFeatures = extractAudioFeatures(audioBase64);
            result.setFeatures(basicFeatures);
            result.setUsingOpenSmile(false);
            
            // 为fallback方法生成占位符数据，保持结构一致性
            Map<String, Double> fallbackAcousticSummary = new HashMap<>();
            Map<String, Double> fallbackEmotionalIndicators = new HashMap<>();
            Map<String, Object> fallbackAudioStats = new HashMap<>();
            Map<String, Double> fallbackTopFeatures = new HashMap<>();
            
            // 从基础特征中提取一些值作为占位符
            if (basicFeatures != null) {
                if (basicFeatures.containsKey("volume")) {
                    fallbackAcousticSummary.put("响度均值", basicFeatures.get("volume"));
                    fallbackEmotionalIndicators.put("能量水平", Math.min(1.0, basicFeatures.get("volume") * 10.0));
                }
                if (basicFeatures.containsKey("pitch")) {
                    fallbackAcousticSummary.put("基频均值", basicFeatures.get("pitch"));
                }
                if (basicFeatures.containsKey("pace")) {
                    fallbackAcousticSummary.put("语速指标", basicFeatures.get("pace"));
                }
                if (basicFeatures.containsKey("duration_ms")) {
                    fallbackAudioStats.put("估计时长(ms)", basicFeatures.get("duration_ms").intValue());
                }
            }
            
            // 添加情感相关的占位符指标
            fallbackEmotionalIndicators.put("活跃度", Math.max(0.0, Math.min(1.0, (emotionScore + 1.0) / 2.0)));
            fallbackEmotionalIndicators.put("紧张度", Math.max(0.0, Math.min(1.0, Math.abs(emotionScore))));
            fallbackEmotionalIndicators.put("情绪稳定性", 0.6); // 默认中等稳定性
            fallbackEmotionalIndicators.put("抑郁倾向", Math.max(0.0, emotionScore));
            
            // 音频统计信息
            fallbackAudioStats.put("特征总数", basicFeatures != null ? basicFeatures.size() : 0);
            fallbackAudioStats.put("有效特征数", basicFeatures != null ? basicFeatures.size() : 0);
            fallbackAudioStats.put("处理时间", System.currentTimeMillis());
            
            // Top特征就是所有基础特征
            if (basicFeatures != null) {
                fallbackTopFeatures.putAll(basicFeatures);
            }
            
            result.setAcousticSummary(fallbackAcousticSummary);
            result.setEmotionalIndicators(fallbackEmotionalIndicators);
            result.setAudioStats(fallbackAudioStats);
            result.setTopFeatures(fallbackTopFeatures);
            
            System.out.println("VoiceDetectionService: Fallback分析完成，生成了占位符数据");
        } catch (Exception e) {
            System.out.println("VoiceDetectionService: 语音分析异常: " + e.getMessage());
            double emotionScore = calculateEmotionFromAudio(audioBase64);
            result.setEmotionScore(emotionScore);
            result.setEmotionCategory(categorizeEmotion(emotionScore));
            result.setConfidence(0.70);
            result.setUsingOpenSmile(false);
            Map<String, Double> basicFeatures = extractAudioFeatures(audioBase64);
            result.setFeatures(basicFeatures);
            Map<String, Double> fallbackAcousticSummary = new HashMap<>();
            Map<String, Double> fallbackEmotionalIndicators = new HashMap<>();
            Map<String, Object> fallbackAudioStats = new HashMap<>();
            Map<String, Double> fallbackTopFeatures = new HashMap<>();
            if (basicFeatures != null) {
                if (basicFeatures.containsKey("volume")) {
                    fallbackAcousticSummary.put("响度均值", basicFeatures.get("volume"));
                    fallbackEmotionalIndicators.put("能量水平", Math.min(1.0, basicFeatures.get("volume") * 10.0));
                }
                if (basicFeatures.containsKey("pitch")) {
                    fallbackAcousticSummary.put("基频均值", basicFeatures.get("pitch"));
                }
                if (basicFeatures.containsKey("pace")) {
                    fallbackAcousticSummary.put("语速指标", basicFeatures.get("pace"));
                }
                if (basicFeatures.containsKey("duration_ms")) {
                    fallbackAudioStats.put("估计时长(ms)", basicFeatures.get("duration_ms").intValue());
                }
            }
            fallbackEmotionalIndicators.put("活跃度", Math.max(0.0, Math.min(1.0, (emotionScore + 1.0) / 2.0)));
            fallbackEmotionalIndicators.put("紧张度", Math.max(0.0, Math.min(1.0, Math.abs(emotionScore))));
            fallbackEmotionalIndicators.put("情绪稳定性", 0.6);
            fallbackEmotionalIndicators.put("抑郁倾向", Math.max(0.0, emotionScore));
            fallbackAudioStats.put("特征总数", basicFeatures != null ? basicFeatures.size() : 0);
            fallbackAudioStats.put("有效特征数", basicFeatures != null ? basicFeatures.size() : 0);
            fallbackAudioStats.put("配置类型", "基础分析");
            fallbackAudioStats.put("处理时间", System.currentTimeMillis());
            if (basicFeatures != null) {
                fallbackTopFeatures.putAll(basicFeatures);
            }
            result.setAcousticSummary(fallbackAcousticSummary);
            result.setEmotionalIndicators(fallbackEmotionalIndicators);
            result.setAudioStats(fallbackAudioStats);
            result.setTopFeatures(fallbackTopFeatures);
        }
        
        return result;
    }

    private double calculateEmotionFromAudio(String audioBase64) {
        try {
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            
            double volume = calculateVolume(audioBytes);
            double pitch = estimatePitch(audioBytes);
            double pace = estimatePace(audioBytes);
            
            double emotionScore = (volume * 0.4 + (1.0 - pitch) * 0.3 + (1.0 - pace) * 0.3) - 0.5;
            
            return Math.max(-1.0, Math.min(1.0, emotionScore));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double calculateVolume(byte[] audioBytes) {
        if (audioBytes.length == 0) return 0.0;
        
        double sum = 0.0;
        for (byte b : audioBytes) {
            sum += Math.abs(b) / 128.0;
        }
        return sum / audioBytes.length;
    }

    private double estimatePitch(byte[] audioBytes) {
        if (audioBytes.length < 2) return 0.5;
        
        int zeroCount = 0;
        for (int i = 0; i < audioBytes.length - 1; i++) {
            if ((audioBytes[i] < 0 && audioBytes[i + 1] >= 0) || 
                (audioBytes[i] >= 0 && audioBytes[i + 1] < 0)) {
                zeroCount++;
            }
        }
        
        double frequency = zeroCount / (double) audioBytes.length;
        return Math.min(1.0, frequency * 500);
    }

    private double estimatePace(byte[] audioBytes) {
        if (audioBytes.length < 100) return 0.5;
        
        double[] energyFrames = new double[audioBytes.length / 100];
        for (int i = 0; i < energyFrames.length; i++) {
            double energy = 0.0;
            for (int j = 0; j < 100 && i * 100 + j < audioBytes.length; j++) {
                energy += Math.abs(audioBytes[i * 100 + j]);
            }
            energyFrames[i] = energy / 100.0;
        }
        
        double variance = calculateVariance(energyFrames);
        return Math.min(1.0, variance / 50.0);
    }

    private double calculateVariance(double[] values) {
        if (values.length == 0) return 0.0;
        
        double mean = 0.0;
        for (double v : values) {
            mean += v;
        }
        mean /= values.length;
        
        double variance = 0.0;
        for (double v : values) {
            variance += (v - mean) * (v - mean);
        }
        return variance / values.length;
    }

    private String categorizeEmotion(double score) {
        if (score < -0.5) {
            return "very_negative";
        } else if (score < -0.2) {
            return "negative";
        } else if (score < 0.2) {
            return "neutral";
        } else if (score < 0.5) {
            return "positive";
        } else {
            return "very_positive";
        }
    }

    private Map<String, Double> extractAudioFeatures(String audioBase64) {
        Map<String, Double> features = new HashMap<>();
        try {
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            features.put("volume", calculateVolume(audioBytes));
            features.put("pitch", estimatePitch(audioBytes));
            features.put("pace", estimatePace(audioBytes));
            features.put("duration_ms", (double) audioBytes.length / 48.0);
        } catch (Exception e) {
            features.put("volume", 0.0);
            features.put("pitch", 0.0);
            features.put("pace", 0.0);
        }
        return features;
    }

    public static class VoiceAnalysisResult {
        private double emotionScore;
        private String emotionCategory;
        private double confidence;
        private Map<String, Double> features;
        private boolean usingOpenSmile;
        private String openSmileConfigType;
        private int featureCount;
        
        // New fields for enhanced acoustic analytics
        private Map<String, Double> acousticSummary;
        private Map<String, Double> emotionalIndicators;
        private Map<String, Object> audioStats;
        private Map<String, Double> topFeatures;

        public double getEmotionScore() {
            return emotionScore;
        }

        public void setEmotionScore(double emotionScore) {
            this.emotionScore = emotionScore;
        }

        public String getEmotionCategory() {
            return emotionCategory;
        }

        public void setEmotionCategory(String emotionCategory) {
            this.emotionCategory = emotionCategory;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public Map<String, Double> getFeatures() {
            return features;
        }

        public void setFeatures(Map<String, Double> features) {
            this.features = features;
        }

        public boolean isUsingOpenSmile() {
            return usingOpenSmile;
        }

        public void setUsingOpenSmile(boolean usingOpenSmile) {
            this.usingOpenSmile = usingOpenSmile;
        }

        public String getOpenSmileConfigType() {
            return openSmileConfigType;
        }

        public void setOpenSmileConfigType(String openSmileConfigType) {
            this.openSmileConfigType = openSmileConfigType;
        }

        public int getFeatureCount() {
            return featureCount;
        }

        public void setFeatureCount(int featureCount) {
            this.featureCount = featureCount;
        }

        public Map<String, Double> getAcousticSummary() {
            return acousticSummary;
        }

        public void setAcousticSummary(Map<String, Double> acousticSummary) {
            this.acousticSummary = acousticSummary;
        }

        public Map<String, Double> getEmotionalIndicators() {
            return emotionalIndicators;
        }

        public void setEmotionalIndicators(Map<String, Double> emotionalIndicators) {
            this.emotionalIndicators = emotionalIndicators;
        }

        public Map<String, Object> getAudioStats() {
            return audioStats;
        }

        public void setAudioStats(Map<String, Object> audioStats) {
            this.audioStats = audioStats;
        }

        public Map<String, Double> getTopFeatures() {
            return topFeatures;
        }

        public void setTopFeatures(Map<String, Double> topFeatures) {
            this.topFeatures = topFeatures;
        }
    }
}
