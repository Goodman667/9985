package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.CognitivePattern;
import com.example.phq9assessment.entity.JournalEntry;
import com.example.phq9assessment.repository.CognitivePatternRepository;
import com.example.phq9assessment.repository.JournalEntryRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CognitivePatternAnalyzer {
    
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    
    @Autowired
    private CognitivePatternRepository cognitivePatternRepository;
    
    private final Gson gson = new Gson();
    
    private static final Map<String, List<String>> PATTERN_KEYWORDS = new HashMap<>();
    private static final Map<String, String> PATTERN_DESCRIPTIONS = new HashMap<>();
    private static final Map<String, String> CBT_CHALLENGES = new HashMap<>();
    
    static {
        PATTERN_KEYWORDS.put("CATASTROPHIZING", Arrays.asList(
            "完蛋了", "糟糕透了", "最坏", "灾难", "毁了", "永远", "再也不会", "必然会"
        ));
        PATTERN_KEYWORDS.put("ALL_OR_NOTHING", Arrays.asList(
            "总是", "从不", "每次都", "绝对", "一定", "完全", "永远", "从来没有"
        ));
        PATTERN_KEYWORDS.put("OVERGENERALIZATION", Arrays.asList(
            "所有人都", "每个人都", "没有人", "任何", "全部", "都是"
        ));
        PATTERN_KEYWORDS.put("MIND_READING", Arrays.asList(
            "他们一定认为", "肯定觉得我", "别人会想", "他们在想"
        ));
        PATTERN_KEYWORDS.put("FORTUNE_TELLING", Arrays.asList(
            "肯定会失败", "注定", "一定会", "不会成功", "永远不可能"
        ));
        PATTERN_KEYWORDS.put("EMOTIONAL_REASONING", Arrays.asList(
            "我感觉", "我觉得自己", "感到", "因为我感觉"
        ));
        PATTERN_KEYWORDS.put("SHOULD_STATEMENTS", Arrays.asList(
            "应该", "必须", "不得不", "理应", "本该"
        ));
        PATTERN_KEYWORDS.put("LABELING", Arrays.asList(
            "我是个", "我就是", "我这种", "我这样的人"
        ));
        PATTERN_KEYWORDS.put("PERSONALIZATION", Arrays.asList(
            "都是我的错", "是我导致", "怪我", "因为我"
        ));
        
        PATTERN_DESCRIPTIONS.put("CATASTROPHIZING", "灾难化思维");
        PATTERN_DESCRIPTIONS.put("ALL_OR_NOTHING", "黑白思维/二分法思维");
        PATTERN_DESCRIPTIONS.put("OVERGENERALIZATION", "过度概括");
        PATTERN_DESCRIPTIONS.put("MIND_READING", "读心术");
        PATTERN_DESCRIPTIONS.put("FORTUNE_TELLING", "算命师思维");
        PATTERN_DESCRIPTIONS.put("EMOTIONAL_REASONING", "情绪化推理");
        PATTERN_DESCRIPTIONS.put("SHOULD_STATEMENTS", "应该式思维");
        PATTERN_DESCRIPTIONS.put("LABELING", "贴标签");
        PATTERN_DESCRIPTIONS.put("PERSONALIZATION", "个人化/责任归因");
        
        CBT_CHALLENGES.put("CATASTROPHIZING", 
            "这真的是最坏的情况吗？有没有其他可能的结果？即使最坏的情况发生，我能应对吗？");
        CBT_CHALLENGES.put("ALL_OR_NOTHING", 
            "事情真的只有这两种极端吗？是否存在中间地带？我能否用百分比来描述这种情况？");
        CBT_CHALLENGES.put("OVERGENERALIZATION", 
            "这是否只是一次或几次的经历？我是否过度推广了单一事件？有没有反例？");
        CBT_CHALLENGES.put("MIND_READING", 
            "我真的知道别人在想什么吗？有没有其他可能的解释？我可以直接询问吗？");
        CBT_CHALLENGES.put("FORTUNE_TELLING", 
            "我真的能预测未来吗？有什么证据支持这个预测？过去是否有类似情况结果不同？");
        CBT_CHALLENGES.put("EMOTIONAL_REASONING", 
            "仅仅因为我有这种感觉，它就一定是真的吗？有什么客观证据支持或反驳这种感觉？");
        CBT_CHALLENGES.put("SHOULD_STATEMENTS", 
            "这是谁的规则？这个要求现实吗？如果朋友有同样情况，我会对他们说什么？");
        CBT_CHALLENGES.put("LABELING", 
            "我是一个完整的人，不能用一个标签定义。这个标签准确吗？我有哪些积极特质？");
        CBT_CHALLENGES.put("PERSONALIZATION", 
            "我真的要为所有事情负责吗？有哪些是我无法控制的因素？其他人有什么责任？");
    }
    
    public JournalEntry createJournalEntry(String userId, String content, String entryType) {
        JournalEntry entry = new JournalEntry();
        entry.setUserId(userId);
        entry.setContent(content);
        entry.setEntryType(entryType);
        
        entry = journalEntryRepository.save(entry);
        
        analyzeAndStoreCognitivePatterns(entry);
        
        return entry;
    }
    
    public Map<String, Object> analyzeJournalEntry(String content) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> patterns = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : PATTERN_KEYWORDS.entrySet()) {
            String patternType = entry.getKey();
            List<String> keywords = entry.getValue();
            
            List<String> foundKeywords = new ArrayList<>();
            List<String> evidenceTexts = new ArrayList<>();
            
            for (String keyword : keywords) {
                if (content.contains(keyword)) {
                    foundKeywords.add(keyword);
                    
                    int index = content.indexOf(keyword);
                    int start = Math.max(0, index - 10);
                    int end = Math.min(content.length(), index + keyword.length() + 20);
                    String evidence = content.substring(start, end);
                    evidenceTexts.add(evidence);
                }
            }
            
            if (!foundKeywords.isEmpty()) {
                Map<String, Object> pattern = new HashMap<>();
                pattern.put("type", patternType);
                pattern.put("description", PATTERN_DESCRIPTIONS.get(patternType));
                pattern.put("foundKeywords", foundKeywords);
                pattern.put("confidence", calculateConfidence(foundKeywords.size()));
                pattern.put("evidence", evidenceTexts);
                pattern.put("cbtChallenge", CBT_CHALLENGES.get(patternType));
                pattern.put("reframingSuggestion", generateReframingSuggestion(patternType));
                
                patterns.add(pattern);
            }
        }
        
        result.put("patterns", patterns);
        result.put("patternCount", patterns.size());
        result.put("overallRisk", assessOverallRisk(patterns.size()));
        result.put("cbtSuggestions", generateCBTSuggestions(patterns));
        
        return result;
    }
    
    private void analyzeAndStoreCognitivePatterns(JournalEntry entry) {
        Map<String, Object> analysis = analyzeJournalEntry(entry.getContent());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> patterns = (List<Map<String, Object>>) analysis.get("patterns");
        
        entry.setCognitivePatternsJson(gson.toJson(patterns));
        
        @SuppressWarnings("unchecked")
        List<String> cbtSuggestions = (List<String>) analysis.get("cbtSuggestions");
        entry.setCbtSuggestions(String.join("\n\n", cbtSuggestions));
        
        journalEntryRepository.save(entry);
        
        for (Map<String, Object> patternData : patterns) {
            CognitivePattern pattern = new CognitivePattern();
            pattern.setJournalEntry(entry);
            pattern.setPatternType((String) patternData.get("type"));
            
            @SuppressWarnings("unchecked")
            List<String> evidence = (List<String>) patternData.get("evidence");
            pattern.setEvidenceText(String.join("; ", evidence));
            
            pattern.setConfidenceScore((Double) patternData.get("confidence"));
            pattern.setCbtChallenge((String) patternData.get("cbtChallenge"));
            pattern.setReframingSuggestion((String) patternData.get("reframingSuggestion"));
            
            cognitivePatternRepository.save(pattern);
        }
    }
    
    private double calculateConfidence(int keywordCount) {
        return Math.min(0.9, 0.3 + (keywordCount * 0.15));
    }
    
    private String assessOverallRisk(int patternCount) {
        if (patternCount >= 5) return "HIGH";
        if (patternCount >= 3) return "MEDIUM";
        if (patternCount >= 1) return "LOW";
        return "MINIMAL";
    }
    
    private List<String> generateCBTSuggestions(List<Map<String, Object>> patterns) {
        List<String> suggestions = new ArrayList<>();
        
        suggestions.add("认知重构练习：尝试用更客观、平衡的方式重新表述你的想法。");
        
        if (patterns.stream().anyMatch(p -> "CATASTROPHIZING".equals(p.get("type")))) {
            suggestions.add("灾难化思维对策：问自己'最可能的结果是什么？'而不是'最坏的结果是什么？'");
        }
        
        if (patterns.stream().anyMatch(p -> "ALL_OR_NOTHING".equals(p.get("type")))) {
            suggestions.add("黑白思维对策：尝试在0-100的量表上评估情况，而不是只有'好'或'坏'两种选择。");
        }
        
        suggestions.add("证据检验：为你的想法寻找支持和反对的证据，保持客观。");
        suggestions.add("自我同情：用对待朋友的方式对待自己，给自己更多理解和宽容。");
        
        return suggestions;
    }
    
    private String generateReframingSuggestion(String patternType) {
        switch (patternType) {
            case "CATASTROPHIZING":
                return "尝试重新表述：'这确实是个挑战，但我可以一步步应对，最可能的结果是...'";
            case "ALL_OR_NOTHING":
                return "尝试重新表述：'这次不够完美，但我在某些方面做得还不错...'";
            case "OVERGENERALIZATION":
                return "尝试重新表述：'这次的情况是...，但不代表每次都会这样'";
            case "MIND_READING":
                return "尝试重新表述：'我不确定他们在想什么，但我可以询问或观察更多信息'";
            case "FORTUNE_TELLING":
                return "尝试重新表述：'我不能预测未来，但我可以为各种可能性做准备'";
            case "EMOTIONAL_REASONING":
                return "尝试重新表述：'我现在有这种感觉，但感觉并不总是反映事实'";
            case "SHOULD_STATEMENTS":
                return "尝试重新表述：'我希望...但如果没做到也没关系，我已经尽力了'";
            case "LABELING":
                return "尝试重新表述：'我在这件事上表现不佳，但这不能定义我这个人'";
            case "PERSONALIZATION":
                return "尝试重新表述：'我在其中有一部分责任，但也有很多因素是我无法控制的'";
            default:
                return "尝试用更平衡、客观的方式重新表述这个想法。";
        }
    }
    
    public List<JournalEntry> getUserJournalEntries(String userId) {
        return journalEntryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Map<String, Object> getPatternTimeline(String userId) {
        List<JournalEntry> entries = journalEntryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        Map<String, Integer> patternCounts = new HashMap<>();
        List<Map<String, Object>> timeline = new ArrayList<>();
        
        for (JournalEntry entry : entries) {
            if (entry.getCognitivePatternsJson() != null && !entry.getCognitivePatternsJson().isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> patterns = gson.fromJson(
                    entry.getCognitivePatternsJson(), 
                    List.class
                );
                
                Map<String, Object> timePoint = new HashMap<>();
                timePoint.put("timestamp", entry.getCreatedAt().toString());
                timePoint.put("patternCount", patterns.size());
                timePoint.put("patterns", patterns);
                timeline.add(timePoint);
                
                for (Map<String, Object> pattern : patterns) {
                    String type = (String) pattern.get("type");
                    patternCounts.put(type, patternCounts.getOrDefault(type, 0) + 1);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("timeline", timeline);
        result.put("patternSummary", patternCounts);
        result.put("totalEntries", entries.size());
        
        return result;
    }
}
