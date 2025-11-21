package com.example.phq9assessment.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SentimentAnalysisService {
    
    private static final Map<String, Double> NEGATIVE_WORDS = new HashMap<>();
    private static final Map<String, Double> POSITIVE_WORDS = new HashMap<>();
    private static final Set<String> DEPRESSION_KEYWORDS = new HashSet<>();
    private static final Set<String> ANXIETY_KEYWORDS = new HashSet<>();
    
    static {
        NEGATIVE_WORDS.put("痛苦", -0.9);
        NEGATIVE_WORDS.put("难过", -0.8);
        NEGATIVE_WORDS.put("悲伤", -0.8);
        NEGATIVE_WORDS.put("抑郁", -0.9);
        NEGATIVE_WORDS.put("绝望", -1.0);
        NEGATIVE_WORDS.put("无助", -0.9);
        NEGATIVE_WORDS.put("孤独", -0.7);
        NEGATIVE_WORDS.put("疲惫", -0.6);
        NEGATIVE_WORDS.put("焦虑", -0.8);
        NEGATIVE_WORDS.put("害怕", -0.7);
        NEGATIVE_WORDS.put("恐惧", -0.8);
        NEGATIVE_WORDS.put("担心", -0.6);
        NEGATIVE_WORDS.put("烦躁", -0.6);
        NEGATIVE_WORDS.put("失眠", -0.7);
        NEGATIVE_WORDS.put("噩梦", -0.7);
        NEGATIVE_WORDS.put("绝望", -1.0);
        NEGATIVE_WORDS.put("厌世", -1.0);
        NEGATIVE_WORDS.put("自杀", -1.0);
        NEGATIVE_WORDS.put("死", -0.9);
        NEGATIVE_WORDS.put("消失", -0.7);
        NEGATIVE_WORDS.put("崩溃", -0.9);
        NEGATIVE_WORDS.put("无望", -0.9);
        NEGATIVE_WORDS.put("空虚", -0.7);
        NEGATIVE_WORDS.put("麻木", -0.7);
        NEGATIVE_WORDS.put("迷茫", -0.6);
        NEGATIVE_WORDS.put("压力", -0.6);
        NEGATIVE_WORDS.put("沮丧", -0.7);
        NEGATIVE_WORDS.put("绝望", -1.0);
        
        POSITIVE_WORDS.put("开心", 0.7);
        POSITIVE_WORDS.put("快乐", 0.8);
        POSITIVE_WORDS.put("幸福", 0.9);
        POSITIVE_WORDS.put("希望", 0.8);
        POSITIVE_WORDS.put("乐观", 0.7);
        POSITIVE_WORDS.put("积极", 0.7);
        POSITIVE_WORDS.put("放松", 0.6);
        POSITIVE_WORDS.put("平静", 0.6);
        POSITIVE_WORDS.put("满足", 0.7);
        POSITIVE_WORDS.put("充实", 0.7);
        POSITIVE_WORDS.put("健康", 0.6);
        POSITIVE_WORDS.put("精力", 0.6);
        POSITIVE_WORDS.put("活力", 0.7);
        POSITIVE_WORDS.put("温暖", 0.6);
        POSITIVE_WORDS.put("爱", 0.8);
        POSITIVE_WORDS.put("支持", 0.7);
        POSITIVE_WORDS.put("好转", 0.8);
        POSITIVE_WORDS.put("改善", 0.7);
        
        DEPRESSION_KEYWORDS.addAll(Arrays.asList(
            "抑郁", "悲伤", "绝望", "无助", "空虚", "麻木", "失去兴趣", 
            "无价值", "自责", "疲惫", "失眠", "食欲不振"
        ));
        
        ANXIETY_KEYWORDS.addAll(Arrays.asList(
            "焦虑", "紧张", "担心", "恐惧", "害怕", "不安", "烦躁", 
            "心慌", "出汗", "颤抖", "坐立不安"
        ));
    }
    
    public SentimentAnalysisResult analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentAnalysisResult(0.0, new ArrayList<>(), new ArrayList<>(), 
                                               new ArrayList<>(), "neutral");
        }
        
        text = text.toLowerCase();
        
        double totalScore = 0.0;
        int wordCount = 0;
        List<String> detectedNegativeWords = new ArrayList<>();
        List<String> detectedPositiveWords = new ArrayList<>();
        List<String> detectedKeywords = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : NEGATIVE_WORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                totalScore += entry.getValue();
                wordCount++;
                detectedNegativeWords.add(entry.getKey());
            }
        }
        
        for (Map.Entry<String, Double> entry : POSITIVE_WORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                totalScore += entry.getValue();
                wordCount++;
                detectedPositiveWords.add(entry.getKey());
            }
        }
        
        for (String keyword : DEPRESSION_KEYWORDS) {
            if (text.contains(keyword)) {
                detectedKeywords.add(keyword + "(抑郁)");
            }
        }
        
        for (String keyword : ANXIETY_KEYWORDS) {
            if (text.contains(keyword)) {
                detectedKeywords.add(keyword + "(焦虑)");
            }
        }
        
        double normalizedScore = wordCount > 0 ? totalScore / wordCount : 0.0;
        normalizedScore = Math.max(-1.0, Math.min(1.0, normalizedScore));
        
        String sentiment;
        if (normalizedScore < -0.3) {
            sentiment = "negative";
        } else if (normalizedScore > 0.3) {
            sentiment = "positive";
        } else {
            sentiment = "neutral";
        }
        
        return new SentimentAnalysisResult(normalizedScore, detectedNegativeWords, 
                                          detectedPositiveWords, detectedKeywords, sentiment);
    }
    
    public static class SentimentAnalysisResult {
        private double score;
        private List<String> negativeWords;
        private List<String> positiveWords;
        private List<String> keywords;
        private String sentiment;
        
        public SentimentAnalysisResult(double score, List<String> negativeWords, 
                                      List<String> positiveWords, List<String> keywords, 
                                      String sentiment) {
            this.score = score;
            this.negativeWords = negativeWords;
            this.positiveWords = positiveWords;
            this.keywords = keywords;
            this.sentiment = sentiment;
        }
        
        public double getScore() {
            return score;
        }
        
        public List<String> getNegativeWords() {
            return negativeWords;
        }
        
        public List<String> getPositiveWords() {
            return positiveWords;
        }
        
        public List<String> getKeywords() {
            return keywords;
        }
        
        public String getSentiment() {
            return sentiment;
        }
        
        public String getSentimentText() {
            if ("negative".equals(sentiment)) {
                return "消极";
            } else if ("positive".equals(sentiment)) {
                return "积极";
            } else {
                return "中性";
            }
        }
    }
}
