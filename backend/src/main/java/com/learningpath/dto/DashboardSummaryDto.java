package com.learningpath.dto;

import java.util.List;

public class DashboardSummaryDto {
    private String fullName;
    private String targetRole;
    private String careerGoal;
    private Integer streakDays;
    private Double totalHoursSpent;
    private Integer weeklyHoursTarget;
    private Double currentPhaseProgress;
    private String currentPhaseTitle;
    private String currentMilestone;
    private Double overallRoadmapProgress;
    private Integer skillsMasteredCount;
    private Integer skillsInProgressCount;
    private Integer skillGapsCount;
    private LearningPathItemDto nextRecommendedAction;
    private List<SkillGapDto> topSkillGaps;
    private List<RecommendationDto> topRecommendations;

    public DashboardSummaryDto() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { this.careerGoal = careerGoal; }

    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }

    public Double getTotalHoursSpent() { return totalHoursSpent; }
    public void setTotalHoursSpent(Double totalHoursSpent) { this.totalHoursSpent = totalHoursSpent; }

    public Integer getWeeklyHoursTarget() { return weeklyHoursTarget; }
    public void setWeeklyHoursTarget(Integer weeklyHoursTarget) { this.weeklyHoursTarget = weeklyHoursTarget; }

    public Double getCurrentPhaseProgress() { return currentPhaseProgress; }
    public void setCurrentPhaseProgress(Double currentPhaseProgress) { this.currentPhaseProgress = currentPhaseProgress; }

    public String getCurrentPhaseTitle() { return currentPhaseTitle; }
    public void setCurrentPhaseTitle(String currentPhaseTitle) { this.currentPhaseTitle = currentPhaseTitle; }

    public String getCurrentMilestone() { return currentMilestone; }
    public void setCurrentMilestone(String currentMilestone) { this.currentMilestone = currentMilestone; }

    public Double getOverallRoadmapProgress() { return overallRoadmapProgress; }
    public void setOverallRoadmapProgress(Double overallRoadmapProgress) { this.overallRoadmapProgress = overallRoadmapProgress; }

    public Integer getSkillsMasteredCount() { return skillsMasteredCount; }
    public void setSkillsMasteredCount(Integer skillsMasteredCount) { this.skillsMasteredCount = skillsMasteredCount; }

    public Integer getSkillsInProgressCount() { return skillsInProgressCount; }
    public void setSkillsInProgressCount(Integer skillsInProgressCount) { this.skillsInProgressCount = skillsInProgressCount; }

    public Integer getSkillGapsCount() { return skillGapsCount; }
    public void setSkillGapsCount(Integer skillGapsCount) { this.skillGapsCount = skillGapsCount; }

    public LearningPathItemDto getNextRecommendedAction() { return nextRecommendedAction; }
    public void setNextRecommendedAction(LearningPathItemDto nextRecommendedAction) { this.nextRecommendedAction = nextRecommendedAction; }

    public List<SkillGapDto> getTopSkillGaps() { return topSkillGaps; }
    public void setTopSkillGaps(List<SkillGapDto> topSkillGaps) { this.topSkillGaps = topSkillGaps; }

    public List<RecommendationDto> getTopRecommendations() { return topRecommendations; }
    public void setTopRecommendations(List<RecommendationDto> topRecommendations) { this.topRecommendations = topRecommendations; }
}
