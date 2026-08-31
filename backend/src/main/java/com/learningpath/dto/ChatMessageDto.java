package com.learningpath.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private Long id;
    private String sender; // USER, ASSISTANT
    private String message;
    private String metadataJson;
    private LocalDateTime createdAt;

    public ChatMessageDto() {}
    public ChatMessageDto(Long id, String sender, String message, String metadataJson, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.metadataJson = metadataJson;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
