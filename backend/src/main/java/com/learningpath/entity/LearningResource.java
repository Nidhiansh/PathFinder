package com.learningpath.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_resources")
public class LearningResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 100)
    private String platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name = "estimated_hours", nullable = false)
    private Double estimatedHours;

    @Column(nullable = false)
    private Double rating = 4.5;

    @Column(name = "quality_score", nullable = false)
    private Double qualityScore = 0.9;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ResourceSkill> resourceSkills = new ArrayList<>();

    public LearningResource() {}

    public LearningResource(String title, String description, ResourceType resourceType, String url, String platform, Difficulty difficulty, Double estimatedHours, Double rating, Double qualityScore) {
        this.title = title;
        this.description = description;
        this.resourceType = resourceType;
        this.url = url;
        this.platform = platform;
        this.difficulty = difficulty;
        this.estimatedHours = estimatedHours;
        this.rating = rating;
        this.qualityScore = qualityScore;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public List<ResourceSkill> getResourceSkills() { return resourceSkills; }
    public void setResourceSkills(List<ResourceSkill> resourceSkills) { this.resourceSkills = resourceSkills; }
}
