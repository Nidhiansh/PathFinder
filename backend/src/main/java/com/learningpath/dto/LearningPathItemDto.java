package com.learningpath.dto;

import java.util.List;

public class LearningPathItemDto {
    private Long id;
    private String itemType; // RESOURCE, PROJECT, ASSESSMENT
    private Long referenceId;
    private String title;
    private String description;
    private String url;
    private String platform;
    private String difficulty;
    private Double estimatedHours;
    private Integer orderIndex;
    private String status; // LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED
    private Double recommendationScore;
    private String recommendationReason;
    private List<String> requiredPrerequisites;
    private Boolean isLocked;

    public LearningPathItemDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getRecommendationScore() { return recommendationScore; }
    public void setRecommendationScore(Double recommendationScore) { this.recommendationScore = recommendationScore; }

    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }

    public List<String> getRequiredPrerequisites() { return requiredPrerequisites; }
    public void setRequiredPrerequisites(List<String> requiredPrerequisites) { this.requiredPrerequisites = requiredPrerequisites; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
}
