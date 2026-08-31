package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.*;
import com.learningpath.exception.BadRequestException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    @Autowired
    private LearningPathRepository pathRepository;

    @Autowired
    private LearningPhaseRepository phaseRepository;

    @Autowired
    private LearningPathItemRepository itemRepository;

    @Autowired
    private LearningResourceRepository resourceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillPrerequisiteRepository prerequisiteRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private LearnerProfileService learnerProfileService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private AuthService authService;

    public LearningPathDto getActiveRoadmap() {
        User user = authService.getCurrentAuthenticatedUser();
        LearningPath path = pathRepository.findByUserIdAndStatus(user.getId(), PathStatus.ACTIVE)
                .orElseGet(() -> generatePersonalizedRoadmap(user));

        return mapToPathDto(path);
    }

    @Transactional
    public LearningPathDto generateRoadmapForCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        return mapToPathDto(generatePersonalizedRoadmap(user));
    }

    @Transactional
    public LearningPath generatePersonalizedRoadmap(User user) {
        // 1. Archive previous active paths for this user
        List<LearningPath> existingPaths = pathRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (LearningPath p : existingPaths) {
            if (p.getStatus() == PathStatus.ACTIVE) {
                p.setStatus(PathStatus.ARCHIVED);
                pathRepository.save(p);
            }
        }

        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseGet(() -> new LearnerProfile(user, user.getUsername(), "Engineering Specialist", "Personalized Engineering Growth"));

        String targetRole = profile.getTargetRole() != null && !profile.getTargetRole().trim().isEmpty() 
                ? profile.getTargetRole() : (profile.getCareerGoal() != null && !profile.getCareerGoal().trim().isEmpty() ? profile.getCareerGoal() : "Engineering Specialist");

        LearningPath path = new LearningPath(user, targetRole + " Mastery Roadmap", targetRole);
        path = pathRepository.save(path);

        // 2. Fetch active skills for this learner
        List<UserSkill> activeUserSkills = userSkillRepository.findByProfileIdAndIsActiveTrue(profile.getId());
        
        // If no active skills exist yet, resolve initial core skills from role requirements
        if (activeUserSkills.isEmpty()) {
            Map<String, Integer> defaultReqs = skillService.getRoleSkillRequirements(targetRole);
            for (Map.Entry<String, Integer> entry : defaultReqs.entrySet()) {
                Skill skill = learnerProfileService.resolveOrCreateSkill(entry.getKey());
                UserSkill us = new UserSkill(profile, skill, 0, false, true, SkillProficiencySource.NOT_ASSESSED);
                activeUserSkills.add(userSkillRepository.save(us));
            }
        }

        // 3. Build graph of skills and resolve topological dependency depth
        Map<Long, UserSkill> skillMap = new LinkedHashMap<>();
        Map<Long, Skill> entityMap = new LinkedHashMap<>();
        for (UserSkill us : activeUserSkills) {
            skillMap.put(us.getSkill().getId(), us);
            entityMap.put(us.getSkill().getId(), us.getSkill());
        }

        // Adjacency graph for active skills
        Map<Long, Set<Long>> prereqGraph = new HashMap<>();
        Map<Long, Set<Long>> outgoingGraph = new HashMap<>();
        Map<Long, Integer> inDegree = new HashMap<>();

        for (Long skillId : skillMap.keySet()) {
            prereqGraph.put(skillId, new HashSet<>());
            outgoingGraph.put(skillId, new HashSet<>());
            inDegree.put(skillId, 0);
        }

        // Query database prerequisites for active skills
        for (Long skillId : skillMap.keySet()) {
            List<SkillPrerequisite> prereqs = prerequisiteRepository.findBySkillId(skillId);
            for (SkillPrerequisite sp : prereqs) {
                Long prereqId = sp.getPrerequisiteSkill().getId();
                if (skillMap.containsKey(prereqId)) {
                    prereqGraph.get(skillId).add(prereqId);
                    outgoingGraph.get(prereqId).add(skillId);
                }
            }
        }

        // Apply topological tier ordering based on category semantics
        for (Long skillId : skillMap.keySet()) {
            if (prereqGraph.get(skillId).isEmpty()) {
                SkillCategory cat = entityMap.get(skillId).getCategory();
                if (cat == SkillCategory.FRAMEWORK || cat == SkillCategory.DATABASE) {
                    for (Long otherId : skillMap.keySet()) {
                        if (entityMap.get(otherId).getCategory() == SkillCategory.LANGUAGE && !otherId.equals(skillId)) {
                            prereqGraph.get(skillId).add(otherId);
                            outgoingGraph.get(otherId).add(skillId);
                            break;
                        }
                    }
                } else if (cat == SkillCategory.ARCHITECTURE || cat == SkillCategory.DEVOPS) {
                    for (Long otherId : skillMap.keySet()) {
                        if ((entityMap.get(otherId).getCategory() == SkillCategory.FRAMEWORK || entityMap.get(otherId).getCategory() == SkillCategory.DATABASE) && !otherId.equals(skillId)) {
                            prereqGraph.get(skillId).add(otherId);
                            outgoingGraph.get(otherId).add(skillId);
                            break;
                        }
                    }
                }
            }
        }

        for (Long skillId : skillMap.keySet()) {
            inDegree.put(skillId, prereqGraph.get(skillId).size());
        }

        // 4. Topological Sort & Dependency Depth Assignment with Cycle Protection
        Queue<Long> queue = new LinkedList<>();
        Map<Long, Integer> depthMap = new HashMap<>();

        for (Long skillId : skillMap.keySet()) {
            if (inDegree.get(skillId) == 0) {
                queue.add(skillId);
                depthMap.put(skillId, 0);
            }
        }

        List<Long> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long curr = queue.poll();
            topoOrder.add(curr);
            int currDepth = depthMap.get(curr);

            for (Long neighbor : outgoingGraph.get(curr)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                depthMap.put(neighbor, Math.max(depthMap.getOrDefault(neighbor, 0), currDepth + 1));
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // Cycle fallback: append any unvisited skills
        for (Long skillId : skillMap.keySet()) {
            if (!topoOrder.contains(skillId)) {
                topoOrder.add(skillId);
                depthMap.put(skillId, depthMap.values().stream().max(Integer::compareTo).orElse(0) + 1);
            }
        }

        // 5. Dynamic Grouping of Skills into Topological Phases
        Map<Integer, List<Long>> depthGroups = new TreeMap<>();
        for (Long skillId : topoOrder) {
            int d = depthMap.getOrDefault(skillId, 0);
            depthGroups.computeIfAbsent(d, k -> new ArrayList<>()).add(skillId);
        }

        List<List<Long>> phaseSkillGroups = new ArrayList<>();
        if (depthGroups.size() <= 4 && depthGroups.size() >= 2) {
            phaseSkillGroups.addAll(depthGroups.values());
        } else {
            int totalSkills = topoOrder.size();
            int numPhases = Math.min(5, Math.max(2, (int) Math.ceil((double) totalSkills / 2.5)));
            int chunkSize = (int) Math.ceil((double) totalSkills / numPhases);
            for (int i = 0; i < totalSkills; i += chunkSize) {
                phaseSkillGroups.add(topoOrder.subList(i, Math.min(totalSkills, i + chunkSize)));
            }
        }

        // 6. Synthesize Learning Phases, Items, and Dynamic Titles
        List<LearningResource> allDbResources = resourceRepository.findAll();
        List<Project> allDbProjects = projectRepository.findAll();
        List<Assessment> allDbAssessments = assessmentRepository.findAll();

        boolean previousPhaseMastered = true;

        for (int phaseIdx = 0; phaseIdx < phaseSkillGroups.size(); phaseIdx++) {
            int phaseNum = phaseIdx + 1;
            List<Long> skillsInPhase = phaseSkillGroups.get(phaseIdx);

            String skillNamesSummary = skillsInPhase.stream()
                    .map(id -> entityMap.get(id).getName())
                    .collect(Collectors.joining(", "));

            String phaseSuffix = phaseNum == 1 ? "Foundations & Core Principles" 
                    : phaseNum == phaseSkillGroups.size() ? "Advanced Architecture & Production Capstone"
                    : "Core Competency & Applied Engineering";

            String phaseTitle = "Phase " + phaseNum + ": " + summarizeSkillList(skillsInPhase, entityMap) + " - " + phaseSuffix;
            String phaseDescription = "Mastery and verification of " + skillNamesSummary + ".";

            PhaseStatus status = (phaseNum == 1 || previousPhaseMastered) ? PhaseStatus.AVAILABLE : PhaseStatus.LOCKED;
            LearningPhase phase = phaseRepository.save(new LearningPhase(path, phaseNum, phaseTitle, phaseDescription, status, 0.0));

            double phaseHours = 0.0;
            int itemOrder = 1;
            boolean phaseSkillsAllProficient = true;

            for (Long skillId : skillsInPhase) {
                UserSkill us = skillMap.get(skillId);
                Skill skill = entityMap.get(skillId);
                int currentProf = us.getProficiencyLevel() != null ? us.getProficiencyLevel() : 0;
                int reqProf = 75;
                int gap = Math.max(0, reqProf - currentProf);

                if (currentProf < 70) {
                    phaseSkillsAllProficient = false;
                }

                // A. Matched Learning Resource(s) for this skill
                List<LearningResource> matchedResources = findMatchingResourcesForSkill(skill, allDbResources);
                if (!matchedResources.isEmpty()) {
                    for (LearningResource lr : matchedResources) {
                        double hours = lr.getEstimatedHours() != null ? lr.getEstimatedHours() : 10.0;
                        if (currentProf >= 75) hours = Math.max(2.0, Math.round(hours * 0.4));
                        LearningPathItem item = new LearningPathItem(
                                phase,
                                ItemType.RESOURCE,
                                lr.getTitle(),
                                lr.getUrl(),
                                hours,
                                itemOrder++,
                                (phaseNum == 1 && itemOrder <= 2) ? ItemStatus.AVAILABLE : ItemStatus.LOCKED
                        );
                        item.setResource(lr);
                        item.setRecommendationScore(lr.getQualityScore() != null ? lr.getQualityScore() * 100.0 : 90.0);
                        item.setRecommendationReason("Curriculum module covering " + skill.getName() + " for " + targetRole);
                        itemRepository.save(item);
                        phaseHours += hours;
                    }
                } else {
                    double hours = currentProf >= 75 ? 4.0 : 12.0;
                    String docUrl = deriveDomainDocUrl(skill.getName());
                    LearningPathItem item = new LearningPathItem(
                            phase,
                            ItemType.RESOURCE,
                            "Mastery Guide: " + skill.getName(),
                            docUrl,
                            hours,
                            itemOrder++,
                            (phaseNum == 1 && itemOrder <= 2) ? ItemStatus.AVAILABLE : ItemStatus.LOCKED
                    );
                    item.setRecommendationScore(92.0);
                    item.setRecommendationReason("Targeted technical documentation and official guide for " + skill.getName());
                    itemRepository.save(item);
                    phaseHours += hours;
                }

                // B. Matched Milestone Project for this skill
                Project matchedProject = findMatchingProjectForSkill(skill, allDbProjects);
                if (matchedProject != null) {
                    double pHours = matchedProject.getEstimatedHours() != null ? matchedProject.getEstimatedHours() : 15.0;
                    LearningPathItem projItem = new LearningPathItem(
                            phase,
                            ItemType.PROJECT,
                            "Project: " + matchedProject.getTitle(),
                            matchedProject.getGithubTemplateUrl() != null ? matchedProject.getGithubTemplateUrl() : "https://github.com",
                            pHours,
                            itemOrder++,
                            ItemStatus.LOCKED
                    );
                    projItem.setProject(matchedProject);
                    projItem.setRecommendationScore(95.0);
                    projItem.setRecommendationReason("Milestone capstone project to demonstrate mastery of " + skill.getName());
                    itemRepository.save(projItem);
                    phaseHours += pHours;
                } else if (gap > 20) {
                    double pHours = 14.0;
                    LearningPathItem projItem = new LearningPathItem(
                            phase,
                            ItemType.PROJECT,
                            "Hands-On Build: " + skill.getName() + " Implementation Project",
                            "https://github.com",
                            pHours,
                            itemOrder++,
                            ItemStatus.LOCKED
                    );
                    projItem.setRecommendationScore(94.0);
                    projItem.setRecommendationReason("Practical portfolio milestone build verifying " + skill.getName() + " skills");
                    itemRepository.save(projItem);
                    phaseHours += pHours;
                }

                // C. Matched Diagnostic Assessment Checkpoint
                Assessment matchedAssessment = findMatchingAssessmentForSkill(skill, allDbAssessments);
                if (matchedAssessment != null) {
                    LearningPathItem assessItem = new LearningPathItem(
                            phase,
                            ItemType.ASSESSMENT,
                            "Checkpoint Assessment: " + matchedAssessment.getTitle(),
                            "/assessments/" + matchedAssessment.getId(),
                            0.5,
                            itemOrder++,
                            ItemStatus.LOCKED
                    );
                    assessItem.setAssessment(matchedAssessment);
                    assessItem.setRecommendationScore(98.0);
                    assessItem.setRecommendationReason("Diagnostic checkpoint validating " + skill.getName() + " proficiency");
                    itemRepository.save(assessItem);
                    phaseHours += 0.5;
                }
            }

            phase.setEstimatedHours(phaseHours);
            phaseRepository.save(phase);
            previousPhaseMastered = phaseSkillsAllProficient;
        }

        // 7. Recalculate Path Total Hours and Timeline Weeks
        double totalHours = 0.0;
        List<LearningPhase> savedPhases = phaseRepository.findByLearningPathIdOrderByPhaseNumberAsc(path.getId());
        for (LearningPhase lp : savedPhases) {
            totalHours += lp.getEstimatedHours() != null ? lp.getEstimatedHours() : 0.0;
        }

        path.setTotalEstimatedHours(totalHours);
        return pathRepository.save(path);
    }

    private List<LearningResource> findMatchingResourcesForSkill(Skill skill, List<LearningResource> allResources) {
        String skillNameLower = skill.getName().toLowerCase();
        List<LearningResource> matches = new ArrayList<>();

        for (LearningResource lr : allResources) {
            // 1. Direct relation via resource_skills
            if (lr.getResourceSkills() != null) {
                boolean linkedSkill = lr.getResourceSkills().stream()
                        .anyMatch(rs -> rs.getSkill() != null && 
                                (rs.getSkill().getId().equals(skill.getId()) || 
                                 rs.getSkill().getName().equalsIgnoreCase(skill.getName())));
                if (linkedSkill) {
                    matches.add(lr);
                    continue;
                }
            }

            // 2. Precise title containment
            String titleLower = lr.getTitle().toLowerCase();
            if (titleLower.contains(skillNameLower)) {
                matches.add(lr);
            }
        }
        return matches.stream().distinct().limit(3).collect(Collectors.toList());
    }

    private Project findMatchingProjectForSkill(Skill skill, List<Project> allProjects) {
        String skillNameLower = skill.getName().toLowerCase();
        for (Project p : allProjects) {
            if (p.getPrimarySkill() != null && 
                (p.getPrimarySkill().getId().equals(skill.getId()) || 
                 p.getPrimarySkill().getName().equalsIgnoreCase(skill.getName()))) {
                return p;
            }
            String titleLower = p.getTitle().toLowerCase();
            if (titleLower.contains(skillNameLower)) {
                return p;
            }
        }
        return null;
    }

    private Assessment findMatchingAssessmentForSkill(Skill skill, List<Assessment> allAssessments) {
        if (skill.getName() == null) return null;
        String sLower = skill.getName().toLowerCase();

        for (Assessment a : allAssessments) {
            String aTitle = a.getTitle().toLowerCase();
            String aSkill = a.getSkill() != null ? a.getSkill().getName().toLowerCase() : "";

            if (sLower.contains("java") && !sLower.contains("javascript") && (aTitle.contains("java") || aSkill.contains("java"))) return a;
            if (sLower.contains("spring") && (aTitle.contains("spring") || aSkill.contains("spring"))) return a;
            if ((sLower.contains("sql") || sLower.contains("relational") || sLower.contains("database")) && (aTitle.contains("sql") || aSkill.contains("sql"))) return a;
            if (sLower.contains("python") && (aTitle.contains("python") || aSkill.contains("python"))) return a;
            if ((sLower.contains("rag") || sLower.contains("langchain") || sLower.contains("retrieval")) && (aTitle.contains("rag") || aTitle.contains("generative") || aSkill.contains("rag"))) return a;
            if (sLower.contains("react") && (aTitle.contains("react") || aSkill.contains("react"))) return a;
            if ((sLower.contains("kubernetes") || sLower.contains("k8s")) && (aTitle.contains("kubernetes") || aSkill.contains("kubernetes"))) return a;
            if (sLower.contains("docker") && (aTitle.contains("docker") || aSkill.contains("docker"))) return a;
        }
        return null;
    }

    private String deriveDomainDocUrl(String skillName) {
        String s = skillName.toLowerCase();
        if (s.contains("flutter")) return "https://docs.flutter.dev";
        if (s.contains("dart")) return "https://dart.dev/guides";
        if (s.contains("kubernetes") || s.contains("k8s")) return "https://kubernetes.io/docs/";
        if (s.contains("solidity") || s.contains("blockchain") || s.contains("ethereum")) return "https://docs.soliditylang.org/";
        if (s.contains("opencv") || s.contains("computer vision")) return "https://docs.opencv.org/";
        if (s.contains("spark")) return "https://spark.apache.org/docs/latest/";
        if (s.contains("kafka")) return "https://kafka.apache.org/documentation/";
        if (s.contains("security") || s.contains("cybersecurity") || s.contains("owasp")) return "https://owasp.org/www-project-top-ten/";
        if (s.contains("rust")) return "https://doc.rust-lang.org/book/";
        if (s.contains("go") || s.contains("golang")) return "https://go.dev/doc/";
        if (s.contains("rag") || s.contains("langchain")) return "https://python.langchain.com/docs/tutorials/rag/";
        if (s.contains("vector")) return "https://github.com/pgvector/pgvector";
        if (s.contains("python")) return "https://docs.python.org/3/";
        if (s.contains("react")) return "https://react.dev";
        if (s.contains("docker")) return "https://docs.docker.com/";
        return "https://devdocs.io";
    }

    private String summarizeSkillList(List<Long> skillIds, Map<Long, Skill> entityMap) {
        if (skillIds.isEmpty()) return "Competency";
        if (skillIds.size() == 1) return entityMap.get(skillIds.get(0)).getName();
        return entityMap.get(skillIds.get(0)).getName() + " & " + entityMap.get(skillIds.get(1)).getName();
    }

    @Transactional
    public LearningPathDto updateItemStatus(Long itemId, UpdateItemStatusRequest request) {
        LearningPathItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path item not found: " + itemId));

        ItemStatus newStatus;
        try {
            newStatus = ItemStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }

        item.setStatus(newStatus);
        if (newStatus == ItemStatus.COMPLETED) {
            item.setCompletedAt(LocalDateTime.now());
        }
        itemRepository.save(item);

        // Check if all items in current phase are completed, then unlock next phase
        LearningPhase currentPhase = item.getLearningPhase();
        List<LearningPathItem> phaseItems = itemRepository.findByLearningPhaseIdOrderByOrderIndexAsc(currentPhase.getId());
        
        // Unlock next item in same phase if available
        for (int i = 0; i < phaseItems.size(); i++) {
            if (phaseItems.get(i).getId().equals(itemId) && (i + 1) < phaseItems.size()) {
                LearningPathItem nextItem = phaseItems.get(i + 1);
                if (nextItem.getStatus() == ItemStatus.LOCKED) {
                    nextItem.setStatus(ItemStatus.AVAILABLE);
                    itemRepository.save(nextItem);
                }
            }
        }

        boolean allCompleted = phaseItems.stream().allMatch(i -> i.getStatus() == ItemStatus.COMPLETED);
        if (allCompleted) {
            currentPhase.setStatus(PhaseStatus.COMPLETED);
            phaseRepository.save(currentPhase);

            // Unlock next phase
            LearningPath path = currentPhase.getLearningPath();
            List<LearningPhase> allPhases = phaseRepository.findByLearningPathIdOrderByPhaseNumberAsc(path.getId());
            for (int i = 0; i < allPhases.size(); i++) {
                if (allPhases.get(i).getId().equals(currentPhase.getId()) && (i + 1) < allPhases.size()) {
                    LearningPhase nextPhase = allPhases.get(i + 1);
                    nextPhase.setStatus(PhaseStatus.AVAILABLE);
                    phaseRepository.save(nextPhase);

                    // Unlock first item of next phase
                    List<LearningPathItem> nextPhaseItems = itemRepository.findByLearningPhaseIdOrderByOrderIndexAsc(nextPhase.getId());
                    if (!nextPhaseItems.isEmpty()) {
                        nextPhaseItems.get(0).setStatus(ItemStatus.AVAILABLE);
                        itemRepository.save(nextPhaseItems.get(0));
                    }
                }
            }
        } else {
            currentPhase.setStatus(PhaseStatus.IN_PROGRESS);
            phaseRepository.save(currentPhase);
        }

        return mapToPathDto(currentPhase.getLearningPath());
    }

    @Transactional
    public LearningPathDto recalculateRoadmapTimeline(RecalculateTimeRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (request.getWeeklyHours() != null && request.getWeeklyHours() > 0) {
            profile.setWeeklyHours(request.getWeeklyHours());
            profileRepository.save(profile);
        }

        LearningPath path = pathRepository.findByUserIdAndStatus(user.getId(), PathStatus.ACTIVE)
                .orElseGet(() -> generatePersonalizedRoadmap(user));

        return mapToPathDto(path);
    }

    private LearningPathDto mapToPathDto(LearningPath path) {
        LearnerProfile profile = profileRepository.findByUser(path.getUser()).orElse(null);
        int weeklyHours = profile != null && profile.getWeeklyHours() != null ? profile.getWeeklyHours() : 10;

        List<LearningPhase> phases = phaseRepository.findByLearningPathIdOrderByPhaseNumberAsc(path.getId());
        List<LearningPhaseDto> phaseDtos = new ArrayList<>();

        int totalItemsCount = 0;
        int completedItemsCount = 0;

        for (LearningPhase phase : phases) {
            List<LearningPathItem> items = itemRepository.findByLearningPhaseIdOrderByOrderIndexAsc(phase.getId());
            List<LearningPathItemDto> itemDtos = new ArrayList<>();
            int phaseCompleted = 0;

            for (LearningPathItem item : items) {
                totalItemsCount++;
                if (item.getStatus() == ItemStatus.COMPLETED) {
                    completedItemsCount++;
                    phaseCompleted++;
                }

                LearningPathItemDto itemDto = new LearningPathItemDto();
                itemDto.setId(item.getId());
                itemDto.setItemType(item.getItemType().name());
                itemDto.setTitle(item.getTitle());
                itemDto.setUrl(item.getUrl());
                itemDto.setEstimatedHours(item.getEstimatedHours());
                itemDto.setOrderIndex(item.getOrderIndex());
                itemDto.setStatus(item.getStatus().name());
                itemDto.setIsLocked(item.getStatus() == ItemStatus.LOCKED);
                itemDto.setRecommendationScore(item.getRecommendationScore());
                itemDto.setRecommendationReason(item.getRecommendationReason());

                if (item.getResource() != null) {
                    itemDto.setReferenceId(item.getResource().getId());
                    itemDto.setDescription(item.getResource().getDescription());
                    itemDto.setPlatform(item.getResource().getPlatform());
                    itemDto.setDifficulty(item.getResource().getDifficulty().name());
                } else if (item.getProject() != null) {
                    itemDto.setReferenceId(item.getProject().getId());
                    itemDto.setDescription(item.getProject().getDescription());
                    itemDto.setDifficulty(item.getProject().getDifficulty().name());
                } else if (item.getAssessment() != null) {
                    itemDto.setReferenceId(item.getAssessment().getId());
                    itemDto.setDescription(item.getAssessment().getDescription());
                    itemDto.setDifficulty(item.getAssessment().getDifficulty().name());
                }

                itemDtos.add(itemDto);
            }

            LearningPhaseDto phaseDto = new LearningPhaseDto();
            phaseDto.setId(phase.getId());
            phaseDto.setPhaseNumber(phase.getPhaseNumber());
            phaseDto.setTitle(phase.getTitle());
            phaseDto.setDescription(phase.getDescription());
            phaseDto.setStatus(phase.getStatus().name());
            phaseDto.setEstimatedHours(phase.getEstimatedHours());
            phaseDto.setProgressPercentage(items.isEmpty() ? 0.0 : Math.round((double) phaseCompleted / items.size() * 100.0));
            phaseDto.setItems(itemDtos);

            phaseDtos.add(phaseDto);
        }

        double overallProgress = totalItemsCount > 0 ? Math.round((double) completedItemsCount / totalItemsCount * 100.0) : 0.0;
        int estimatedWeeks = (int) Math.ceil(path.getTotalEstimatedHours() / (double) weeklyHours);

        LearningPathDto dto = new LearningPathDto();
        dto.setId(path.getId());
        dto.setTitle(path.getTitle());
        dto.setTargetRole(path.getTargetRole());
        dto.setStatus(path.getStatus().name());
        dto.setTotalEstimatedHours(path.getTotalEstimatedHours());
        dto.setEstimatedWeeks(Math.max(1, estimatedWeeks));
        dto.setOverallProgressPercentage(overallProgress);
        dto.setTotalItems(totalItemsCount);
        dto.setCompletedItems(completedItemsCount);
        dto.setPhases(phaseDtos);

        return dto;
    }
}
