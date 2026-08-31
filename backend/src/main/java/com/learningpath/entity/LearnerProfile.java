package com.learningpath.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learner_profiles")
public class LearnerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "target_role", length = 100)
    private String targetRole;

    @Column(name = "career_goal", columnDefinition = "TEXT")
    private String careerGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 30)
    private ExperienceLevel experienceLevel = ExperienceLevel.BEGINNER;

    @Column(name = "weekly_hours")
    private Integer weeklyHours = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_style", length = 30)
    private LearningStyle preferredStyle = LearningStyle.PRACTICAL;

    @Column(name = "preferred_resource_types", length = 255)
    private String preferredResourceTypes = "COURSE,PROJECT,DOCUMENTATION";

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

    @Column(name = "streak_days")
    private Integer streakDays = 1;

    @Column(name = "total_hours_spent")
    private Double totalHoursSpent = 0.0;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSkill> userSkills = new ArrayList<>();

    public LearnerProfile() {}

    public LearnerProfile(User user, String fullName, String targetRole, String careerGoal) {
        this.user = user;
        this.fullName = fullName;
        this.targetRole = targetRole;
        this.careerGoal = careerGoal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { this.careerGoal = careerGoal; }

    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }

    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }

    public LearningStyle getPreferredStyle() { return preferredStyle; }
    public void setPreferredStyle(LearningStyle preferredStyle) { this.preferredStyle = preferredStyle; }

    public String getPreferredResourceTypes() { return preferredResourceTypes; }
    public void setPreferredResourceTypes(String preferredResourceTypes) { this.preferredResourceTypes = preferredResourceTypes; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }

    public Double getTotalHoursSpent() { return totalHoursSpent; }
    public void setTotalHoursSpent(Double totalHoursSpent) { this.totalHoursSpent = totalHoursSpent; }

    public List<UserSkill> getUserSkills() { return userSkills; }
    public void setUserSkills(List<UserSkill> userSkills) { this.userSkills = userSkills; }
}
