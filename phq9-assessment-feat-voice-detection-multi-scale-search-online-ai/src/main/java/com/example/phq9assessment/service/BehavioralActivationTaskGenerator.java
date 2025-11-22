package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.entity.CompletedBehavioralTask;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import com.example.phq9assessment.repository.CompletedBehavioralTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BehavioralActivationTaskGenerator {
    
    @Autowired
    private AssessmentRecordRepository assessmentRecordRepository;
    
    @Autowired
    private CompletedBehavioralTaskRepository taskRepository;
    
    private static final Map<String, List<Map<String, String>>> TASK_LIBRARY = new HashMap<>();
    
    static {
        List<Map<String, String>> easyTasks = new ArrayList<>();
        easyTasks.add(createTask("喝一杯水", "起床后或现在喝一杯温水，滋润身体", "自我照顾"));
        easyTasks.add(createTask("深呼吸5次", "进行5次深呼吸，每次吸气4秒，呼气6秒", "放松练习"));
        easyTasks.add(createTask("整理床铺", "花2分钟整理床铺，创造整洁的空间", "日常活动"));
        easyTasks.add(createTask("听一首喜欢的歌", "选择一首让你感到平静或愉快的歌曲", "愉快活动"));
        easyTasks.add(createTask("向窗外看5分钟", "观察窗外的景色，注意天气、颜色和动态", "正念练习"));
        easyTasks.add(createTask("给朋友发一条消息", "向一位朋友发送简单的问候消息", "社交活动"));
        easyTasks.add(createTask("洗脸刷牙", "完成基本的个人卫生护理", "自我照顾"));
        easyTasks.add(createTask("伸展身体5分钟", "进行简单的伸展运动，活动筋骨", "身体活动"));
        
        List<Map<String, String>> mediumTasks = new ArrayList<>();
        mediumTasks.add(createTask("散步15分钟", "到户外或室内走动15分钟，保持舒适节奏", "身体活动"));
        mediumTasks.add(createTask("准备一顿简单的餐食", "为自己准备一份营养的简单餐食", "自我照顾"));
        mediumTasks.add(createTask("整理一个小区域", "整理书桌、床头柜或一个抽屉", "日常活动"));
        mediumTasks.add(createTask("阅读15分钟", "阅读一本书、杂志或文章15分钟", "愉快活动"));
        mediumTasks.add(createTask("进行10分钟冥想", "使用冥想app或自主进行10分钟冥想", "放松练习"));
        mediumTasks.add(createTask("给家人打电话", "与家人通话10-15分钟，分享近况", "社交活动"));
        mediumTasks.add(createTask("写日记", "写下今天的感受和想法，不少于5分钟", "反思活动"));
        mediumTasks.add(createTask("做一项家务", "洗碗、吸尘或整理衣物", "日常活动"));
        
        List<Map<String, String>> hardTasks = new ArrayList<>();
        hardTasks.add(createTask("锻炼30分钟", "进行30分钟的有氧运动，如跑步、游泳或骑车", "身体活动"));
        hardTasks.add(createTask("参加社交活动", "参加朋友聚会、兴趣小组或社区活动", "社交活动"));
        hardTasks.add(createTask("完成一项推迟的任务", "处理一直拖延的工作或个人事务", "目标导向"));
        hardTasks.add(createTask("学习新技能", "开始学习一项新技能或爱好，至少投入1小时", "愉快活动"));
        hardTasks.add(createTask("深度清洁一个房间", "彻底清洁和整理一个房间", "日常活动"));
        hardTasks.add(createTask("志愿服务", "参与志愿服务活动，帮助他人", "有意义活动"));
        hardTasks.add(createTask("外出探索", "到新的地方探索，如公园、博物馆或咖啡馆", "愉快活动"));
        hardTasks.add(createTask("参加团体课程", "参加瑜伽、舞蹈或其他团体课程", "身体活动"));
        
        TASK_LIBRARY.put("EASY", easyTasks);
        TASK_LIBRARY.put("MEDIUM", mediumTasks);
        TASK_LIBRARY.put("HARD", hardTasks);
    }
    
    private static Map<String, String> createTask(String name, String description, String category) {
        Map<String, String> task = new HashMap<>();
        task.put("name", name);
        task.put("description", description);
        task.put("category", category);
        return task;
    }
    
    public List<Map<String, Object>> generatePersonalizedTasks(String userId, int count) {
        List<AssessmentRecord> records = assessmentRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        String difficultyLevel = determineDifficultyLevel(records);
        
        List<CompletedBehavioralTask> completedTasks = taskRepository.findByUserIdOrderByAssignedAtDesc(userId);
        Map<String, Double> taskEffectiveness = calculateTaskEffectiveness(completedTasks);
        
        List<Map<String, String>> availableTasks = TASK_LIBRARY.get(difficultyLevel);
        if (availableTasks == null) {
            availableTasks = TASK_LIBRARY.get("EASY");
        }
        
        Set<String> recentTaskNames = completedTasks.stream()
            .limit(5)
            .map(CompletedBehavioralTask::getTaskName)
            .collect(Collectors.toSet());
        
        List<Map<String, String>> candidateTasks = availableTasks.stream()
            .filter(t -> !recentTaskNames.contains(t.get("name")))
            .collect(Collectors.toList());
        
        if (candidateTasks.isEmpty()) {
            candidateTasks = availableTasks;
        }
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        Collections.shuffle(candidateTasks, new Random());
        
        for (int i = 0; i < Math.min(count, candidateTasks.size()); i++) {
            Map<String, String> task = candidateTasks.get(i);
            
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("name", task.get("name"));
            recommendation.put("description", task.get("description"));
            recommendation.put("category", task.get("category"));
            recommendation.put("difficultyLevel", difficultyLevel);
            recommendation.put("estimatedDuration", estimateDuration(difficultyLevel));
            
            Double effectiveness = taskEffectiveness.get(task.get("name"));
            if (effectiveness != null) {
                recommendation.put("previousEffectiveness", Math.round(effectiveness * 10) / 10.0);
            }
            
            recommendations.add(recommendation);
        }
        
        return recommendations;
    }
    
    private String determineDifficultyLevel(List<AssessmentRecord> records) {
        if (records.isEmpty()) {
            return "EASY";
        }
        
        AssessmentRecord latest = records.get(records.size() - 1);
        int score = latest.getTotalScore();
        
        if (score >= 15) {
            return "EASY";
        } else if (score >= 8) {
            return "MEDIUM";
        } else {
            List<CompletedBehavioralTask> recentCompleted = 
                taskRepository.findByUserIdAndCompletedOrderByAssignedAtDesc(latest.getUserId(), true);
            
            if (recentCompleted.size() >= 3) {
                return "HARD";
            }
            return "MEDIUM";
        }
    }
    
    private Map<String, Double> calculateTaskEffectiveness(List<CompletedBehavioralTask> completedTasks) {
        Map<String, Double> effectiveness = new HashMap<>();
        
        for (CompletedBehavioralTask task : completedTasks) {
            if (task.getCompleted() && task.getMoodBefore() != null && task.getMoodAfter() != null) {
                double improvement = task.getMoodAfter() - task.getMoodBefore();
                effectiveness.put(task.getTaskName(), improvement);
            }
        }
        
        return effectiveness;
    }
    
    private String estimateDuration(String difficultyLevel) {
        switch (difficultyLevel) {
            case "EASY":
                return "2-5分钟";
            case "MEDIUM":
                return "10-20分钟";
            case "HARD":
                return "30-60分钟";
            default:
                return "5-10分钟";
        }
    }
    
    public CompletedBehavioralTask assignTask(String userId, String taskName, String taskDescription, 
                                             String difficultyLevel, String category, Double moodBefore) {
        CompletedBehavioralTask task = new CompletedBehavioralTask();
        task.setUserId(userId);
        task.setTaskName(taskName);
        task.setTaskDescription(taskDescription);
        task.setDifficultyLevel(difficultyLevel);
        task.setCategory(category);
        task.setMoodBefore(moodBefore);
        
        return taskRepository.save(task);
    }
    
    public CompletedBehavioralTask completeTask(Long taskId, Integer rating, String feedback, 
                                               Double moodAfter) {
        CompletedBehavioralTask task = taskRepository.findById(taskId).orElse(null);
        
        if (task != null) {
            task.setCompleted(true);
            task.setCompletionRating(rating);
            task.setFeedback(feedback);
            task.setMoodAfter(moodAfter);
            task.setCompletedAt(LocalDateTime.now());
            
            return taskRepository.save(task);
        }
        
        return null;
    }
    
    public Map<String, Object> getTaskHistory(String userId) {
        List<CompletedBehavioralTask> tasks = taskRepository.findByUserIdOrderByAssignedAtDesc(userId);
        
        Map<String, Object> history = new HashMap<>();
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(CompletedBehavioralTask::getCompleted).count();
        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;
        
        history.put("totalTasks", totalTasks);
        history.put("completedTasks", completedTasks);
        history.put("completionRate", Math.round(completionRate * 10) / 10.0);
        
        double avgRating = tasks.stream()
            .filter(t -> t.getCompleted() && t.getCompletionRating() != null)
            .mapToInt(CompletedBehavioralTask::getCompletionRating)
            .average()
            .orElse(0);
        
        history.put("averageRating", Math.round(avgRating * 10) / 10.0);
        
        double avgMoodImprovement = tasks.stream()
            .filter(t -> t.getCompleted() && t.getMoodBefore() != null && t.getMoodAfter() != null)
            .mapToDouble(t -> t.getMoodAfter() - t.getMoodBefore())
            .average()
            .orElse(0);
        
        history.put("averageMoodImprovement", Math.round(avgMoodImprovement * 10) / 10.0);
        
        Map<String, Long> categoryBreakdown = tasks.stream()
            .collect(Collectors.groupingBy(
                CompletedBehavioralTask::getCategory, 
                Collectors.counting()
            ));
        
        history.put("categoryBreakdown", categoryBreakdown);
        
        List<Map<String, Object>> recentTasks = tasks.stream()
            .limit(10)
            .map(t -> {
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("id", t.getId());
                taskData.put("name", t.getTaskName());
                taskData.put("category", t.getCategory());
                taskData.put("completed", t.getCompleted());
                taskData.put("assignedAt", t.getAssignedAt().toString());
                if (t.getCompleted()) {
                    taskData.put("completedAt", t.getCompletedAt().toString());
                    taskData.put("rating", t.getCompletionRating());
                }
                return taskData;
            })
            .collect(Collectors.toList());
        
        history.put("recentTasks", recentTasks);
        
        return history;
    }
    
    public List<Map<String, Object>> getTopPerformingTasks(String userId, int limit) {
        List<CompletedBehavioralTask> tasks = taskRepository.findByUserIdAndCompletedOrderByAssignedAtDesc(userId, true);
        
        return tasks.stream()
            .filter(t -> t.getMoodBefore() != null && t.getMoodAfter() != null)
            .sorted((t1, t2) -> {
                double improvement1 = t1.getMoodAfter() - t1.getMoodBefore();
                double improvement2 = t2.getMoodAfter() - t2.getMoodBefore();
                return Double.compare(improvement2, improvement1);
            })
            .limit(limit)
            .map(t -> {
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("name", t.getTaskName());
                taskData.put("category", t.getCategory());
                taskData.put("moodImprovement", Math.round((t.getMoodAfter() - t.getMoodBefore()) * 10) / 10.0);
                taskData.put("rating", t.getCompletionRating());
                return taskData;
            })
            .collect(Collectors.toList());
    }
}
