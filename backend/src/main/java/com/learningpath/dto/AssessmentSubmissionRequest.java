package com.learningpath.dto;

import java.util.Map;

public class AssessmentSubmissionRequest {
    private Map<Long, Integer> answers; // questionId -> selectedOptionIndex

    public AssessmentSubmissionRequest() {}
    public AssessmentSubmissionRequest(Map<Long, Integer> answers) { this.answers = answers; }

    public Map<Long, Integer> getAnswers() { return answers; }
    public void setAnswers(Map<Long, Integer> answers) { this.answers = answers; }
}
