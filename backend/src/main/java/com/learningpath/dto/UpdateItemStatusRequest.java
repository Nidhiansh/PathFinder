package com.learningpath.dto;

public class UpdateItemStatusRequest {
    private String status; // IN_PROGRESS, COMPLETED

    public UpdateItemStatusRequest() {}
    public UpdateItemStatusRequest(String status) { this.status = status; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
