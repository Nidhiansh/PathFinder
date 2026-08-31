package com.learningpath.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id")
    private LearningResource resource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private Double score; // 0.0 - 100.0

    @Column(name = "match_factors_json", columnDefinition = "TEXT")
    private String matchFactorsJson;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;

    @Column(name = "is_accepted")
    private Boolean isAccepted = false;

    @Column(name = "is_dismissed")
    private Boolean isDismissed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Recommendation() {}

    public Recommendation(User user, LearningResource resource, Project project, Double score, String matchFactorsJson, String explanation) {
        this.user = user;
        this.resource = resource;
        this.project = project;
        this.score = score;
        this.matchFactorsJson = matchFactorsJson;
        this.explanation = explanation;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LearningResource getResource() { return resource; }
    public void setResource(LearningResource resource) { this.resource = resource; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getMatchFactorsJson() { return matchFactorsJson; }
    public void setMatchFactorsJson(String matchFactorsJson) { this.matchFactorsJson = matchFactorsJson; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Boolean getIsAccepted() { return isAccepted; }
    public void setIsAccepted(Boolean isAccepted) { this.isAccepted = isAccepted; }

    public Boolean getIsDismissed() { return isDismissed; }
    public void setIsDismissed(Boolean isDismissed) { this.isDismissed = isDismissed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
