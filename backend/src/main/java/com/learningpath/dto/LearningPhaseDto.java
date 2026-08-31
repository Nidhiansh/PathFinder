package com.learningpath.dto;

import java.util.List;

public class LearningPhaseDto {
    private Long id;
    private Integer phaseNumber;
    private String title;
    private String description;
    private String status; // LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED
    private Double estimatedHours;
    private Double progressPercentage;
    private List<LearningPathItemDto> items;

    public LearningPhaseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPhaseNumber() { return phaseNumber; }
    public void setPhaseNumber(Integer phaseNumber) { this.phaseNumber = phaseNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }

    public List<LearningPathItemDto> getItems() { return items; }
    public void setItems(List<LearningPathItemDto> items) { this.items = items; }
}
