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
                OpenSmileService.OpenSmileResult openSmileResult = openSmileService.extractFeatures(audioBase64);
                
                if (openSmileResult.isSuccess()) {
                    // 使用OpenSMILE的结果
                    result.setEmotionScore(openSmileResult.getDepressionScore());
                    result.setEmotionCategory(openSmileResult.getDepressionLevel());
                    result.setConfidence(0.95); // OpenSMILE更可靠
                    result.setFeatures(openSmileResult.getFeatures());
                    result.setUsingOpenSmile(true);
                    result.setOpenSmileConfigType(openSmileResult.getConfigType());
                    result.setFeatureCount(openSmileResult.getFeatureCount());
                    return result;
                }
            }
            
            // Fallback到原有的简单特征提取方法
            double emotionScore = calculateEmotionFromAudio(audioBase64);
            result.setEmotionScore(emotionScore);
            result.setEmotionCategory(categorizeEmotion(emotionScore));
            result.setConfidence(0.70); // 简单方法置信度较低
            result.setFeatures(extractAudioFeatures(audioBase64));
            result.setUsingOpenSmile(false);
        } catch (Exception e) {
            result.setEmotionScore(0.0);
            result.setEmotionCategory("error");
            result.setConfidence(0.0);
            result.setUsingOpenSmile(false);
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
    }
}
