package com.learningpath;

import com.learningpath.dto.SkillGapDto;
import com.learningpath.service.SkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class SkillServiceTest {

    @Autowired
    private SkillService skillService;

    @Test
    public void testGetRoleSkillRequirements_BackendJava() {
        Map<String, Integer> reqs = skillService.getRoleSkillRequirements("Backend Java Developer");
        assertNotNull(reqs);
        assertTrue(reqs.containsKey("Java"));
        assertTrue(reqs.containsKey("Spring Boot"));
        assertTrue(reqs.containsKey("SQL & Relational Databases"));
        assertEquals(85, reqs.get("Java"));
        assertEquals(80, reqs.get("Spring Boot"));
    }

    @Test
    @WithMockUser(username = "demo_java")
    public void testCalculateSkillGaps_DemoUser() {
        List<SkillGapDto> gaps = skillService.calculateSkillGaps();
        assertNotNull(gaps);
        assertFalse(gaps.isEmpty());

        // Spring Boot should be identified as a high gap
        boolean foundSpringBoot = false;
        for (SkillGapDto gap : gaps) {
            if (gap.getSkillName().equalsIgnoreCase("Spring Boot")) {
                foundSpringBoot = true;
                assertTrue(gap.getGap() > 0, "Spring Boot should have a positive gap");
            }
        }
        assertTrue(foundSpringBoot, "Spring Boot gap should be calculated");
    }
}
