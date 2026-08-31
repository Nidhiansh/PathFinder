package com.learningpath.dto;

import java.util.List;

public class SkillGapDto {
    private String skillName;
    private String category;
    private Integer currentProficiency;
    private Integer requiredProficiency;
    private Integer gap;
    private String status; // MASTERED, IN_PROGRESS, MISSING
    private List<String> unsatisfiedPrerequisites;

    public SkillGapDto() {}

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getCurrentProficiency() { return currentProficiency; }
    public void setCurrentProficiency(Integer currentProficiency) { this.currentProficiency = currentProficiency; }

    public Integer getRequiredProficiency() { return requiredProficiency; }
    public void setRequiredProficiency(Integer requiredProficiency) { this.requiredProficiency = requiredProficiency; }

    public Integer getGap() { return gap; }
    public void setGap(Integer gap) { this.gap = gap; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getUnsatisfiedPrerequisites() { return unsatisfiedPrerequisites; }
    public void setUnsatisfiedPrerequisites(List<String> unsatisfiedPrerequisites) { this.unsatisfiedPrerequisites = unsatisfiedPrerequisites; }
}
