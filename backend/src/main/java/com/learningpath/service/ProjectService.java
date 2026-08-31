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
                : (profile.getCareerGoal() != null && !profile.getCareerGoal().trim().isEmpty() ? profile.getCareerGoal() : "Engineering Specialist");

        Map<String, Integer> targetRequirements = skillService.getRoleSkillRequirements(targetRole);
        Set<String> allRequiredSkillNames = new LinkedHashSet<>();
        if (!targetRequirements.isEmpty()) {
            allRequiredSkillNames.addAll(targetRequirements.keySet());
        } else {
            for (UserSkill us : activeSkills) {
                if (us.getIsActive() != null && us.getIsActive()) {
                    allRequiredSkillNames.add(us.getSkill().getName());
                }
            }
        }
        if (allRequiredSkillNames.isEmpty()) {
            allRequiredSkillNames.add(targetRole);
        }

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

        // 1. Generic Semantic Relevance Matching of Database Projects
        for (Project p : allDbProjects) {
            String pTitle = p.getTitle().toLowerCase();
            String pDesc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
            String pSkill = p.getPrimarySkill() != null ? p.getPrimarySkill().getName().toLowerCase() : "";
            
            double relevance = 0.0;
            
            // Check direct primary skill match
            if (!pSkill.isEmpty() && allRequiredSkillNames.contains(pSkill)) {
                relevance += 0.5;
            }

            // Check skill token and name overlaps
            int matchedSkillsCount = 0;
            for (String reqSkill : allRequiredSkillNames) {
                if (pTitle.contains(reqSkill) || pDesc.contains(reqSkill)) {
                    matchedSkillsCount++;
                } else {
                    String[] tokens = reqSkill.split("\\s+");
                    int tokenMatches = 0;
                    for (String t : tokens) {
                        if (t.length() > 3 && (pTitle.contains(t) || pDesc.contains(t))) {
                            tokenMatches++;
                        }
                    }
                    if (tokenMatches > 0 && tokenMatches >= (tokens.length / 2)) {
                        matchedSkillsCount++;
                    }
                }
            }

            if (!allRequiredSkillNames.isEmpty()) {
                relevance += (0.5 * ((double) matchedSkillsCount / (double) allRequiredSkillNames.size()));
            }

            if (relevance >= 0.25) {
                ProjectDto dto = new ProjectDto();
                dto.setId(p.getId());
                dto.setTitle(p.getTitle());
                dto.setDescription(p.getDescription());
                dto.setDifficulty(p.getDifficulty() != null ? p.getDifficulty().name() : "INTERMEDIATE");
                dto.setEstimatedHours(p.getEstimatedHours() != null ? p.getEstimatedHours() : 15.0);
                dto.setDeliverables(p.getDeliverables());
                dto.setRubric(p.getRubric());
                dto.setPrimarySkillName(p.getPrimarySkill() != null ? p.getPrimarySkill().getName() : targetRole);
                dto.setSkills(p.getPrimarySkill() != null ? List.of(p.getPrimarySkill().getName()) : List.of());
                dto.setGithubTemplateUrl(p.getGithubTemplateUrl() != null ? p.getGithubTemplateUrl() : "https://github.com");
                dto.setIsAiGenerated(false);
                dto.setRoadmapPhase("Core Architecture Milestone");
                dto.setScore(Math.min(98.0, 80.0 + (relevance * 20.0)));
                dto.setExplanation("Curated milestone directly validating " + dto.getPrimarySkillName() + " competency for " + targetRole + ".");
                matchedProjects.add(dto);
            }
        }

        // 2. If database projects are insufficient (< 2), dynamically synthesize structured projects via AI / Knowledge Generator
        if (matchedProjects.size() < 2) {
            List<ProjectDto> generated = aiServiceClient.generateProjects(
                    targetRole,
                    profile.getCareerGoal(),
                    profile.getExperienceLevel() != null ? profile.getExperienceLevel().name() : "INTERMEDIATE",
                    new ArrayList<>(allRequiredSkillNames),
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

        // Sort descending by score
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
                profile.getTargetRole() != null ? profile.getTargetRole() : "Engineering Specialist",
                profile.getCareerGoal(),
                profile.getExperienceLevel() != null ? profile.getExperienceLevel().name() : "INTERMEDIATE",
                skillNames,
                gapNames,
                List.of(),
                customTopic
        );
    }
}
