package com.example.phq9assessment.repository;

import com.example.phq9assessment.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    
    Optional<Questionnaire> findByCode(String code);
    
    List<Questionnaire> findByIsActiveTrueOrderByName();
    
    List<Questionnaire> findByCategory(String category);
    
    @Query("SELECT q FROM Questionnaire q WHERE q.isActive = true " +
           "AND (LOWER(q.name) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(q.description) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(q.code) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    List<Questionnaire> searchQuestionnaires(@Param("keyword") String keyword);
    
    List<Questionnaire> findByIsActiveTrue();
}
