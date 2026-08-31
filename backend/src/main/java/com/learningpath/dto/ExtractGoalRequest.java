package com.learningpath.dto;

import java.util.List;

public class ExtractGoalRequest {
    private String prompt;

    public ExtractGoalRequest() {}
    public ExtractGoalRequest(String prompt) { this.prompt = prompt; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}
