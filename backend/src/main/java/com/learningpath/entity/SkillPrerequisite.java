package com.learningpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skill_prerequisites")
public class SkillPrerequisite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prerequisite_skill_id", nullable = false)
    private Skill prerequisiteSkill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrerequisiteStrength strength = PrerequisiteStrength.REQUIRED;

    public SkillPrerequisite() {}

    public SkillPrerequisite(Skill skill, Skill prerequisiteSkill, PrerequisiteStrength strength) {
        this.skill = skill;
        this.prerequisiteSkill = prerequisiteSkill;
        this.strength = strength;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Skill getPrerequisiteSkill() { return prerequisiteSkill; }
    public void setPrerequisiteSkill(Skill prerequisiteSkill) { this.prerequisiteSkill = prerequisiteSkill; }

    public PrerequisiteStrength getStrength() { return strength; }
    public void setStrength(PrerequisiteStrength strength) { this.strength = strength; }
}
