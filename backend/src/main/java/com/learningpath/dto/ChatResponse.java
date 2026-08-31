package com.learningpath.dto;

import java.util.List;

public class ChatResponse {
    private String reply;
    private String suggestedAction; // e.g. "ADAPT_ROADMAP", "REC_PROJECT"
    private String actionType;
    private Object actionPayload;
    private List<String> quickReplies;

    public ChatResponse() {}
    public ChatResponse(String reply, String suggestedAction, List<String> quickReplies) {
        this.reply = reply;
        this.suggestedAction = suggestedAction;
        this.quickReplies = quickReplies;
    }

    public ChatResponse(String reply, String suggestedAction, String actionType, Object actionPayload, List<String> quickReplies) {
        this.reply = reply;
        this.suggestedAction = suggestedAction;
        this.actionType = actionType;
        this.actionPayload = actionPayload;
        this.quickReplies = quickReplies;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Object getActionPayload() { return actionPayload; }
    public void setActionPayload(Object actionPayload) { this.actionPayload = actionPayload; }

    public List<String> getQuickReplies() { return quickReplies; }
    public void setQuickReplies(List<String> quickReplies) { this.quickReplies = quickReplies; }
}
