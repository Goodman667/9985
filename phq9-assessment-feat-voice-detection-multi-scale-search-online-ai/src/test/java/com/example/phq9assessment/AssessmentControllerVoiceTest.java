package com.example.phq9assessment;

import com.example.phq9assessment.entity.AssessmentRecord;
import com.example.phq9assessment.model.AssessmentResult;
import com.example.phq9assessment.repository.AssessmentRecordRepository;
import com.example.phq9assessment.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssessmentController.class)
@AutoConfigureWebMvc
class AssessmentControllerVoiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssessmentRecordRepository assessmentRecordRepository;

    @MockBean
    private SentimentAnalysisService sentimentAnalysisService;

    @MockBean
    private MachineLearningService machineLearningService;

    @MockBean
    private AnomalyDetectionService anomalyDetectionService;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private QuestionnaireService questionnaireService;

    @MockBean
    private VoiceDetectionService voiceDetectionService;

    @MockBean
    private OnlineAIService onlineAIService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Mock questionnaire service to return default questions
        when(questionnaireService.getAllActiveQuestionnaires()).thenReturn(new ArrayList<>());
        when(questionnaireService.getQuestionsForQuestionnaire(anyString())).thenReturn(new ArrayList<>());
    }

    @Test
    void testSubmitAssessment_WithVoiceAudio_CallsVoiceDetectionService() throws Exception {
        // Arrange
        String voiceAudioBase64 = "dGVzdCBhdWRpbyBkYXRhIGJhc2U2NA==";
        
        // Mock voice analysis result
        VoiceDetectionService.VoiceAnalysisResult mockVoiceResult = new VoiceDetectionService.VoiceAnalysisResult();
        mockVoiceResult.setEmotionScore(0.3);
        mockVoiceResult.setEmotionCategory("mild_negative");
        mockVoiceResult.setConfidence(0.95);
        mockVoiceResult.setUsingOpenSmile(true);
        mockVoiceResult.setOpenSmileConfigType("eGeMAPSv02");
        mockVoiceResult.setFeatureCount(88);
        
        // Mock enhanced analytics
        Map<String, Double> acousticSummary = new HashMap<>();
        acousticSummary.put("基频均值", 50.0);
        acousticSummary.put("响度均值", -20.0);
        mockVoiceResult.setAcousticSummary(acousticSummary);
        
        Map<String, Double> emotionalIndicators = new HashMap<>();
        emotionalIndicators.put("活跃度", 0.6);
        emotionalIndicators.put("紧张度", 0.3);
        mockVoiceResult.setEmotionalIndicators(emotionalIndicators);
        
        Map<String, Object> audioStats = new HashMap<>();
        audioStats.put("特征总数", 88);
        audioStats.put("配置类型", "eGeMAPSv02");
        mockVoiceResult.setAudioStats(audioStats);
        
        Map<String, Double> topFeatures = new HashMap<>();
        topFeatures.put("F0semitoneFrom27.5Hz_sma3nz_amean", 50.0);
        mockVoiceResult.setTopFeatures(topFeatures);

        when(voiceDetectionService.analyzeVoiceFeatures(anyString())).thenReturn(mockVoiceResult);
        when(sentimentAnalysisService.analyzeSentiment(anyString())).thenReturn(
            new SentimentAnalysisService.SentimentAnalysisResult());
        when(machineLearningService.calculateRiskScore(any(int[].class), anyString(), anyDouble()))
            .thenReturn(0.2);
        when(machineLearningService.clusterUser(any(int[].class), anyDouble()))
            .thenReturn(new MachineLearningService.ClusterResult());
        when(machineLearningService.analyzeTrend(any()))
            .thenReturn(new MachineLearningService.TrendAnalysis());
        when(anomalyDetectionService.detectAnomalies(any(int[].class)))
            .thenReturn(new AnomalyDetectionService.AnomalyDetectionResult());
        when(recommendationService.generateRecommendations(any(int[].class), anyInt(), anyString()))
            .thenReturn(new ArrayList<>());
        when(onlineAIService.enhanceSentimentAnalysis(anyString()))
            .thenReturn(new OnlineAIService.AIEnhancementResult());
        when(assessmentRecordRepository.save(any(AssessmentRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MvcResult result = mockMvc.perform(post("/submit")
                .param("voiceAudio", voiceAudioBase64)
                .param("questionnaireCode", "PHQ-9")
                .param("q1", "1")
                .param("q2", "1")
                .param("q3", "1")
                .param("q4", "1")
                .param("q5", "1")
                .param("q6", "1")
                .param("q7", "1")
                .param("q8", "1")
                .param("q9", "1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        // Assert
        // Verify voice detection service was called with the audio data
        verify(voiceDetectionService).analyzeVoiceFeatures(voiceAudioBase64);
        
        // Verify assessment record was saved with voice analysis data
        ArgumentCaptor<AssessmentRecord> recordCaptor = ArgumentCaptor.forClass(AssessmentRecord.class);
        verify(assessmentRecordRepository).save(recordCaptor.capture());
        
        AssessmentRecord savedRecord = recordCaptor.getValue();
        assertNotNull(savedRecord.getVoiceEmotionScore());
        assertEquals(0.3, savedRecord.getVoiceEmotionScore());
        assertNotNull(savedRecord.getVoiceFeatures());
        
        // Verify the voice features contain the enhanced analytics
        String voiceFeaturesJson = savedRecord.getVoiceFeatures();
        assertNotNull(voiceFeaturesJson);
        assertFalse(voiceFeaturesJson.isEmpty());
        
        // Parse the JSON to verify structure
        Map<String, Object> voiceFeaturesMap = objectMapper.readValue(voiceFeaturesJson, Map.class);
        assertNotNull(voiceFeaturesMap.get("acousticSummary"));
        assertNotNull(voiceFeaturesMap.get("emotionalIndicators"));
        assertNotNull(voiceFeaturesMap.get("audioStats"));
        assertNotNull(voiceFeaturesMap.get("topFeatures"));
        
        // Verify the result model contains voice analysis
        Object resultModel = result.getModelAndView().getModel().get("result");
        assertNotNull(resultModel);
        assertTrue(resultModel instanceof AssessmentResult);
        
        AssessmentResult assessmentResult = (AssessmentResult) resultModel;
        assertNotNull(assessmentResult.getVoiceAnalysis());
        assertTrue(assessmentResult.getVoiceAnalysis().isUsingOpenSmile());
        assertEquals(0.3, assessmentResult.getVoiceAnalysis().getEmotionScore());
        assertEquals("mild_negative", assessmentResult.getVoiceAnalysis().getEmotionCategory());
        assertEquals(0.95, assessmentResult.getVoiceAnalysis().getConfidence());
        assertEquals("eGeMAPSv02", assessmentResult.getVoiceAnalysis().getOpenSmileConfigType());
        assertEquals(88, assessmentResult.getVoiceAnalysis().getFeatureCount());
    }

    @Test
    void testSubmitAssessment_WithoutVoiceAudio_DoesNotCallVoiceDetectionService() throws Exception {
        // Act
        mockMvc.perform(post("/submit")
                .param("questionnaireCode", "PHQ-9")
                .param("q1", "1")
                .param("q2", "1")
                .param("q3", "1")
                .param("q4", "1")
                .param("q5", "1")
                .param("q6", "1")
                .param("q7", "1")
                .param("q8", "1")
                .param("q9", "1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Assert
        // Verify voice detection service was NOT called
        verify(voiceDetectionService, never()).analyzeVoiceFeatures(anyString());
        
        // Verify assessment record was saved without voice analysis data
        ArgumentCaptor<AssessmentRecord> recordCaptor = ArgumentCaptor.forClass(AssessmentRecord.class);
        verify(assessmentRecordRepository).save(recordCaptor.capture());
        
        AssessmentRecord savedRecord = recordCaptor.getValue();
        assertNull(savedRecord.getVoiceEmotionScore());
        assertNull(savedRecord.getVoiceFeatures());
    }

    @Test
    void testSubmitAssessment_WithEmptyVoiceAudio_DoesNotCallVoiceDetectionService() throws Exception {
        // Act
        mockMvc.perform(post("/submit")
                .param("voiceAudio", "")
                .param("questionnaireCode", "PHQ-9")
                .param("q1", "1")
                .param("q2", "1")
                .param("q3", "1")
                .param("q4", "1")
                .param("q5", "1")
                .param("q6", "1")
                .param("q7", "1")
                .param("q8", "1")
                .param("q9", "1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Assert
        // Verify voice detection service was NOT called
        verify(voiceDetectionService, never()).analyzeVoiceFeatures(anyString());
    }

    @Test
    void testSubmitAssessment_VoiceDetectionReturnsNull_HandlesGracefully() throws Exception {
        // Arrange
        String voiceAudioBase64 = "dGVzdCBhdWRpbyBkYXRhIGJhc2U2NA==";
        
        when(voiceDetectionService.analyzeVoiceFeatures(anyString())).thenReturn(null);
        when(sentimentAnalysisService.analyzeSentiment(anyString())).thenReturn(
            new SentimentAnalysisService.SentimentAnalysisResult());
        when(machineLearningService.calculateRiskScore(any(int[].class), anyString(), anyDouble()))
            .thenReturn(0.2);
        when(machineLearningService.clusterUser(any(int[].class), anyDouble()))
            .thenReturn(new MachineLearningService.ClusterResult());
        when(machineLearningService.analyzeTrend(any()))
            .thenReturn(new MachineLearningService.TrendAnalysis());
        when(anomalyDetectionService.detectAnomalies(any(int[].class)))
            .thenReturn(new AnomalyDetectionService.AnomalyDetectionResult());
        when(recommendationService.generateRecommendations(any(int[].class), anyInt(), anyString()))
            .thenReturn(new ArrayList<>());
        when(onlineAIService.enhanceSentimentAnalysis(anyString()))
            .thenReturn(new OnlineAIService.AIEnhancementResult());
        when(assessmentRecordRepository.save(any(AssessmentRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        mockMvc.perform(post("/submit")
                .param("voiceAudio", voiceAudioBase64)
                .param("questionnaireCode", "PHQ-9")
                .param("q1", "1")
                .param("q2", "1")
                .param("q3", "1")
                .param("q4", "1")
                .param("q5", "1")
                .param("q6", "1")
                .param("q7", "1")
                .param("q8", "1")
                .param("q9", "1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Verify voice detection service was called
        verify(voiceDetectionService).analyzeVoiceFeatures(voiceAudioBase64);
        
        // Verify assessment record was saved without voice analysis data (since result is null)
        ArgumentCaptor<AssessmentRecord> recordCaptor = ArgumentCaptor.forClass(AssessmentRecord.class);
        verify(assessmentRecordRepository).save(recordCaptor.capture());
        
        AssessmentRecord savedRecord = recordCaptor.getValue();
        assertNull(savedRecord.getVoiceEmotionScore());
        assertNull(savedRecord.getVoiceFeatures());
    }
}