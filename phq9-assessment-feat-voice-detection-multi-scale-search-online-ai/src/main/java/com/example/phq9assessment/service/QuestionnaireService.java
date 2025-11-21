package com.example.phq9assessment.service;

import com.example.phq9assessment.entity.Questionnaire;
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
        }
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
