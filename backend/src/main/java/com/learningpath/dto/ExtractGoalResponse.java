package com.learningpath.dto;

import java.util.List;

public class ExtractGoalResponse {
    private String targetRole;
    private String careerGoal;
    private String experienceLevel;
    private Integer estimatedMonths;
    private List<String> extractedSkills;
    private List<String> missingSkills;
    private String learningPace;
    private String aiSummary;

    public ExtractGoalResponse() {}

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { this.careerGoal = careerGoal; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public Integer getEstimatedMonths() { return estimatedMonths; }
    public void setEstimatedMonths(Integer estimatedMonths) { this.estimatedMonths = estimatedMonths; }

    public List<String> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<String> extractedSkills) { this.extractedSkills = extractedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public String getLearningPace() { return learningPace; }
    public void setLearningPace(String learningPace) { this.learningPace = learningPace; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}
