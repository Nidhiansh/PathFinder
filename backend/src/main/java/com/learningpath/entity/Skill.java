package com.learningpath.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 150)
    private String name;

    @Column(name = "canonical_name", length = 150)
    private String canonicalName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SkillCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private Difficulty difficultyLevel = Difficulty.INTERMEDIATE;

    @Column(name = "external_source", length = 50)
    private String externalSource = "PATHFINDER_CANONICAL";

    @Column(name = "external_id", length = 200)
    private String externalId;

    @Column(name = "source_version", length = 30)
    private String sourceVersion = "1.0.0";

    @Column(name = "domain", length = 100)
    private String domain = "GENERAL_ENGINEERING";

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SkillPrerequisite> prerequisites = new ArrayList<>();

    public Skill() {}

    public Skill(String name, SkillCategory category, String description, Difficulty difficultyLevel) {
        this.name = name;
        this.canonicalName = name;
        this.category = category;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
        this.externalSource = "PATHFINDER_CANONICAL";
    }

    public Skill(String name, SkillCategory category, String description, Difficulty difficultyLevel, String externalSource, String externalId, String domain) {
        this.name = name;
        this.canonicalName = name;
        this.category = category;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
        this.externalSource = externalSource;
        this.externalId = externalId;
        this.domain = domain;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCanonicalName() { return canonicalName != null ? canonicalName : name; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Difficulty getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(Difficulty difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getExternalSource() { return externalSource; }
    public void setExternalSource(String externalSource) { this.externalSource = externalSource; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getSourceVersion() { return sourceVersion; }
    public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public List<SkillPrerequisite> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<SkillPrerequisite> prerequisites) { this.prerequisites = prerequisites; }
}
