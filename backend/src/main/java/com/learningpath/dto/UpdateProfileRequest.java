package com.learningpath.dto;

import java.util.List;

public class UpdateProfileRequest {
    private String fullName;
    private String targetRole;
    private String careerGoal;
    private String experienceLevel;
    private Integer weeklyHours;
    private String preferredStyle;
    private String preferredResourceTypes;
    private String interests;
    private List<SkillProficiencyInput> skills;

    public static class SkillProficiencyInput {
        private String skillName;
        private Integer proficiencyLevel;

        public SkillProficiencyInput() {}
        public SkillProficiencyInput(String skillName, Integer proficiencyLevel) {
            this.skillName = skillName;
            this.proficiencyLevel = proficiencyLevel;
        }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public Integer getProficiencyLevel() { return proficiencyLevel; }
        public void setProficiencyLevel(Integer proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
    }

    public UpdateProfileRequest() {}

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

    public List<SkillProficiencyInput> getSkills() { return skills; }
    public void setSkills(List<SkillProficiencyInput> skills) { this.skills = skills; }
}
