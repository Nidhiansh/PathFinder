package com.learningpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(columnDefinition = "TEXT")
    private String deliverables;

    @Column(columnDefinition = "TEXT")
    private String rubric;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primary_skill_id")
    private Skill primarySkill;

    @Column(name = "github_template_url", length = 500)
    private String githubTemplateUrl;

    public Project() {}

    public Project(String title, String description, Difficulty difficulty, Double estimatedHours, String deliverables, String rubric, Skill primarySkill, String githubTemplateUrl) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.estimatedHours = estimatedHours;
        this.deliverables = deliverables;
        this.rubric = rubric;
        this.primarySkill = primarySkill;
        this.githubTemplateUrl = githubTemplateUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }

    public String getRubric() { return rubric; }
    public void setRubric(String rubric) { this.rubric = rubric; }

    public Skill getPrimarySkill() { return primarySkill; }
    public void setPrimarySkill(Skill primarySkill) { this.primarySkill = primarySkill; }

    public String getGithubTemplateUrl() { return githubTemplateUrl; }
    public void setGithubTemplateUrl(String githubTemplateUrl) { this.githubTemplateUrl = githubTemplateUrl; }
}
