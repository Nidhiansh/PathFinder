package com.learningpath.dto;

public class RecalculateTimeRequest {
    private Integer weeklyHours;

    public RecalculateTimeRequest() {}
    public RecalculateTimeRequest(Integer weeklyHours) { this.weeklyHours = weeklyHours; }

    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }
}
