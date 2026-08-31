package com.learningpath.dto;

import java.util.List;

public class AssessmentResultDto {
    private Long submissionId;
    private Long assessmentId;
    private String assessmentTitle;
    private String skillName;
    private Integer scorePercentage;
    private Boolean passed;
    private Integer correctCount;
    private Integer totalQuestions;
    private String adaptiveActionTaken;
    private String feedbackSummary;
    private List<QuestionDto> reviewedQuestions;

    public AssessmentResultDto() {}

    public Long getSubmissionId() { return id(); }
    public Long id() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public String getAssessmentTitle() { return assessmentTitle; }
    public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public Integer getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Integer scorePercentage) { this.scorePercentage = scorePercentage; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public String getAdaptiveActionTaken() { return adaptiveActionTaken; }
    public void setAdaptiveActionTaken(String adaptiveActionTaken) { this.adaptiveActionTaken = adaptiveActionTaken; }

    public String getFeedbackSummary() { return feedbackSummary; }
    public void setFeedbackSummary(String feedbackSummary) { this.feedbackSummary = feedbackSummary; }

    public List<QuestionDto> getReviewedQuestions() { return reviewedQuestions; }
    public void setReviewedQuestions(List<QuestionDto> reviewedQuestions) { this.reviewedQuestions = reviewedQuestions; }
}
