package com.learningpath;

import com.learningpath.dto.LearningPathDto;
import com.learningpath.dto.RecalculateTimeRequest;
import com.learningpath.service.RoadmapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class RoadmapServiceTest {

    @Autowired
    private RoadmapService roadmapService;

    @Test
    @WithMockUser(username = "demo_java")
    public void testGetActiveRoadmap() {
        LearningPathDto roadmap = roadmapService.getActiveRoadmap();
        assertNotNull(roadmap);
        assertEquals("Backend Java Developer", roadmap.getTargetRole());
        assertFalse(roadmap.getPhases().isEmpty());
        assertTrue(roadmap.getTotalEstimatedHours() > 0);
    }

    @Test
    @WithMockUser(username = "demo_java")
    public void testRecalculateRoadmapTimeline() {
        // Change from 10 hrs/wk to 5 hrs/wk
        RecalculateTimeRequest req = new RecalculateTimeRequest(5);
        LearningPathDto updated = roadmapService.recalculateRoadmapTimeline(req);

        assertNotNull(updated);
        assertTrue(updated.getEstimatedWeeks() > 10, "Estimated weeks should scale when hours/week decrease");
    }
}
