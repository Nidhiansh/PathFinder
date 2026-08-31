package com.learningpath.dto;

import java.util.ArrayList;
import java.util.List;

public class ExtractGoalResponse {
    private String targetRole;
    private String careerGoal;
    private String rawGoal;
    private String normalizedGoal;
    private String goalType = "TOPIC_LEARNING"; // TOPIC_LEARNING, CAREER_GOAL, PROJECT_GOAL, CERTIFICATION_GOAL
    private String experienceLevel = "INTERMEDIATE";
    private Integer estimatedMonths = 6;
    private List<String> extractedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> coreSkills = new ArrayList<>();
    private List<String> prerequisiteSkills = new ArrayList<>();
    private List<String> optionalSkills = new ArrayList<>();
    private List<String> excludedSkills = new ArrayList<>();
    private Double confidence = 0.90;
    private String learningPace = "10 hours/week (Hands-On)";
    private String aiSummary;

    public ExtractGoalResponse() {}

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { 
        this.careerGoal = careerGoal; 
        if (this.rawGoal == null) this.rawGoal = careerGoal;
    }

    public String getRawGoal() { return rawGoal; }
    public void setRawGoal(String rawGoal) { this.rawGoal = rawGoal; }

    public String getNormalizedGoal() { return normalizedGoal; }
    public void setNormalizedGoal(String normalizedGoal) { this.normalizedGoal = normalizedGoal; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public Integer getEstimatedMonths() { return estimatedMonths; }
    public void setEstimatedMonths(Integer estimatedMonths) { this.estimatedMonths = estimatedMonths; }

    public List<String> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<String> extractedSkills) { this.extractedSkills = extractedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getCoreSkills() { return coreSkills; }
    public void setCoreSkills(List<String> coreSkills) { this.coreSkills = coreSkills; }

    public List<String> getPrerequisiteSkills() { return prerequisiteSkills; }
    public void setPrerequisiteSkills(List<String> prerequisiteSkills) { this.prerequisiteSkills = prerequisiteSkills; }

    public List<String> getOptionalSkills() { return optionalSkills; }
    public void setOptionalSkills(List<String> optionalSkills) { this.optionalSkills = optionalSkills; }

    public List<String> getExcludedSkills() { return excludedSkills; }
    public void setExcludedSkills(List<String> excludedSkills) { this.excludedSkills = excludedSkills; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getLearningPace() { return learningPace; }
    public void setLearningPace(String learningPace) { this.learningPace = learningPace; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}
