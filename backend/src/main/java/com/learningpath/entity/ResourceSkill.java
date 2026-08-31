package com.learningpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resource_skills")
public class ResourceSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private LearningResource resource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "coverage_weight")
    private Double coverageWeight = 1.0;

    public ResourceSkill() {}

    public ResourceSkill(LearningResource resource, Skill skill, Double coverageWeight) {
        this.resource = resource;
        this.skill = skill;
        this.coverageWeight = coverageWeight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LearningResource getResource() { return resource; }
    public void setResource(LearningResource resource) { this.resource = resource; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Double getCoverageWeight() { return coverageWeight; }
    public void setCoverageWeight(Double coverageWeight) { this.coverageWeight = coverageWeight; }
}
