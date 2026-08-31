package com.learningpath.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_phases")
public class LearningPhase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @Column(name = "phase_number", nullable = false)
    private Integer phaseNumber;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PhaseStatus status = PhaseStatus.LOCKED;

    @Column(name = "estimated_hours")
    private Double estimatedHours = 0.0;

    @OneToMany(mappedBy = "learningPhase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC")
    private List<LearningPathItem> items = new ArrayList<>();

    public LearningPhase() {}

    public LearningPhase(LearningPath learningPath, Integer phaseNumber, String title, String description, PhaseStatus status, Double estimatedHours) {
        this.learningPath = learningPath;
        this.phaseNumber = phaseNumber;
        this.title = title;
        this.description = description;
        this.status = status;
        this.estimatedHours = estimatedHours;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LearningPath getLearningPath() { return learningPath; }
    public void setLearningPath(LearningPath learningPath) { this.learningPath = learningPath; }

    public Integer getPhaseNumber() { return phaseNumber; }
    public void setPhaseNumber(Integer phaseNumber) { this.phaseNumber = phaseNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PhaseStatus getStatus() { return status; }
    public void setStatus(PhaseStatus status) { this.status = status; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public List<LearningPathItem> getItems() { return items; }
    public void setItems(List<LearningPathItem> items) { this.items = items; }
}
