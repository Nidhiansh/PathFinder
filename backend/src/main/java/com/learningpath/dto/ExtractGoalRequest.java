package com.learningpath.dto;

public class ExtractGoalRequest {
    private String prompt;
    private Boolean applyToProfile = false;

    public ExtractGoalRequest() {}
    public ExtractGoalRequest(String prompt) { 
        this.prompt = prompt; 
    }
    public ExtractGoalRequest(String prompt, Boolean applyToProfile) { 
        this.prompt = prompt; 
        this.applyToProfile = applyToProfile;
    }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public Boolean getApplyToProfile() { return applyToProfile; }
    public void setApplyToProfile(Boolean applyToProfile) { this.applyToProfile = applyToProfile; }
}
