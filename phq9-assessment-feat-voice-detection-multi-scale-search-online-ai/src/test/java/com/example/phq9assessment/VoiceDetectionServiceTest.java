package com.example.phq9assessment;

import com.example.phq9assessment.service.OpenSmileService;
import com.example.phq9assessment.service.VoiceDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoiceDetectionServiceTest {

    @Mock
    private OpenSmileService openSmileService;

    @InjectMocks
    private VoiceDetectionService voiceDetectionService;

    @BeforeEach
    void setUp() {
        // Enable voice detection for testing
        ReflectionTestUtils.setField(voiceDetectionService, "voiceEnabled", true);
    }

    @Test
    void testAnalyzeVoiceFeatures_WithOpenSmile_Success() {
        // Arrange
        String audioBase64 = "dGVzdCBhdWRpbyBkYXRh"; // base64 encoded test data
        
        // Mock OpenSMILE result
        OpenSmileService.OpenSmileResult mockOpenSmileResult = new OpenSmileService.OpenSmileResult();
        mockOpenSmileResult.setSuccess(true);
        mockOpenSmileResult.setDepressionScore(0.3);
        mockOpenSmileResult.setDepressionLevel("轻度风险");
        mockOpenSmileResult.setConfigType("eGeMAPSv02");
        mockOpenSmileResult.setFeatureCount(88);
        
        // Mock features
        Map<String, Double> features = new HashMap<>();
        features.put("F0semitoneFrom27.5Hz_sma3nz_amean", 50.0);
        features.put("loudness_sma3_amean", -20.0);
        features.put("jitterLocal_sma3nz_amean", 0.01);
        features.put("shimmerLocaldB_sma3nz_amean", 0.5);
        features.put("HNRdBACF_sma3nz_amean", 10.0);
        mockOpenSmileResult.setFeatures(features);
        
        // Mock enhanced analytics
        Map<String, Double> acousticSummary = new HashMap<>();
        acousticSummary.put("基频均值", 50.0);
        acousticSummary.put("响度均值", -20.0);
        mockOpenSmileResult.setAcousticSummary(acousticSummary);
        
        Map<String, Double> emotionalIndicators = new HashMap<>();
        emotionalIndicators.put("活跃度", 0.6);
        emotionalIndicators.put("紧张度", 0.3);
        mockOpenSmileResult.setEmotionalIndicators(emotionalIndicators);
        
        Map<String, Object> audioStats = new HashMap<>();
        audioStats.put("特征总数", 88);
        audioStats.put("配置类型", "eGeMAPSv02");
        mockOpenSmileResult.setAudioStats(audioStats);
        
        Map<String, Double> topFeatures = new HashMap<>();
        topFeatures.put("F0semitoneFrom27.5Hz_sma3nz_amean", 50.0);
        topFeatures.put("loudness_sma3_amean", -20.0);
        mockOpenSmileResult.setTopFeatures(topFeatures);

        when(openSmileService.isAvailable()).thenReturn(true);
        when(openSmileService.extractFeatures(any(String.class))).thenReturn(mockOpenSmileResult);

        // Act
        VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures(audioBase64);

        // Assert
        assertNotNull(result);
        assertTrue(result.isUsingOpenSmile());
        assertEquals(0.3, result.getEmotionScore());
        assertEquals("轻度风险", result.getEmotionCategory());
        assertEquals(0.95, result.getConfidence());
        assertEquals("eGeMAPSv02", result.getOpenSmileConfigType());
        assertEquals(88, result.getFeatureCount());
        
        // Verify enhanced fields are populated
        assertNotNull(result.getAcousticSummary());
        assertFalse(result.getAcousticSummary().isEmpty());
        assertEquals(50.0, result.getAcousticSummary().get("基频均值"));
        
        assertNotNull(result.getEmotionalIndicators());
        assertFalse(result.getEmotionalIndicators().isEmpty());
        assertEquals(0.6, result.getEmotionalIndicators().get("活跃度"));
        
        assertNotNull(result.getAudioStats());
        assertFalse(result.getAudioStats().isEmpty());
        assertEquals(88, result.getAudioStats().get("特征总数"));
        
        assertNotNull(result.getTopFeatures());
        assertFalse(result.getTopFeatures().isEmpty());
        assertEquals(50.0, result.getTopFeatures().get("F0semitoneFrom27.5Hz_sma3nz_amean"));

        // Verify OpenSMILE service was called
        verify(openSmileService).isAvailable();
        verify(openSmileService).extractFeatures(audioBase64);
    }

    @Test
    void testAnalyzeVoiceFeatures_WithOpenSmile_Failure() {
        // Arrange
        String audioBase64 = "dGVzdCBhdWRpbyBkYXRh";
        
        OpenSmileService.OpenSmileResult mockOpenSmileResult = new OpenSmileService.OpenSmileResult();
        mockOpenSmileResult.setSuccess(false);
        mockOpenSmileResult.setErrorMessage("OpenSMILE执行失败");

        when(openSmileService.isAvailable()).thenReturn(true);
        when(openSmileService.extractFeatures(any(String.class))).thenReturn(mockOpenSmileResult);

        // Act
        VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures(audioBase64);

        // Assert
        assertNotNull(result);
        assertFalse(result.isUsingOpenSmile());
        assertEquals(0.70, result.getConfidence()); // Fallback confidence
        assertNotNull(result.getAcousticSummary());
        assertNotNull(result.getEmotionalIndicators());
        assertNotNull(result.getAudioStats());
        assertNotNull(result.getTopFeatures());

        // Verify OpenSMILE service was called
        verify(openSmileService).isAvailable();
        verify(openSmileService).extractFeatures(audioBase64);
    }

    @Test
    void testAnalyzeVoiceFeatures_OpenSmileUnavailable() {
        // Arrange
        String audioBase64 = "dGVzdCBhdWRpbyBkYXRh";

        when(openSmileService.isAvailable()).thenReturn(false);

        // Act
        VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures(audioBase64);

        // Assert
        assertNotNull(result);
        assertFalse(result.isUsingOpenSmile());
        assertEquals(0.70, result.getConfidence()); // Fallback confidence
        assertNotNull(result.getAcousticSummary());
        assertNotNull(result.getEmotionalIndicators());
        assertNotNull(result.getAudioStats());
        assertNotNull(result.getTopFeatures());
        
        // Verify fallback data is generated
        assertFalse(result.getAcousticSummary().isEmpty());
        assertFalse(result.getEmotionalIndicators().isEmpty());
        assertFalse(result.getAudioStats().isEmpty());

        // Verify OpenSMILE service availability was checked
        verify(openSmileService).isAvailable();
        verify(openSmileService, never()).extractFeatures(any(String.class));
    }

    @Test
    void testAnalyzeVoiceFeatures_VoiceDisabled() {
        // Arrange
        ReflectionTestUtils.setField(voiceDetectionService, "voiceEnabled", false);
        String audioBase64 = "dGVzdCBhdWRpbyBkYXRh";

        // Act
        VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures(audioBase64);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getEmotionScore());
        assertEquals("neutral", result.getEmotionCategory());
        assertEquals(0.0, result.getConfidence());
        assertFalse(result.isUsingOpenSmile());
        
        // Verify OpenSMILE service was not called
        verify(openSmileService, never()).isAvailable();
        verify(openSmileService, never()).extractFeatures(any(String.class));
    }

    @Test
    void testAnalyzeVoiceFeatures_NullAudio() {
        // Act
        VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures(null);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getEmotionScore());
        assertEquals("neutral", result.getEmotionCategory());
        assertEquals(0.0, result.getConfidence());
        assertFalse(result.isUsingOpenSmile());
        
        // Verify OpenSMILE service was not called
        verify(openSmileService, never()).isAvailable();
        verify(openSmileService, never()).extractFeatures(any(String.class));
    }

    @Test
    void testAnalyzeVoiceFeatures_EmptyAudio() {
        // Act
        VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures("");

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getEmotionScore());
        assertEquals("neutral", result.getEmotionCategory());
        assertEquals(0.0, result.getConfidence());
        assertFalse(result.isUsingOpenSmile());
        
        // Verify OpenSMILE service was not called
        verify(openSmileService, never()).isAvailable();
        verify(openSmileService, never()).extractFeatures(any(String.class));
    }

    @Test
    void testAnalyzeVoiceFeatures_OpenSmileException() {
        // Arrange
        String audioBase64 = "dGVzdCBhdWRpbyBkYXRh";

        when(openSmileService.isAvailable()).thenReturn(true);
        when(openSmileService.extractFeatures(any(String.class)))
            .thenThrow(new RuntimeException("OpenSMILE processing error"));

        // Act
        VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures(audioBase64);

        // Assert
        assertNotNull(result);
        assertFalse(result.isUsingOpenSmile());
        assertEquals(0.70, result.getConfidence()); // Fallback confidence
        assertNotNull(result.getAcousticSummary());
        assertNotNull(result.getEmotionalIndicators());
        assertNotNull(result.getAudioStats());
        assertNotNull(result.getTopFeatures());

        // Verify OpenSMILE service was called
        verify(openSmileService).isAvailable();
        verify(openSmileService).extractFeatures(audioBase64);
    }
}