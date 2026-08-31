package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.*;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private LearningPathRepository pathRepository;

    @Autowired
    private SkillService skillService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private RoadmapService roadmapService;

    @Autowired
    private AuthService authService;

    public DashboardSummaryDto getDashboardSummary() {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        LearningPathDto roadmap = roadmapService.getActiveRoadmap();
        List<SkillGapDto> skillGaps = skillService.calculateSkillGaps();
        List<RecommendationDto> recommendations = recommendationService.getRecommendations();

        int mastered = 0;
        int inProgress = 0;
        int missing = 0;

        for (SkillGapDto gap : skillGaps) {
            if ("MASTERED".equalsIgnoreCase(gap.getStatus())) mastered++;
            else if ("IN_PROGRESS".equalsIgnoreCase(gap.getStatus())) inProgress++;
            else missing++;
        }

        // Find next action
        LearningPathItemDto nextAction = null;
        String currentPhaseTitle = "Phase 1: Getting Started";
        Double currentPhaseProgress = 0.0;

        if (roadmap != null && roadmap.getPhases() != null) {
            for (LearningPhaseDto phase : roadmap.getPhases()) {
                if (!"COMPLETED".equalsIgnoreCase(phase.getStatus())) {
                    currentPhaseTitle = phase.getTitle();
                    currentPhaseProgress = phase.getProgressPercentage();
                    if (phase.getItems() != null) {
                        for (LearningPathItemDto item : phase.getItems()) {
                            if (!"COMPLETED".equalsIgnoreCase(item.getStatus()) && !"LOCKED".equalsIgnoreCase(item.getStatus())) {
                                nextAction = item;
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setFullName(profile.getFullName());
        dto.setTargetRole(profile.getTargetRole());
        dto.setCareerGoal(profile.getCareerGoal());
        dto.setStreakDays(profile.getStreakDays() != null ? profile.getStreakDays() : 1);
        dto.setTotalHoursSpent(profile.getTotalHoursSpent() != null ? profile.getTotalHoursSpent() : 0.0);
        dto.setWeeklyHoursTarget(profile.getWeeklyHours() != null ? profile.getWeeklyHours() : 10);
        dto.setCurrentPhaseTitle(currentPhaseTitle);
        dto.setCurrentPhaseProgress(currentPhaseProgress);
        dto.setCurrentMilestone("Complete " + currentPhaseTitle);
        dto.setOverallRoadmapProgress(roadmap != null ? roadmap.getOverallProgressPercentage() : 0.0);
        dto.setSkillsMasteredCount(mastered);
        dto.setSkillsInProgressCount(inProgress);
        dto.setSkillGapsCount(missing);
        dto.setNextRecommendedAction(nextAction);
        dto.setTopSkillGaps(skillGaps.stream().limit(6).toList());
        dto.setTopRecommendations(recommendations.stream().limit(4).toList());

        return dto;
    }
}
