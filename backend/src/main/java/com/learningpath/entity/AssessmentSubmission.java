package com.learningpath.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_submissions")
public class AssessmentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "score_percentage", nullable = false)
    private Integer scorePercentage;

    @Column(nullable = false)
    private Boolean passed;

    @Column(name = "adaptive_action_taken", columnDefinition = "TEXT")
    private String adaptiveActionTaken;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    public AssessmentSubmission() {}

    public AssessmentSubmission(User user, Assessment assessment, Integer scorePercentage, Boolean passed, String adaptiveActionTaken) {
        this.user = user;
        this.assessment = assessment;
        this.scorePercentage = scorePercentage;
        this.passed = passed;
        this.adaptiveActionTaken = adaptiveActionTaken;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public Integer getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Integer scorePercentage) { this.scorePercentage = scorePercentage; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public String getAdaptiveActionTaken() { return adaptiveActionTaken; }
    public void setAdaptiveActionTaken(String adaptiveActionTaken) { this.adaptiveActionTaken = adaptiveActionTaken; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
