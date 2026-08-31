package com.learningpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skill_relations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source_skill_id", "target_skill_id", "relation_type"})
})
public class SkillRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_skill_id", nullable = false)
    private Skill sourceSkill;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_skill_id", nullable = false)
    private Skill targetSkill;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    private SkillRelationType relationType;

    @Column(name = "strength")
    private Double strength = 1.0; // 0.0 to 1.0

    @Column(columnDefinition = "TEXT")
    private String reason;

    public SkillRelation() {}

    public SkillRelation(Skill sourceSkill, Skill targetSkill, SkillRelationType relationType, Double strength, String reason) {
        this.sourceSkill = sourceSkill;
        this.targetSkill = targetSkill;
        this.relationType = relationType;
        this.strength = strength;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Skill getSourceSkill() { return sourceSkill; }
    public void setSourceSkill(Skill sourceSkill) { this.sourceSkill = sourceSkill; }

    public Skill getTargetSkill() { return targetSkill; }
    public void setTargetSkill(Skill targetSkill) { this.targetSkill = targetSkill; }

    public SkillRelationType getRelationType() { return relationType; }
    public void setRelationType(SkillRelationType relationType) { this.relationType = relationType; }

    public Double getStrength() { return strength; }
    public void setStrength(Double strength) { this.strength = strength; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
