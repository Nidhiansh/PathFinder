package com.learningpath.dto;

import java.util.List;

public class LearningPathDto {
    private Long id;
    private String title;
    private String targetRole;
    private String status;
    private Double totalEstimatedHours;
    private Integer estimatedWeeks;
    private Double overallProgressPercentage;
    private Integer totalItems;
    private Integer completedItems;
    private List<LearningPhaseDto> phases;

    public LearningPathDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalEstimatedHours() { return totalEstimatedHours; }
    public void setTotalEstimatedHours(Double totalEstimatedHours) { this.totalEstimatedHours = totalEstimatedHours; }

    public Integer getEstimatedWeeks() { return estimatedWeeks; }
    public void setEstimatedWeeks(Integer estimatedWeeks) { this.estimatedWeeks = estimatedWeeks; }

    public Double getOverallProgressPercentage() { return overallProgressPercentage; }
    public void setOverallProgressPercentage(Double overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public Integer getCompletedItems() { return completedItems; }
    public void setCompletedItems(Integer completedItems) { this.completedItems = completedItems; }

    public List<LearningPhaseDto> getPhases() { return phases; }
    public void setPhases(List<LearningPhaseDto> phases) { this.phases = phases; }
}
