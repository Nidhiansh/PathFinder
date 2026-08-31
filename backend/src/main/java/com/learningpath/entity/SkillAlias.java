package com.learningpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skill_aliases", indexes = {
    @Index(name = "idx_alias_lookup", columnList = "alias")
})
public class SkillAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String alias;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "canonical_skill_id", nullable = false)
    private Skill canonicalSkill;

    @Column(name = "match_type", length = 30)
    private String matchType = "SYNONYM"; // SYNONYM, ACRONYM, VARIANT, LOCALIZED

    public SkillAlias() {}

    public SkillAlias(String alias, Skill canonicalSkill, String matchType) {
        this.alias = alias;
        this.canonicalSkill = canonicalSkill;
        this.matchType = matchType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public Skill getCanonicalSkill() { return canonicalSkill; }
    public void setCanonicalSkill(Skill canonicalSkill) { this.canonicalSkill = canonicalSkill; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
}
