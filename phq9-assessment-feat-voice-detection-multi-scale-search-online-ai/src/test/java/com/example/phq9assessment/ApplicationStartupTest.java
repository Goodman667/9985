package com.example.phq9assessment;

import com.example.phq9assessment.service.OpenSmileService;
import com.example.phq9assessment.service.VoiceDetectionService;
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
class ApplicationStartupTest {

    @Autowired(required = false)
    private OpenSmileService openSmileService;

    @Autowired
    private VoiceDetectionService voiceDetectionService;

    @Test
    void applicationContext_Loads() {
        // Test that the application context loads successfully with OpenSMILE enabled
        assertNotNull(voiceDetectionService, "VoiceDetectionService should be always available");
    }

    @Test
    void openSmileService_Configuration() {
        // Test that OpenSMILE service configuration doesn't cause startup issues
        if (openSmileService != null) {
            assertDoesNotThrow(() -> {
                // These should not throw exceptions during startup
                openSmileService.isAvailable();
                openSmileService.testConfiguration();
            });
        }
    }

    @Test
    void voiceDetectionService_WithNullAudio() {
        // Test that voice detection service handles null input gracefully
        assertDoesNotThrow(() -> {
            VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures(null);
            assertNotNull(result);
            assertEquals("neutral", result.getEmotionCategory());
            assertEquals(0.0, result.getEmotionScore());
            assertEquals(0.0, result.getConfidence());
            assertFalse(result.isUsingOpenSmile());
        });
    }

    @Test
    void voiceDetectionService_WithEmptyAudio() {
        // Test that voice detection service handles empty input gracefully
        assertDoesNotThrow(() -> {
            VoiceDetectionService.VoiceAnalysisResult result = voiceDetectionService.analyzeVoiceFeatures("");
            assertNotNull(result);
            assertEquals("neutral", result.getEmotionCategory());
            assertEquals(0.0, result.getEmotionScore());
            assertEquals(0.0, result.getConfidence());
            assertFalse(result.isUsingOpenSmile());
        });
    }
}