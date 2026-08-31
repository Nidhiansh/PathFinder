package com.learningpath.dto;

import java.util.List;
import java.util.Map;

public class RecommendationDto {
    private Long id;
    private String title;
    private String description;
    private String type; // COURSE, VIDEO, DOCUMENTATION, BOOK, TUTORIAL, PROJECT
    private String url;
    private String platform;
    private String difficulty;
    private Double estimatedHours;
    private Double score;
    private String explanation;
    private Map<String, Double> matchFactors; // e.g. skillGapMatch: 0.35, goalMatch: 0.25
    private List<String> skillsTaught;
    private List<String> prerequisites;
    private Boolean isPrerequisitesMet;

    public RecommendationDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Map<String, Double> getMatchFactors() { return matchFactors; }
    public void setMatchFactors(Map<String, Double> matchFactors) { this.matchFactors = matchFactors; }

    public List<String> getSkillsTaught() { return skillsTaught; }
    public void setSkillsTaught(List<String> skillsTaught) { this.skillsTaught = skillsTaught; }

    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }

    public Boolean getIsPrerequisitesMet() { return isPrerequisitesMet; }
    public void setIsPrerequisitesMet(Boolean isPrerequisitesMet) { this.isPrerequisitesMet = isPrerequisitesMet; }
}
