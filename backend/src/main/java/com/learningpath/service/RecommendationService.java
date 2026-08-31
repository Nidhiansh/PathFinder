package com.learningpath.service;

import com.learningpath.dto.RecommendationDto;
import com.learningpath.dto.RecommendationFeedbackRequest;
import com.learningpath.entity.*;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private LearningResourceRepository resourceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResourceSkillRepository resourceSkillRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationFeedbackRepository feedbackRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillService skillService;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillPrerequisiteRepository prerequisiteRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ProjectService projectService;

    public List<RecommendationDto> getRecommendations() {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<LearningResource> allResources = resourceRepository.findAll();
        List<UserSkill> userSkills = userSkillRepository.findByProfileIdAndIsActiveTrue(profile.getId());
        if (userSkills.isEmpty()) {
            userSkills = userSkillRepository.findByProfileId(profile.getId());
        }

        Map<String, Integer> currentProficiencies = new HashMap<>();
        for (UserSkill us : userSkills) {
            currentProficiencies.put(us.getSkill().getName().toLowerCase(), us.getProficiencyLevel());
        }

        Map<String, Integer> targetRequirements = skillService.getRoleSkillRequirements(
                profile.getTargetRole() != null && !profile.getTargetRole().trim().isEmpty() 
                        ? profile.getTargetRole() : "Software Engineer"
        );

        List<RecommendationDto> recommendations = new ArrayList<>();

        // Score Courses & Resources
        for (LearningResource resource : allResources) {
            RecommendationDto dto = calculateResourceRecommendation(resource, profile, currentProficiencies, targetRequirements);
            if (dto.getScore() > 40.0) {
                recommendations.add(dto);
            }
        }

        // Score Projects via ProjectService (domain-aligned, zero Java leaks)
        List<com.learningpath.dto.ProjectDto> domainProjects = projectService.getRecommendedProjects();
        for (com.learningpath.dto.ProjectDto pdto : domainProjects) {
            RecommendationDto dto = new RecommendationDto();
            dto.setId(pdto.getId() != null ? pdto.getId() + 10000L : 20000L);
            dto.setTitle(pdto.getTitle());
            dto.setDescription(pdto.getDescription());
            dto.setType("PROJECT");
            dto.setUrl(pdto.getGithubTemplateUrl() != null ? pdto.getGithubTemplateUrl() : "https://github.com");
            dto.setPlatform("Portfolio Project");
            dto.setDifficulty(pdto.getDifficulty());
            dto.setEstimatedHours(pdto.getEstimatedHours());
            dto.setScore(pdto.getScore() != null ? pdto.getScore() : 90.0);
            dto.setExplanation(pdto.getExplanation());
            dto.setSkillsTaught(pdto.getSkills() != null && !pdto.getSkills().isEmpty() ? pdto.getSkills() : List.of(pdto.getPrimarySkillName()));
            dto.setPrerequisites(List.of(pdto.getPrimarySkillName() + " Fundamentals"));
            dto.setIsPrerequisitesMet(true);

            Map<String, Double> matchFactors = new LinkedHashMap<>();
            matchFactors.put("practicalApplication", 0.95);
            matchFactors.put("portfolioRelevance", 0.92);
            dto.setMatchFactors(matchFactors);

            recommendations.add(dto);
        }

        // Sort descending by score
        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return recommendations;
    }

    private RecommendationDto calculateResourceRecommendation(
            LearningResource resource,
            LearnerProfile profile,
            Map<String, Integer> currentProficiencies,
            Map<String, Integer> targetRequirements
    ) {
        List<ResourceSkill> resourceSkills = resourceSkillRepository.findByResourceId(resource.getId());
        List<String> taughtSkills = resourceSkills.stream().map(rs -> rs.getSkill().getName()).collect(Collectors.toList());

        // Factor 1: Skill Gap Match (0.30)
        double skillGapMatch = 0.0;
        int matchedSkillsCount = 0;
        for (String skill : taughtSkills) {
            String lowerSkill = skill.toLowerCase();
            int currentLevel = currentProficiencies.getOrDefault(lowerSkill, 0);
            int requiredLevel = targetRequirements.getOrDefault(skill, 70);
            if (currentLevel < requiredLevel) {
                double gapProportion = (double)(requiredLevel - currentLevel) / 100.0;
                skillGapMatch += gapProportion;
                matchedSkillsCount++;
            }
        }
        if (matchedSkillsCount > 0) {
            skillGapMatch = Math.min(1.0, skillGapMatch / matchedSkillsCount);
        }

        // Factor 2: Goal Relevance (0.25)
        double goalMatch = 0.50;
        String roleLower = (profile.getTargetRole() != null ? profile.getTargetRole() : "").toLowerCase();
        String goalLower = (profile.getCareerGoal() != null ? profile.getCareerGoal() : "").toLowerCase();
        String titleLower = resource.getTitle().toLowerCase();
        String descLower = (resource.getDescription() != null ? resource.getDescription() : "").toLowerCase();

        boolean directSkillMatch = taughtSkills.stream().anyMatch(ts -> 
            roleLower.contains(ts.toLowerCase()) || goalLower.contains(ts.toLowerCase()) ||
            targetRequirements.containsKey(ts)
        );

        if (directSkillMatch) {
            goalMatch = 0.95;
        } else if ((roleLower.contains("rag") || roleLower.contains("generative") || roleLower.contains("llm")) &&
                   (titleLower.contains("rag") || titleLower.contains("langchain") || titleLower.contains("vector") || titleLower.contains("prompt") || titleLower.contains("ragas") || descLower.contains("rag") || descLower.contains("embedding"))) {
            goalMatch = 0.95;
        } else if ((roleLower.contains("java") || roleLower.contains("backend")) &&
                   (titleLower.contains("java") || titleLower.contains("spring") || descLower.contains("java"))) {
            goalMatch = 0.95;
        } else if ((roleLower.contains("fullstack") || roleLower.contains("web") || roleLower.contains("react") || roleLower.contains("frontend")) &&
                   (titleLower.contains("react") || titleLower.contains("node") || titleLower.contains("javascript"))) {
            goalMatch = 0.95;
        } else if ((roleLower.contains("devops") || roleLower.contains("cloud") || roleLower.contains("kubernetes")) &&
                   (titleLower.contains("docker") || titleLower.contains("system design") || titleLower.contains("kubernetes"))) {
            goalMatch = 0.95;
        } else if (roleLower.contains("ai") && (titleLower.contains("python") || titleLower.contains("machine learning") || titleLower.contains("neural"))) {
            goalMatch = 0.90;
        }

        // Factor 3: Prerequisite Compatibility (0.15)
        double prerequisiteMatch = 1.0;
        boolean prerequisitesMet = true;
        for (String sName : taughtSkills) {
            Skill skill = skillRepository.findByNameIgnoreCase(sName).orElse(null);
            if (skill != null) {
                List<SkillPrerequisite> prereqs = prerequisiteRepository.findBySkillId(skill.getId());
                for (SkillPrerequisite sp : prereqs) {
                    int pLevel = currentProficiencies.getOrDefault(sp.getPrerequisiteSkill().getName().toLowerCase(), 0);
                    if (pLevel < 40) {
                        prerequisiteMatch = 0.4;
                        prerequisitesMet = false;
                        break;
                    }
                }
            }
        }

        // Factor 4: Difficulty Match (0.10)
        double difficultyMatch = 0.8;
        if (profile.getExperienceLevel() != null) {
            if (profile.getExperienceLevel().name().equalsIgnoreCase(resource.getDifficulty().name())) {
                difficultyMatch = 1.0;
            } else {
                difficultyMatch = 0.6;
            }
        }

        // Factor 5: Learning Style Preference (0.10)
        double styleMatch = 0.7;
        if (profile.getPreferredStyle() != null) {
            if (profile.getPreferredStyle() == LearningStyle.VIDEO && resource.getResourceType() == ResourceType.VIDEO) {
                styleMatch = 1.0;
            } else if (profile.getPreferredStyle() == LearningStyle.READING && (resource.getResourceType() == ResourceType.BOOK || resource.getResourceType() == ResourceType.DOCUMENTATION)) {
                styleMatch = 1.0;
            } else if (profile.getPreferredStyle() == LearningStyle.PRACTICAL && resource.getResourceType() == ResourceType.COURSE) {
                styleMatch = 0.95;
            }
        }

        // Factor 6: Quality Score (0.10)
        double qualityScore = resource.getQualityScore() != null ? resource.getQualityScore() : 0.9;

        // Weighted Total (0 - 100)
        double totalScore = (
                0.30 * skillGapMatch +
                0.25 * goalMatch +
                0.15 * prerequisiteMatch +
                0.10 * difficultyMatch +
                0.10 * styleMatch +
                0.10 * qualityScore
        ) * 100.0;

        totalScore = Math.min(99.0, Math.max(30.0, Math.round(totalScore * 10.0) / 10.0));

        // Generate Explainable AI Reasoning
        String explanation = generateResourceExplanation(resource, skillGapMatch, goalMatch, prerequisitesMet, profile);

        Map<String, Double> matchFactors = new LinkedHashMap<>();
        matchFactors.put("skillGapMatch", Math.round(skillGapMatch * 100.0) / 100.0);
        matchFactors.put("goalMatch", Math.round(goalMatch * 100.0) / 100.0);
        matchFactors.put("prerequisiteMatch", Math.round(prerequisiteMatch * 100.0) / 100.0);
        matchFactors.put("difficultyMatch", Math.round(difficultyMatch * 100.0) / 100.0);
        matchFactors.put("styleMatch", Math.round(styleMatch * 100.0) / 100.0);
        matchFactors.put("qualityMatch", Math.round(qualityScore * 100.0) / 100.0);

        RecommendationDto dto = new RecommendationDto();
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setDescription(resource.getDescription());
        dto.setType(resource.getResourceType().name());
        dto.setUrl(resource.getUrl());
        dto.setPlatform(resource.getPlatform());
        dto.setDifficulty(resource.getDifficulty().name());
        dto.setEstimatedHours(resource.getEstimatedHours());
        dto.setScore(totalScore);
        dto.setExplanation(explanation);
        dto.setMatchFactors(matchFactors);
        dto.setSkillsTaught(taughtSkills);
        dto.setPrerequisites(List.of());
        dto.setIsPrerequisitesMet(prerequisitesMet);

        return dto;
    }

    private RecommendationDto calculateProjectRecommendation(
            Project project,
            LearnerProfile profile,
            Map<String, Integer> currentProficiencies,
            Map<String, Integer> targetRequirements
    ) {
        String skillName = project.getPrimarySkill() != null ? project.getPrimarySkill().getName() : "Software Development";
        int currentLevel = currentProficiencies.getOrDefault(skillName.toLowerCase(), 0);
        
        Integer targetReq = targetRequirements.get(skillName);
        boolean isSkillInTargetRole = targetReq != null;
        int requiredLevel = isSkillInTargetRole ? targetReq : 0;

        double skillGapMatch = 0.20;
        if (isSkillInTargetRole) {
            skillGapMatch = currentLevel < requiredLevel ? Math.min(1.0, (double)(requiredLevel - currentLevel) / 100.0) : 0.40;
        }

        double goalMatch = isSkillInTargetRole ? 0.95 : 0.25;
        String roleLower = (profile.getTargetRole() != null ? profile.getTargetRole() : "").toLowerCase();
        String projTitleLower = project.getTitle().toLowerCase();
        String projDescLower = (project.getDescription() != null ? project.getDescription() : "").toLowerCase();

        if ((roleLower.contains("rag") || roleLower.contains("generative") || roleLower.contains("llm")) &&
            (projTitleLower.contains("rag") || projTitleLower.contains("vector") || projTitleLower.contains("qa") || projTitleLower.contains("hybrid") || projDescLower.contains("rag") || projDescLower.contains("chroma"))) {
            goalMatch = 0.98;
        } else if ((roleLower.contains("java") || roleLower.contains("backend")) &&
            (projTitleLower.contains("java") || projTitleLower.contains("spring") || projDescLower.contains("spring"))) {
            goalMatch = 0.98;
        } else if ((roleLower.contains("fullstack") || roleLower.contains("react")) &&
            (projTitleLower.contains("react") || projTitleLower.contains("full stack") || projDescLower.contains("react"))) {
            goalMatch = 0.98;
        } else if (!isSkillInTargetRole) {
            goalMatch = 0.15;
        }

        boolean prerequisitesMet = currentLevel >= 30 || (isSkillInTargetRole && currentProficiencies.size() > 0);
        double prerequisiteMatch = prerequisitesMet ? 1.0 : 0.4;
        double styleMatch = profile.getPreferredStyle() == LearningStyle.PRACTICAL ? 1.0 : 0.8;

        double totalScore = (
                0.35 * skillGapMatch +
                0.30 * goalMatch +
                0.15 * prerequisiteMatch +
                0.10 * styleMatch +
                0.10 * 0.95
        ) * 100.0;
        totalScore = Math.min(99.0, Math.max(25.0, Math.round(totalScore * 10.0) / 10.0));

        String explanation = "Recommended as a practical milestone project to validate and cement your " +
                skillName + " skills. Hands-on construction provides verifiable portfolio artifacts for your " +
                (profile.getTargetRole() != null ? profile.getTargetRole() : "career goal") + ".";

        Map<String, Double> matchFactors = new LinkedHashMap<>();
        matchFactors.put("practicalApplication", 0.95);
        matchFactors.put("skillGapFulfillment", skillGapMatch);
        matchFactors.put("portfolioRelevance", 0.90);

        RecommendationDto dto = new RecommendationDto();
        dto.setId(project.getId() + 10000L); // Offset to distinguish project IDs in recommendation list
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setType("PROJECT");
        dto.setUrl(project.getGithubTemplateUrl() != null ? project.getGithubTemplateUrl() : "https://github.com");
        dto.setPlatform("Hands-on Project");
        dto.setDifficulty(project.getDifficulty().name());
        dto.setEstimatedHours(project.getEstimatedHours());
        dto.setScore(totalScore);
        dto.setExplanation(explanation);
        dto.setMatchFactors(matchFactors);
        dto.setSkillsTaught(List.of(skillName));
        dto.setPrerequisites(List.of(skillName + " Foundations"));
        dto.setIsPrerequisitesMet(prerequisitesMet);

        return dto;
    }

    private String generateResourceExplanation(
            LearningResource resource,
            double skillGapMatch,
            double goalMatch,
            boolean prerequisitesMet,
            LearnerProfile profile
    ) {
        StringBuilder sb = new StringBuilder();
        if (skillGapMatch > 0.6) {
            sb.append("Directly addresses your primary skill gap for ").append(profile.getTargetRole()).append(". ");
        }
        if (prerequisitesMet) {
            sb.append("You have satisfied all foundational prerequisites, making this the optimal next stepping stone. ");
        } else {
            sb.append("Note: Completing foundational prerequisites first will maximize retention. ");
        }
        if (profile.getPreferredStyle() != null) {
            sb.append("Matches your preferred ").append(profile.getPreferredStyle().name().toLowerCase()).append(" learning style.");
        }
        return sb.toString();
    }

    @Transactional
    public void submitFeedback(Long recommendationId, RecommendationFeedbackRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        Recommendation recommendation = recommendationRepository.findById(recommendationId).orElse(null);
        if (recommendation != null) {
            RecommendationFeedback feedback = new RecommendationFeedback(
                    recommendation,
                    user,
                    request.getRating(),
                    request.getFeedbackText()
            );
            feedbackRepository.save(feedback);
        }
    }
}
