package com.example.phq9assessment.entity;

import javax.persistence.*;

@Entity
@Table(name = "questions")
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "questionnaire_code", nullable = false)
    private String questionnaireCode;
    
    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;
    
    @Column(name = "question_text", length = 1000, nullable = false)
    private String questionText;
    
    @Column(name = "option_0")
    private String option0;
    
    @Column(name = "option_1")
    private String option1;
    
    @Column(name = "option_2")
    private String option2;
    
    @Column(name = "option_3")
    private String option3;
    
    @Column(name = "max_points")
    private Integer maxPoints;

    public Question() {
    }

    public Question(String questionnaireCode, Integer questionNumber, String questionText,
                   String option0, String option1, String option2, String option3, Integer maxPoints) {
        this.questionnaireCode = questionnaireCode;
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.option0 = option0;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.maxPoints = maxPoints;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionnaireCode() {
        return questionnaireCode;
    }

    public void setQuestionnaireCode(String questionnaireCode) {
        this.questionnaireCode = questionnaireCode;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOption0() {
        return option0;
    }

    public void setOption0(String option0) {
        this.option0 = option0;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public Integer getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(Integer maxPoints) {
        this.maxPoints = maxPoints;
    }
}
