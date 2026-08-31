package com.learningpath.dto;

public class UserSkillDto {
    private Long skillId;
    private String skillName;
    private String category;
    private Integer proficiencyLevel;
    private Boolean isVerified;
    private Boolean isActive = true;
    private String source = "USER_PROVIDED";

    public UserSkillDto() {}
    public UserSkillDto(Long skillId, String skillName, String category, Integer proficiencyLevel, Boolean isVerified) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.proficiencyLevel = proficiencyLevel;
        this.isVerified = isVerified;
        this.isActive = true;
        this.source = "USER_PROVIDED";
    }

    public UserSkillDto(Long skillId, String skillName, String category, Integer proficiencyLevel, Boolean isVerified, Boolean isActive, String source) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.proficiencyLevel = proficiencyLevel;
        this.isVerified = isVerified;
        this.isActive = isActive != null ? isActive : true;
        this.source = source != null ? source : "USER_PROVIDED";
    }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(Integer proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
