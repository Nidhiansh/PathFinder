package com.learningpath.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractGoalResponse {
    @JsonAlias({"target_role", "targetRole"})
    private String targetRole;

    @JsonAlias({"career_goal", "careerGoal"})
    private String careerGoal;

    @JsonAlias({"raw_goal", "rawGoal"})
    private String rawGoal;

    @JsonAlias({"normalized_goal", "normalizedGoal"})
    private String normalizedGoal;

    @JsonAlias({"goal_type", "goalType"})
    private String goalType = "TOPIC_LEARNING"; // TOPIC_LEARNING, CAREER_GOAL, PROJECT_GOAL, CERTIFICATION_GOAL

    @JsonAlias({"experience_level", "experienceLevel"})
    private String experienceLevel = "INTERMEDIATE";

    @JsonAlias({"estimated_months", "estimatedMonths"})
    private Integer estimatedMonths = 6;

    @JsonAlias({"extracted_skills", "extractedSkills"})
    private List<String> extractedSkills = new ArrayList<>();

    @JsonAlias({"missing_skills", "missingSkills"})
    private List<String> missingSkills = new ArrayList<>();

    @JsonAlias({"core_skills", "coreSkills"})
    private List<String> coreSkills = new ArrayList<>();

    @JsonAlias({"prerequisite_skills", "prerequisiteSkills"})
    private List<String> prerequisiteSkills = new ArrayList<>();

    @JsonAlias({"optional_skills", "optionalSkills"})
    private List<String> optionalSkills = new ArrayList<>();

    @JsonAlias({"excluded_skills", "excludedSkills"})
    private List<String> excludedSkills = new ArrayList<>();

    @JsonAlias({"confidence"})
    private Double confidence = 0.90;

    @JsonAlias({"learning_pace", "learningPace"})
    private String learningPace = "10 hours/week (Hands-On)";

    @JsonAlias({"ai_summary", "aiSummary"})
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
