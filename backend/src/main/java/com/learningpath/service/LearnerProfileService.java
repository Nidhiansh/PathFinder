package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.*;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.LearnerProfileRepository;
import com.learningpath.repository.SkillAliasRepository;
import com.learningpath.repository.SkillRepository;
import com.learningpath.repository.UserSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LearnerProfileService {

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillAliasRepository aliasRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private AiServiceClient aiServiceClient;

    @Autowired
    private RoadmapService roadmapService;

    public LearnerProfileDto getProfile() {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user"));

        return mapToDto(profile);
    }

    @Transactional
    public LearnerProfileDto updateProfile(UpdateProfileRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseGet(() -> new LearnerProfile(user, user.getUsername(), "Engineering Specialist", ""));

        boolean roleChanged = false;
        if (request.getTargetRole() != null && !request.getTargetRole().equalsIgnoreCase(profile.getTargetRole())) {
            roleChanged = true;
            profile.setTargetRole(request.getTargetRole());
        }

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getCareerGoal() != null) profile.setCareerGoal(request.getCareerGoal());
        if (request.getExperienceLevel() != null) {
            try {
                profile.setExperienceLevel(ExperienceLevel.valueOf(request.getExperienceLevel().toUpperCase()));
            } catch (Exception e) {
                profile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
            }
        }
        if (request.getWeeklyHours() != null && request.getWeeklyHours() > 0) {
            profile.setWeeklyHours(request.getWeeklyHours());
        }
        if (request.getPreferredStyle() != null) {
            try {
                profile.setPreferredStyle(LearningStyle.valueOf(request.getPreferredStyle().toUpperCase()));
            } catch (Exception e) {
                profile.setPreferredStyle(LearningStyle.PRACTICAL);
            }
        }
        if (request.getPreferredResourceTypes() != null) profile.setPreferredResourceTypes(request.getPreferredResourceTypes());
        if (request.getInterests() != null) profile.setInterests(request.getInterests());

        profile = profileRepository.save(profile);

        // Synchronize skills if provided
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            Set<String> incomingSkillNames = request.getSkills().stream()
                    .map(s -> s.getSkillName().trim().toLowerCase())
                    .collect(Collectors.toSet());

            // If role changed or fresh goal set, deactivate previous skills that are not in the new goal's skill set
            List<UserSkill> existingUserSkills = userSkillRepository.findByProfileId(profile.getId());
            for (UserSkill us : existingUserSkills) {
                String existingName = us.getSkill().getName().trim().toLowerCase();
                if (!incomingSkillNames.contains(existingName)) {
                    us.setIsActive(false); // Retain in database for historical record, but deactivate from current goal
                    userSkillRepository.save(us);
                }
            }

            // Save or activate incoming skills
            for (UpdateProfileRequest.SkillProficiencyInput skillInput : request.getSkills()) {
                if (skillInput.getSkillName() == null || skillInput.getSkillName().trim().isEmpty()) continue;
                String sName = skillInput.getSkillName().trim();
                Skill skill = resolveOrCreateSkill(sName);

                UserSkill userSkill = userSkillRepository.findByProfileIdAndSkillId(profile.getId(), skill.getId()).orElse(null);
                int profLevel = skillInput.getProficiencyLevel() != null ? Math.min(100, Math.max(0, skillInput.getProficiencyLevel())) : 50;

                if (userSkill == null) {
                    userSkill = new UserSkill(profile, skill, profLevel, false, true, SkillProficiencySource.USER_PROVIDED);
                } else {
                    userSkill.setProficiencyLevel(profLevel);
                    userSkill.setIsActive(true); // Reactivate for current goal
                    if (userSkill.getSource() == null) {
                        userSkill.setSource(SkillProficiencySource.USER_PROVIDED);
                    }
                }
                userSkillRepository.save(userSkill);
            }
        }

        // Auto-regenerate active roadmap if role changed
        if (roleChanged) {
            roadmapService.generatePersonalizedRoadmap(user);
        }

        return mapToDto(profile);
    }

    public Skill resolveOrCreateSkill(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            return skillRepository.findByNameIgnoreCase("General Competency").orElseGet(() ->
                    skillRepository.save(new Skill("General Competency", SkillCategory.CORE_CS, "General foundational competencies", Difficulty.INTERMEDIATE))
            );
        }

        String cleanName = normalizeSkillName(skillName.trim());

        // 1. Direct match in DB
        Optional<Skill> exactMatch = skillRepository.findByNameIgnoreCase(cleanName);
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        // 2. Query Alias repository
        Optional<SkillAlias> aliasOpt = aliasRepository.findByAliasIgnoreCase(cleanName.toLowerCase());
        if (aliasOpt.isPresent()) {
            return aliasOpt.get().getCanonicalSkill();
        }

        // 3. Dynamic creation with safe domain and category inference
        SkillCategory category = inferSkillCategory(cleanName);
        Skill newSkill = new Skill(cleanName, category, "Competency and practical applications in " + cleanName, Difficulty.INTERMEDIATE, "DYNAMIC_INFERRED", null, "UNANTICIPATED_DOMAIN");
        return skillRepository.save(newSkill);
    }

    private String normalizeSkillName(String name) {
        String trimmed = name.replaceAll("(?i)^(mastery of|fundamentals of|learning|skills in|knowledge of)\\s+", "").trim();
        trimmed = trimmed.replaceAll("[,.;:]+$", "").trim();
        return trimmed;
    }

    private SkillCategory inferSkillCategory(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("react") || lower.contains("vue") || lower.contains("angular") || lower.contains("html") || lower.contains("css") || lower.contains("ui") || lower.contains("widget")) {
            return SkillCategory.FRAMEWORK;
        }
        if (lower.contains("sql") || lower.contains("postgres") || lower.contains("mongo") || lower.contains("database") || lower.contains("redis") || lower.contains("db")) {
            return SkillCategory.DATABASE;
        }
        if (lower.contains("docker") || lower.contains("kubernetes") || lower.contains("k8s") || lower.contains("cloud") || lower.contains("aws") || lower.contains("ci/cd") || lower.contains("terraform")) {
            return SkillCategory.DEVOPS;
        }
        if (lower.contains("ai") || lower.contains("ml") || lower.contains("rag") || lower.contains("llm") || lower.contains("vector") || lower.contains("learning") || lower.contains("model") || lower.contains("data")) {
            return SkillCategory.DATA_AI;
        }
        if (lower.contains("java") || lower.contains("python") || lower.contains("c++") || lower.contains("rust") || lower.contains("golang") || lower.contains("javascript") || lower.contains("typescript") || lower.contains("dart") || lower.contains("solidity")) {
            return SkillCategory.LANGUAGE;
        }
        if (lower.contains("api") || lower.contains("rest") || lower.contains("microservices") || lower.contains("system design") || lower.contains("architecture")) {
            return SkillCategory.ARCHITECTURE;
        }
        return SkillCategory.CORE_CS;
    }

    @Transactional
    public ExtractGoalResponse extractGoalFromPrompt(ExtractGoalRequest request) {
        ExtractGoalResponse analysis = aiServiceClient.analyzeGoal(request.getPrompt());

        // Update profile with extracted goal if requested
        if (request.getApplyToProfile() != null && request.getApplyToProfile()) {
            User user = authService.getCurrentAuthenticatedUser();
            LearnerProfile profile = profileRepository.findByUser(user)
                    .orElseGet(() -> new LearnerProfile(user, user.getUsername(), analysis.getTargetRole(), request.getPrompt()));

            profile.setTargetRole(analysis.getTargetRole());
            profile.setCareerGoal(request.getPrompt());
            if (analysis.getExperienceLevel() != null) {
                try {
                    profile.setExperienceLevel(ExperienceLevel.valueOf(analysis.getExperienceLevel().toUpperCase()));
                } catch (Exception e) {
                    profile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
                }
            }
            profile = profileRepository.save(profile);

            // Deactivate existing skills that are not part of the newly extracted goal
            Set<String> newRequiredSkills = (analysis.getMissingSkills() != null ? analysis.getMissingSkills() : new ArrayList<String>()).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            if (analysis.getCoreSkills() != null) {
                for (String cs : analysis.getCoreSkills()) newRequiredSkills.add(cs.toLowerCase());
            }

            List<UserSkill> existingSkills = userSkillRepository.findByProfileId(profile.getId());
            for (UserSkill us : existingSkills) {
                if (!newRequiredSkills.contains(us.getSkill().getName().toLowerCase())) {
                    us.setIsActive(false);
                    userSkillRepository.save(us);
                }
            }

            // Populate active skills for newly extracted goal
            if (analysis.getMissingSkills() != null) {
                for (String skillName : analysis.getMissingSkills()) {
                    Skill skill = resolveOrCreateSkill(skillName);
                    UserSkill userSkill = userSkillRepository.findByProfileIdAndSkillId(profile.getId(), skill.getId()).orElse(null);
                    if (userSkill == null) {
                        userSkill = new UserSkill(profile, skill, 0, false, true, SkillProficiencySource.NOT_ASSESSED);
                    } else {
                        userSkill.setIsActive(true);
                    }
                    userSkillRepository.save(userSkill);
                }
            }

            // Immediately synthesize fresh personalized roadmap for this new goal
            roadmapService.generatePersonalizedRoadmap(user);
        }

        return analysis;
    }

    private LearnerProfileDto mapToDto(LearnerProfile profile) {
        List<UserSkill> userSkills = userSkillRepository.findByProfileIdAndIsActiveTrue(profile.getId());
        if (userSkills.isEmpty()) {
            userSkills = userSkillRepository.findByProfileId(profile.getId());
        }

        List<UserSkillDto> skillDtos = userSkills.stream().map(us -> {
            Skill skill = us.getSkill();
            return new UserSkillDto(
                    skill.getId(),
                    skill.getName(),
                    skill.getCategory().name(),
                    us.getProficiencyLevel(),
                    us.getIsVerified(),
                    us.getIsActive() != null ? us.getIsActive() : true,
                    us.getSource() != null ? us.getSource().name() : "USER_PROVIDED"
            );
        }).collect(Collectors.toList());

        LearnerProfileDto dto = new LearnerProfileDto();
        dto.setId(profile.getId());
        if (profile.getUser() != null) {
            dto.setUserId(profile.getUser().getId());
            dto.setUsername(profile.getUser().getUsername());
        }
        dto.setFullName(profile.getFullName());
        dto.setTargetRole(profile.getTargetRole());
        dto.setCareerGoal(profile.getCareerGoal());
        dto.setExperienceLevel(profile.getExperienceLevel() != null ? profile.getExperienceLevel().name() : "BEGINNER");
        dto.setWeeklyHours(profile.getWeeklyHours());
        dto.setPreferredStyle(profile.getPreferredStyle() != null ? profile.getPreferredStyle().name() : "PRACTICAL");
        dto.setPreferredResourceTypes(profile.getPreferredResourceTypes());
        dto.setInterests(profile.getInterests());
        dto.setStreakDays(profile.getStreakDays());
        dto.setTotalHoursSpent(profile.getTotalHoursSpent());
        dto.setSkills(skillDtos);
        return dto;
    }
}
