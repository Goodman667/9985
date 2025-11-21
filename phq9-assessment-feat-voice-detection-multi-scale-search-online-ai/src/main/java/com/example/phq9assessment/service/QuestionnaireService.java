package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.Question;
import com.example.phq9assessment.entity.Questionnaire;
import com.example.phq9assessment.repository.QuestionRepository;
import com.example.phq9assessment.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionnaireService {
    
    @Autowired
    private QuestionnaireRepository questionnaireRepository;
    
    @Autowired
    private QuestionRepository questionRepository;

    public void initializeDefaultQuestionnaires() {
        if (questionnaireRepository.findByIsActiveTrue().isEmpty()) {
            List<Questionnaire> questionnaires = new ArrayList<>();
            questionnaires.add(new Questionnaire("PHQ-9", "患者健康问卷(PHQ-9)", 
                "用于筛查和测量抑郁症严重程度的9项问卷", "抑郁症", 9, 27));
            questionnaires.add(new Questionnaire("GAD-7", "广泛性焦虑症量表(GAD-7)", 
                "用于筛查和测量广泛性焦虑症的7项问卷", "焦虑症", 7, 21));
            questionnaires.add(new Questionnaire("PSQI", "匹兹堡睡眠质量指数", 
                "用于评估睡眠质量和睡眠障碍的19项问卷", "睡眠障碍", 19, 100));
            questionnaires.add(new Questionnaire("HAMA", "汉密尔顿焦虑量表", 
                "用于评估焦虑症患者的严重程度的14项问卷", "焦虑症", 14, 56));
            questionnaires.add(new Questionnaire("HAMD", "汉密尔顿抑郁量表", 
                "用于评估抑郁症患者的症状严重程度的17项问卷", "抑郁症", 17, 54));
            questionnaires.add(new Questionnaire("SAS", "自评焦虑量表(SAS)", 
                "用于筛查和评估焦虑水平的20项自评量表", "焦虑症", 20, 80));
            questionnaires.add(new Questionnaire("SDS", "自评抑郁量表(SDS)", 
                "用于筛查和评估抑郁水平的20项自评量表", "抑郁症", 20, 80));
            questionnaires.add(new Questionnaire("PCL-5", "创伤后应激障碍清单", 
                "用于评估创伤后应激障碍症状的20项问卷", "创伤应激", 20, 80));
            questionnaires.add(new Questionnaire("MoCA", "蒙特利尔认知评估", 
                "用于筛查轻度认知障碍的30项问卷", "认知功能", 30, 30));
            questionnaires.add(new Questionnaire("MOCA-BRIEF", "简短蒙特利尔认知评估", 
                "用于快速筛查认知障碍的10项简化版本", "认知功能", 10, 10));
            
            questionnaireRepository.saveAll(questionnaires);
            
            initializeQuestions();
        }
    }
    
    private void initializeQuestions() {
        if (questionRepository.count() == 0) {
            List<Question> questions = new ArrayList<>();

            questions.add(new Question("PHQ-9", 1, "做事时提不起劲或没有兴趣", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 2, "感到心情低落、沮丧或绝望", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 3, "入睡困难、睡不安稳或睡眠过多", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 4, "感到疲惫或没有活力", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 5, "食欲不振或暴饮暴食", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 6, "感觉自己很糟或觉得自己很失败，让自己或家人失望", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 7, "对事物专注有困难，例如看报纸或看电视", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 8, "行动或说话速度变得缓慢或相反地感觉坐立不安、烦躁", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("PHQ-9", 9, "有不如死掉或用某种方式伤害自己的念头", "完全没有", "有几天", "一半以上", "几乎每天", 3));

            questions.add(new Question("GAD-7", 1, "感到紧张、焦虑或快要崩溃", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("GAD-7", 2, "无法停止或控制担忧", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("GAD-7", 3, "对各种各样的事情担忧过多", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("GAD-7", 4, "很难放松下来", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("GAD-7", 5, "由于不安而无法静坐", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("GAD-7", 6, "变得容易烦恼或易怒", "完全没有", "有几天", "一半以上", "几乎每天", 3));
            questions.add(new Question("GAD-7", 7, "感到害怕，好像有什么可怕的事情会发生", "完全没有", "有几天", "一半以上", "几乎每天", 3));

            questionRepository.saveAll(questions);
        }
    }

    public List<Question> getQuestionsForQuestionnaire(String questionnaireCode) {
        List<Question> questions = questionRepository.findByQuestionnaireCodeOrderByQuestionNumberAsc(questionnaireCode);
        if (!questions.isEmpty()) {
            return questions;
        }
        return generatePlaceholderQuestions(questionnaireCode);
    }

    private List<Question> generatePlaceholderQuestions(String questionnaireCode) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findByCode(questionnaireCode);
        List<Question> placeholders = new ArrayList<>();
        if (questionnaireOpt.isPresent()) {
            Questionnaire questionnaire = questionnaireOpt.get();
            int total = questionnaire.getTotalQuestions() != null ? questionnaire.getTotalQuestions() : 0;
            for (int i = 1; i <= total; i++) {
                placeholders.add(new Question(
                        questionnaireCode,
                        i,
                        questionnaire.getName() + " - 第" + i + "题（预置问题，待自定义）",
                        "完全没有",
                        "轻微",
                        "中等",
                        "严重",
                        3
                ));
            }
        }
        return placeholders;
    }

    public List<Questionnaire> getAllActiveQuestionnaires() {
        return questionnaireRepository.findByIsActiveTrueOrderByName();
    }

    public List<Questionnaire> searchQuestionnaires(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveQuestionnaires();
        }
        return questionnaireRepository.searchQuestionnaires(keyword);
    }

    public Optional<Questionnaire> getQuestionnaireByCode(String code) {
        return questionnaireRepository.findByCode(code);
    }

    public List<Questionnaire> getQuestionnairesByCategory(String category) {
        return questionnaireRepository.findByCategory(category);
    }

    public Questionnaire saveQuestionnaire(Questionnaire questionnaire) {
        return questionnaireRepository.save(questionnaire);
    }

    public void deleteQuestionnaire(Long id) {
        questionnaireRepository.deleteById(id);
    }
}
