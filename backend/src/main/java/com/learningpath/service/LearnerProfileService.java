package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.*;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.LearnerProfileRepository;
import com.learningpath.repository.SkillRepository;
import com.learningpath.repository.UserRepository;
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
                .orElseGet(() -> new LearnerProfile(user, user.getUsername(), "Software Engineer", ""));

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
            return skillRepository.findByNameIgnoreCase("General Engineering").orElseGet(() ->
                    skillRepository.save(new Skill("General Engineering", SkillCategory.CORE_CS, "General software engineering competencies", Difficulty.INTERMEDIATE))
            );
        }

        String cleanName = normalizeSkillName(skillName.trim());

        // 1. Direct match in DB
        Optional<Skill> exactMatch = skillRepository.findByNameIgnoreCase(cleanName);
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        // 2. Alias resolution
        String canonicalName = resolveCanonicalAlias(cleanName);
        if (!canonicalName.equalsIgnoreCase(cleanName)) {
            Optional<Skill> canonicalMatch = skillRepository.findByNameIgnoreCase(canonicalName);
            if (canonicalMatch.isPresent()) {
                return canonicalMatch.get();
            }
            cleanName = canonicalName;
        }

        // 3. Dynamic creation with safe category inference
        SkillCategory category = inferSkillCategory(cleanName);
        Skill newSkill = new Skill(cleanName, category, "Competency and practical applications in " + cleanName, Difficulty.INTERMEDIATE);
        return skillRepository.save(newSkill);
    }

    private String normalizeSkillName(String name) {
        String trimmed = name.replaceAll("(?i)^(mastery of|fundamentals of|learning|skills in|knowledge of)\\s+", "").trim();
        // Remove trailing punctuation
        trimmed = trimmed.replaceAll("[,.;:]+$", "").trim();
        return trimmed;
    }

    private String resolveCanonicalAlias(String name) {
        String lower = name.toLowerCase();
        if (lower.equals("vector db") || lower.equals("vector database") || lower.equals("vector databases") || lower.equals("vector dbs")) {
            return "Vector Databases & Embeddings";
        }
        if (lower.equals("prompt engineering") || lower.equals("prompting") || lower.equals("llm prompting") || lower.equals("prompt design")) {
            return "Prompt Engineering & LLM APIs";
        }
        if (lower.equals("rag") || lower.equals("retrieval augmented generation") || lower.equals("langchain rag") || lower.equals("rag pipelines")) {
            return "RAG Architecture & LangChain";
        }
        if (lower.equals("kubernetes") || lower.equals("k8s") || lower.equals("k8s orchestration") || lower.equals("kube")) {
            return "Cloud Infrastructure & Kubernetes";
        }
        if (lower.equals("docker") || lower.equals("containers") || lower.equals("containerization") || lower.equals("docker containers")) {
            return "Docker & Containers";
        }
        if (lower.equals("react") || lower.equals("reactjs") || lower.equals("react js")) {
            return "React.js";
        }
        if (lower.equals("javascript") || lower.equals("js") || lower.equals("es6") || lower.equals("vanilla js")) {
            return "JavaScript (ES6+)";
        }
        if (lower.equals("python") || lower.equals("python3") || lower.equals("core python")) {
            return "Python Programming";
        }
        if (lower.equals("java 21") || lower.equals("core java") || lower.equals("modern java")) {
            return "Java";
        }
        if (lower.equals("spring") || lower.equals("spring framework") || lower.equals("springboot")) {
            return "Spring Boot";
        }
        if (lower.equals("sql") || lower.equals("postgres") || lower.equals("postgresql") || lower.equals("relational db") || lower.equals("relational databases")) {
            return "SQL & Relational Databases";
        }
        if (lower.equals("git") || lower.equals("github") || lower.equals("version control")) {
            return "Git & Version Control";
        }
        if (lower.equals("flutter") || lower.equals("flutter framework")) {
            return "Flutter Framework & Widgets";
        }
        if (lower.equals("dart") || lower.equals("dart language")) {
            return "Dart Programming";
        }
        if (lower.equals("solidity") || lower.equals("solidity programming")) {
            return "Solidity Programming";
        }
        if (lower.equals("smart contract") || lower.equals("smart contracts") || lower.equals("evm")) {
            return "Smart Contracts & EVM";
        }
        if (lower.equals("opencv") || lower.equals("cv") || lower.equals("image processing")) {
            return "OpenCV Image Processing";
        }
        if (lower.equals("spark") || lower.equals("apache spark") || lower.equals("pyspark")) {
            return "Apache Spark & Distributed Computing";
        }
        if (lower.equals("kafka") || lower.equals("apache kafka")) {
            return "Kafka & Event Streaming";
        }
        return name;
    }

    private SkillCategory inferSkillCategory(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("rag") || lower.contains("llm") || lower.contains("prompt") || lower.contains("vector") ||
            lower.contains("ai") || lower.contains("machine learning") || lower.contains("deep learning") ||
            lower.contains("neural") || lower.contains("nlp") || lower.contains("data") || lower.contains("embedding") ||
            lower.contains("langchain") || lower.contains("llama") || lower.contains("evaluation") || lower.contains("model") ||
            lower.contains("vision") || lower.contains("opencv") || lower.contains("spark")) {
            return SkillCategory.DATA_AI;
        } else if (lower.contains("docker") || lower.contains("cloud") || lower.contains("kubernetes") ||
                   lower.contains("k8s") || lower.contains("devops") || lower.contains("ci/cd") ||
                   lower.contains("aws") || lower.contains("linux") || lower.contains("terraform") ||
                   lower.contains("security") || lower.contains("cybersecurity") || lower.contains("penetration")) {
            return SkillCategory.DEVOPS;
        } else if (lower.contains("react") || lower.contains("spring") || lower.contains("node") ||
                   lower.contains("express") || lower.contains("fastapi") || lower.contains("django") ||
                   lower.contains("flask") || lower.contains("vue") || lower.contains("angular") ||
                   lower.contains("flutter") || lower.contains("framework") || lower.contains("solidity") ||
                   lower.contains("web3") || lower.contains("smart contract")) {
            return SkillCategory.FRAMEWORK;
        } else if (lower.contains("python") || lower.contains("java") || lower.contains("javascript") ||
                   lower.contains("typescript") || lower.contains("c++") || lower.contains("go") ||
                   lower.contains("rust") || lower.contains("dart") || lower.contains("kotlin") || lower.contains("swift")) {
            return SkillCategory.LANGUAGE;
        } else if (lower.contains("sql") || lower.contains("database") || lower.contains("postgres") ||
                   lower.contains("mongo") || lower.contains("redis") || lower.contains("db") || lower.contains("kafka")) {
            return SkillCategory.DATABASE;
        } else if (lower.contains("api") || lower.contains("microservice") || lower.contains("system design") ||
                   lower.contains("architecture") || lower.contains("distributed")) {
            return SkillCategory.ARCHITECTURE;
        }
        return SkillCategory.CORE_CS;
    }

    public ExtractGoalResponse extractGoalFromPrompt(ExtractGoalRequest request) {
        return aiServiceClient.analyzeGoal(request.getPrompt());
    }

    private LearnerProfileDto mapToDto(LearnerProfile profile) {
        LearnerProfileDto dto = new LearnerProfileDto();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setUsername(profile.getUser().getUsername());
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

        // Return only currently active skills in profile DTO for clean downstream consumer personalization
        List<UserSkill> userSkills = userSkillRepository.findByProfileIdAndIsActiveTrue(profile.getId());
        if (userSkills.isEmpty()) {
            userSkills = userSkillRepository.findByProfileId(profile.getId());
        }

        List<UserSkillDto> skillDtos = userSkills.stream().map(us -> new UserSkillDto(
                us.getSkill().getId(),
                us.getSkill().getName(),
                us.getSkill().getCategory().name(),
                us.getProficiencyLevel(),
                us.getIsVerified(),
                us.getIsActive() != null ? us.getIsActive() : true,
                us.getSource() != null ? us.getSource().name() : "USER_PROVIDED"
        )).collect(Collectors.toList());
        dto.setSkills(skillDtos);

        return dto;
    }
}
