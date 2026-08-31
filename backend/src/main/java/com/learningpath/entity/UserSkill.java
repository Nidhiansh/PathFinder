package com.learningpath.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_skills")
public class UserSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private LearnerProfile profile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "proficiency_level", nullable = false)
    private Integer proficiencyLevel = 0; // 0 - 100

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30)
    private SkillProficiencySource source = SkillProficiencySource.USER_PROVIDED;

    @Column(name = "last_assessed_at")
    private LocalDateTime lastAssessedAt;

    public UserSkill() {}

    public UserSkill(LearnerProfile profile, Skill skill, Integer proficiencyLevel, Boolean isVerified) {
        this.profile = profile;
        this.skill = skill;
        this.proficiencyLevel = proficiencyLevel;
        this.isVerified = isVerified;
        this.isActive = true;
        this.source = isVerified ? SkillProficiencySource.ASSESSED : SkillProficiencySource.USER_PROVIDED;
        this.lastAssessedAt = LocalDateTime.now();
    }

    public UserSkill(LearnerProfile profile, Skill skill, Integer proficiencyLevel, Boolean isVerified, Boolean isActive, SkillProficiencySource source) {
        this.profile = profile;
        this.skill = skill;
        this.proficiencyLevel = proficiencyLevel;
        this.isVerified = isVerified;
        this.isActive = isActive != null ? isActive : true;
        this.source = source != null ? source : SkillProficiencySource.USER_PROVIDED;
        this.lastAssessedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LearnerProfile getProfile() { return profile; }
    public void setProfile(LearnerProfile profile) { this.profile = profile; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Integer getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(Integer proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public SkillProficiencySource getSource() { return source; }
    public void setSource(SkillProficiencySource source) { this.source = source; }

    public LocalDateTime getLastAssessedAt() { return lastAssessedAt; }
    public void setLastAssessedAt(LocalDateTime lastAssessedAt) { this.lastAssessedAt = lastAssessedAt; }
}
