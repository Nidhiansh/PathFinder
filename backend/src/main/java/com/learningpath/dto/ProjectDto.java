package com.learningpath.dto;

import java.util.ArrayList;
import java.util.List;

public class ProjectDto {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private Double estimatedHours;
    private String deliverables;
    private String rubric;
    private String primarySkillName;
    private String githubTemplateUrl;
    private List<String> skills = new ArrayList<>();
    private Boolean isAiGenerated = false;
    private String roadmapPhase;
    private Double score = 90.0;
    private String explanation;
    private Boolean completed = false;

    public ProjectDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }

    public String getRubric() { return rubric; }
    public void setRubric(String rubric) { this.rubric = rubric; }

    public String getPrimarySkillName() { return primarySkillName; }
    public void setPrimarySkillName(String primarySkillName) { this.primarySkillName = primarySkillName; }

    public String getGithubTemplateUrl() { return githubTemplateUrl; }
    public void setGithubTemplateUrl(String githubTemplateUrl) { this.githubTemplateUrl = githubTemplateUrl; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public Boolean getIsAiGenerated() { return isAiGenerated; }
    public void setIsAiGenerated(Boolean isAiGenerated) { this.isAiGenerated = isAiGenerated; }

    public String getRoadmapPhase() { return roadmapPhase; }
    public void setRoadmapPhase(String roadmapPhase) { this.roadmapPhase = roadmapPhase; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
