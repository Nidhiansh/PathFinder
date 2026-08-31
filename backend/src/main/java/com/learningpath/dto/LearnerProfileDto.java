package com.learningpath.dto;

import java.util.List;

public class LearnerProfileDto {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String targetRole;
    private String careerGoal;
    private String experienceLevel;
    private Integer weeklyHours;
    private String preferredStyle;
    private String preferredResourceTypes;
    private String interests;
    private Integer streakDays;
    private Double totalHoursSpent;
    private List<UserSkillDto> skills;

    public LearnerProfileDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { this.careerGoal = careerGoal; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }

    public String getPreferredStyle() { return preferredStyle; }
    public void setPreferredStyle(String preferredStyle) { this.preferredStyle = preferredStyle; }

    public String getPreferredResourceTypes() { return preferredResourceTypes; }
    public void setPreferredResourceTypes(String preferredResourceTypes) { this.preferredResourceTypes = preferredResourceTypes; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }

    public Double getTotalHoursSpent() { return totalHoursSpent; }
    public void setTotalHoursSpent(Double totalHoursSpent) { this.totalHoursSpent = totalHoursSpent; }

    public List<UserSkillDto> getSkills() { return skills; }
    public void setSkills(List<UserSkillDto> skills) { this.skills = skills; }
}
