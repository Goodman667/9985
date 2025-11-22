package com.example.phq9assessment;

import com.example.phq9assessment.service.OpenSmileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "ai.opensmile.enabled=true",
    "ai.voice.enabled=true"
})
class OpenSmileIntegrationTest {

    @Autowired(required = false)
    private OpenSmileService openSmileService;

    @Test
    void testOpenSmileService_Autowired() {
        // Test that OpenSMILE service is properly autowired when enabled
        assertNotNull(openSmileService, "OpenSMILE service should be autowired when enabled");
    }

    @Test
    void testOpenSmileService_Configuration() {
        if (openSmileService != null) {
            // Test configuration method doesn't throw exceptions
            assertDoesNotThrow(() -> {
                String configInfo = openSmileService.testConfiguration();
                assertNotNull(configInfo);
                assertFalse(configInfo.isEmpty());
            });
        }
    }

    @Test
    void testOpenSmileService_Availability() {
        if (openSmileService != null) {
            // Test availability check doesn't throw exceptions
            assertDoesNotThrow(() -> {
                boolean available = openSmileService.isAvailable();
                // We don't assert true here since OpenSMILE might not be installed in test environment
                // We just verify the method doesn't crash
            });
        }
    }

    @Test
    void testOpenSmileService_ResultClass() {
        // Test that the OpenSmileResult class has all required fields
        OpenSmileService.OpenSmileResult result = new OpenSmileService.OpenSmileResult();
        
        // Test basic fields
        result.setSuccess(true);
        assertTrue(result.isSuccess());
        
        result.setDepressionScore(0.5);
        assertEquals(0.5, result.getDepressionScore());
        
        result.setDepressionLevel("test");
        assertEquals("test", result.getDepressionLevel());
        
        result.setConfigType("eGeMAPSv02");
        assertEquals("eGeMAPSv02", result.getConfigType());
        
        result.setFeatureCount(88);
        assertEquals(88, result.getFeatureCount());
        
        // Test new enhanced fields
        java.util.Map<String, Double> acousticSummary = new java.util.HashMap<>();
        acousticSummary.put("基频均值", 50.0);
        result.setAcousticSummary(acousticSummary);
        assertNotNull(result.getAcousticSummary());
        assertEquals(50.0, result.getAcousticSummary().get("基频均值"));
        
        java.util.Map<String, Double> emotionalIndicators = new java.util.HashMap<>();
        emotionalIndicators.put("活跃度", 0.6);
        result.setEmotionalIndicators(emotionalIndicators);
        assertNotNull(result.getEmotionalIndicators());
        assertEquals(0.6, result.getEmotionalIndicators().get("活跃度"));
        
        java.util.Map<String, Object> audioStats = new java.util.HashMap<>();
        audioStats.put("特征总数", 88);
        result.setAudioStats(audioStats);
        assertNotNull(result.getAudioStats());
        assertEquals(88, result.getAudioStats().get("特征总数"));
        
        java.util.Map<String, Double> topFeatures = new java.util.HashMap<>();
        topFeatures.put("test_feature", 1.0);
        result.setTopFeatures(topFeatures);
        assertNotNull(result.getTopFeatures());
        assertEquals(1.0, result.getTopFeatures().get("test_feature"));
    }
}