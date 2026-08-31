package com.learningpath.service;

import com.learningpath.dto.ProjectDto;
import com.learningpath.dto.SkillGapDto;
import com.learningpath.entity.*;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillService skillService;

    @Autowired
    private LearningPathRepository pathRepository;

    @Autowired
    private LearningPhaseRepository phaseRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private AiServiceClient aiServiceClient;

    public List<ProjectDto> getRecommendedProjects() {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<UserSkill> activeSkills = userSkillRepository.findByProfileIdAndIsActiveTrue(profile.getId());
        if (activeSkills.isEmpty()) {
            activeSkills = userSkillRepository.findByProfileId(profile.getId());
        }

        Set<String> activeSkillNames = activeSkills.stream()
                .map(us -> us.getSkill().getName().toLowerCase())
                .collect(Collectors.toSet());

        String targetRole = profile.getTargetRole() != null && !profile.getTargetRole().trim().isEmpty()
                ? profile.getTargetRole()
                : "Software Engineer";

        Map<String, Integer> targetRequirements = skillService.getRoleSkillRequirements(targetRole);
        List<SkillGapDto> skillGaps = skillService.calculateSkillGaps();
        List<String> gapNames = skillGaps.stream().map(SkillGapDto::getSkillName).collect(Collectors.toList());

        // Retrieve active roadmap phases
        Optional<LearningPath> activePathOpt = pathRepository.findByUserIdAndStatus(user.getId(), PathStatus.ACTIVE);
        List<String> phaseNames = new ArrayList<>();
        if (activePathOpt.isPresent()) {
            List<LearningPhase> phases = phaseRepository.findByLearningPathIdOrderByPhaseNumberAsc(activePathOpt.get().getId());
            for (LearningPhase ph : phases) {
                phaseNames.add(ph.getTitle());
            }
        }

        List<ProjectDto> matchedProjects = new ArrayList<>();
        List<Project> allDbProjects = projectRepository.findAll();

        // 1. Dynamic Matching of Existing Database Projects
        for (Project p : allDbProjects) {
            String pSkill = p.getPrimarySkill() != null ? p.getPrimarySkill().getName().toLowerCase() : "";
            boolean matchesActiveSkill = activeSkillNames.contains(pSkill);
            boolean matchesTargetReq = targetRequirements.keySet().stream().anyMatch(req -> req.equalsIgnoreCase(pSkill));
            
            String roleLower = targetRole.toLowerCase();
            String pTitleLower = p.getTitle().toLowerCase();
            String pDescLower = p.getDescription() != null ? p.getDescription().toLowerCase() : "";

            boolean roleMatches = false;
            if ((roleLower.contains("java") || roleLower.contains("backend")) && (pTitleLower.contains("java") || pTitleLower.contains("spring"))) {
                roleMatches = true;
            } else if ((roleLower.contains("rag") || roleLower.contains("generative") || roleLower.contains("llm")) && 
                       (pTitleLower.contains("rag") || pTitleLower.contains("vector") || pDescLower.contains("rag") || pDescLower.contains("langchain"))) {
                roleMatches = true;
            } else if ((roleLower.contains("fullstack") || roleLower.contains("react")) && (pTitleLower.contains("react") || pTitleLower.contains("full stack"))) {
                roleMatches = true;
            } else if ((roleLower.contains("devops") || roleLower.contains("cloud") || roleLower.contains("kubernetes")) && (pTitleLower.contains("docker") || pTitleLower.contains("compose"))) {
                roleMatches = true;
            }

            if (matchesActiveSkill || matchesTargetReq || roleMatches) {
                ProjectDto dto = new ProjectDto();
                dto.setId(p.getId());
                dto.setTitle(p.getTitle());
                dto.setDescription(p.getDescription());
                dto.setDifficulty(p.getDifficulty() != null ? p.getDifficulty().name() : "INTERMEDIATE");
                dto.setEstimatedHours(p.getEstimatedHours() != null ? p.getEstimatedHours() : 15.0);
                dto.setDeliverables(p.getDeliverables());
                dto.setRubric(p.getRubric());
                dto.setPrimarySkillName(p.getPrimarySkill() != null ? p.getPrimarySkill().getName() : "Software Engineering");
                dto.setSkills(p.getPrimarySkill() != null ? List.of(p.getPrimarySkill().getName()) : List.of());
                dto.setGithubTemplateUrl(p.getGithubTemplateUrl() != null ? p.getGithubTemplateUrl() : "https://github.com");
                dto.setIsAiGenerated(false);
                dto.setRoadmapPhase("Core Architecture Milestone");
                dto.setScore(94.0);
                dto.setExplanation("Curated engineering milestone directly validating " + dto.getPrimarySkillName() + " competency.");
                matchedProjects.add(dto);
            }
        }

        // 2. If database projects are insufficient (< 2), dynamically synthesize structured projects via AI / Graph Generator
        if (matchedProjects.size() < 2) {
            List<ProjectDto> generated = aiServiceClient.generateProjects(
                    targetRole,
                    profile.getCareerGoal(),
                    profile.getExperienceLevel() != null ? profile.getExperienceLevel().name() : "INTERMEDIATE",
                    new ArrayList<>(activeSkillNames),
                    gapNames,
                    phaseNames,
                    null
            );

            for (ProjectDto gen : generated) {
                boolean alreadyPresent = matchedProjects.stream().anyMatch(m -> m.getTitle().equalsIgnoreCase(gen.getTitle()));
                if (!alreadyPresent) {
                    matchedProjects.add(gen);
                }
            }
        }

        // Sort descending by score / estimated hours
        matchedProjects.sort((a, b) -> Double.compare(
                b.getScore() != null ? b.getScore() : 90.0,
                a.getScore() != null ? a.getScore() : 90.0
        ));

        return matchedProjects;
    }

    public List<ProjectDto> generateAdaptiveProject(String customTopic) {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<UserSkill> activeSkills = userSkillRepository.findByProfileIdAndIsActiveTrue(profile.getId());
        List<String> skillNames = activeSkills.stream().map(us -> us.getSkill().getName()).collect(Collectors.toList());
        List<SkillGapDto> skillGaps = skillService.calculateSkillGaps();
        List<String> gapNames = skillGaps.stream().map(SkillGapDto::getSkillName).collect(Collectors.toList());

        return aiServiceClient.generateProjects(
                profile.getTargetRole() != null ? profile.getTargetRole() : "Software Engineer",
                profile.getCareerGoal(),
                profile.getExperienceLevel() != null ? profile.getExperienceLevel().name() : "INTERMEDIATE",
                skillNames,
                gapNames,
                List.of(),
                customTopic
        );
    }
}
