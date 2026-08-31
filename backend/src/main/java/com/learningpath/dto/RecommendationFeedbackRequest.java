package com.learningpath.dto;

public class RecommendationFeedbackRequest {
    private Integer rating; // 1 to 5
    private String feedbackText;

    public RecommendationFeedbackRequest() {}
    public RecommendationFeedbackRequest(Integer rating, String feedbackText) {
        this.rating = rating;
        this.feedbackText = feedbackText;
    }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
}
