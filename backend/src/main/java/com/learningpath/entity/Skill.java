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

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SkillCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private Difficulty difficultyLevel = Difficulty.INTERMEDIATE;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SkillPrerequisite> prerequisites = new ArrayList<>();

    public Skill() {}

    public Skill(String name, SkillCategory category, String description, Difficulty difficultyLevel) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Difficulty getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(Difficulty difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public List<SkillPrerequisite> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<SkillPrerequisite> prerequisites) { this.prerequisites = prerequisites; }
}
