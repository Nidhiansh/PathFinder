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

        String targetRole = profile.getTargetRole() != null && !profile.getTargetRole().trim().isEmpty() 
                ? profile.getTargetRole() : (profile.getCareerGoal() != null && !profile.getCareerGoal().trim().isEmpty() ? profile.getCareerGoal() : "Engineering Specialist");

        Map<String, Integer> targetRequirements = new LinkedHashMap<>(skillService.getRoleSkillRequirements(targetRole));
        if (targetRequirements.isEmpty()) {
            for (UserSkill us : userSkills) {
                if (us.getIsActive() != null && us.getIsActive()) {
                    targetRequirements.put(us.getSkill().getName(), 75);
                }
            }
        }

        List<RecommendationDto> recommendations = new ArrayList<>();

        // 1. Score Courses & Resources in Database using generic semantic relevance
        for (LearningResource resource : allResources) {
            RecommendationDto dto = calculateResourceRecommendation(resource, profile, currentProficiencies, targetRequirements);
            if (dto.getScore() > 45.0) {
                recommendations.add(dto);
            }
        }

        // 2. If insufficient resources (< 2), synthesize dynamic domain-aligned recommendations
        if (recommendations.size() < 2 && !targetRequirements.isEmpty()) {
            int synthId = 30001;
            for (String reqSkill : targetRequirements.keySet()) {
                if (recommendations.size() >= 4) break;
                RecommendationDto synth = new RecommendationDto();
                synth.setId((long) synthId++);
                synth.setTitle("Comprehensive Study Specification: " + reqSkill);
                synth.setDescription("Structured conceptual learning curriculum, core reference material, and hands-on practice guidelines for " + reqSkill + ".");
                synth.setType("COURSE");
                synth.setUrl("https://openlibrary.org/search?q=" + java.net.URLEncoder.encode(reqSkill, java.nio.charset.StandardCharsets.UTF_8));
                synth.setPlatform("Curated Study Specification (AI_INFERRED)");
                synth.setDifficulty("INTERMEDIATE");
                synth.setEstimatedHours(16.0);
                synth.setScore(95.0);
                synth.setExplanation("Directly targets " + reqSkill + ", fulfilling high-priority competency gap for " + targetRole + ".");
                synth.setSkillsTaught(List.of(reqSkill));
                synth.setPrerequisites(List.of("Foundations of " + reqSkill));
                synth.setIsPrerequisitesMet(true);

                Map<String, Double> matchFactors = new LinkedHashMap<>();
                matchFactors.put("skillGapMatch", 0.95);
                matchFactors.put("goalRelevance", 0.98);
                matchFactors.put("prerequisiteCompatibility", 1.0);
                synth.setMatchFactors(matchFactors);

                recommendations.add(synth);
            }
        }

        // 3. Score Projects via ProjectService (domain-aligned, zero cross-domain leaks)
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
            int requiredLevel = targetRequirements.getOrDefault(skill, 0);
            if (requiredLevel > 0 && currentLevel < requiredLevel) {
                double gapProportion = (double)(requiredLevel - currentLevel) / 100.0;
                skillGapMatch += gapProportion;
                matchedSkillsCount++;
            }
        }
        if (matchedSkillsCount > 0) {
            skillGapMatch = Math.min(1.0, skillGapMatch / matchedSkillsCount);
        }

        // Factor 2: Goal Relevance (0.25) - Domain-Independent
        double goalMatch = 0.05;
        String roleLower = (profile.getTargetRole() != null ? profile.getTargetRole() : "").toLowerCase();
        String goalLower = (profile.getCareerGoal() != null ? profile.getCareerGoal() : "").toLowerCase();
        String titleLower = resource.getTitle().toLowerCase();

        boolean directSkillMatch = taughtSkills.stream().anyMatch(ts -> 
            targetRequirements.containsKey(ts) ||
            roleLower.contains(ts.toLowerCase()) || 
            goalLower.contains(ts.toLowerCase())
        );

        if (directSkillMatch) {
            goalMatch = 0.95;
        } else {
            // Check token overlap
            for (String req : targetRequirements.keySet()) {
                if (titleLower.contains(req.toLowerCase())) {
                    goalMatch = 0.90;
                    break;
                }
            }
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

        totalScore = Math.round(totalScore * 10.0) / 10.0;

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
        dto.setSkillsTaught(taughtSkills);
        dto.setIsPrerequisitesMet(prerequisitesMet);

        List<String> prereqNames = new ArrayList<>();
        for (String sName : taughtSkills) {
            Skill skill = skillRepository.findByNameIgnoreCase(sName).orElse(null);
            if (skill != null) {
                List<SkillPrerequisite> prereqs = prerequisiteRepository.findBySkillId(skill.getId());
                for (SkillPrerequisite sp : prereqs) {
                    prereqNames.add(sp.getPrerequisiteSkill().getName());
                }
            }
        }
        dto.setPrerequisites(prereqNames.stream().distinct().collect(Collectors.toList()));

        String explanation = generateExplainableReason(taughtSkills, skillGapMatch, goalMatch, prerequisitesMet, totalScore);
        dto.setExplanation(explanation);

        Map<String, Double> matchFactors = new LinkedHashMap<>();
        matchFactors.put("skillGapMatch", Math.round(skillGapMatch * 100.0) / 100.0);
        matchFactors.put("goalRelevance", Math.round(goalMatch * 100.0) / 100.0);
        matchFactors.put("prerequisiteCompatibility", Math.round(prerequisiteMatch * 100.0) / 100.0);
        matchFactors.put("difficultyMatch", Math.round(difficultyMatch * 100.0) / 100.0);
        matchFactors.put("learningStyleMatch", Math.round(styleMatch * 100.0) / 100.0);
        matchFactors.put("qualityScore", Math.round(qualityScore * 100.0) / 100.0);
        dto.setMatchFactors(matchFactors);

        return dto;
    }

    private String generateExplainableReason(
            List<String> taughtSkills,
            double skillGapMatch,
            double goalMatch,
            boolean prerequisitesMet,
            double totalScore
    ) {
        StringBuilder sb = new StringBuilder();
        String skillsStr = taughtSkills.isEmpty() ? "your target skills" : String.join(", ", taughtSkills);

        if (totalScore >= 90.0) {
            sb.append("Highest leverage milestone: ");
        } else if (totalScore >= 75.0) {
            sb.append("Strong goal alignment: ");
        } else {
            sb.append("Recommended milestone: ");
        }

        sb.append("directly targets ").append(skillsStr).append(" which is a high-priority competency requirement.");

        if (!prerequisitesMet) {
            sb.append(" Note: foundational prerequisites should be completed first.");
        }

        return sb.toString();
    }

    @Transactional
    public void submitFeedback(Long resourceId, RecommendationFeedbackRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        // Log / record feedback safely
    }

    @Transactional
    public void recordFeedback(RecommendationFeedbackRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
    }
}
