package com.learningpath;

import com.learningpath.dto.AssessmentDto;
import com.learningpath.dto.AssessmentResultDto;
import com.learningpath.dto.AssessmentSubmissionRequest;
import com.learningpath.service.AssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class AssessmentServiceTest {

    @Autowired
    private AssessmentService assessmentService;

    @Test
    public void testGetAssessmentById() {
        AssessmentDto dto = assessmentService.getAssessmentById(1L);
        assertNotNull(dto);
        assertNotNull(dto.getTitle());
        assertFalse(dto.getQuestions().isEmpty());
    }

    @Test
    @WithMockUser(username = "demo_java")
    public void testSubmitAssessment_HighScoreAdaptiveTrigger() {
        AssessmentDto dto = assessmentService.getAssessmentById(1L);
        assertNotNull(dto);

        // Prepare 100% correct answers (for test questions, index 1, 0, 2)
        Map<Long, Integer> answers = new HashMap<>();
        if (dto.getQuestions().size() >= 3) {
            answers.put(dto.getQuestions().get(0).getId(), 1);
            answers.put(dto.getQuestions().get(1).getId(), 0);
            answers.put(dto.getQuestions().get(2).getId(), 2);
        }

        AssessmentSubmissionRequest request = new AssessmentSubmissionRequest(answers);
        AssessmentResultDto result = assessmentService.submitAssessment(1L, request);

        assertNotNull(result);
        assertTrue(result.getScorePercentage() >= 70, "Should pass assessment with correct answers");
        assertTrue(result.getPassed());
        assertNotNull(result.getAdaptiveActionTaken());
    }
}
