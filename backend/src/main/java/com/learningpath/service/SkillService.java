package com.learningpath.service;

import com.learningpath.dto.SkillDto;
import com.learningpath.dto.SkillGapDto;
import com.learningpath.dto.UserSkillDto;
import com.learningpath.dto.ExtractGoalResponse;
import com.learningpath.entity.*;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillAliasRepository aliasRepository;

    @Autowired
    private SkillRelationRepository relationRepository;

    @Autowired
    private SkillPrerequisiteRepository prerequisiteRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private AiServiceClient aiServiceClient;

    public List<SkillDto> getAllSkills() {
        return skillRepository.findAll().stream().map(this::mapToSkillDto).collect(Collectors.toList());
    }

    public List<SkillGapDto> calculateSkillGaps() {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        String targetRole = profile.getTargetRole() != null && !profile.getTargetRole().trim().isEmpty() 
                ? profile.getTargetRole() : (profile.getCareerGoal() != null && !profile.getCareerGoal().trim().isEmpty() ? profile.getCareerGoal() : "Engineering Specialist");

        Map<String, Integer> requiredSkillsForRole = getRoleSkillRequirements(targetRole);

        // Fetch active skills for the current goal context
        List<UserSkill> userSkills = userSkillRepository.findByProfileIdAndIsActiveTrue(profile.getId());
        if (userSkills.isEmpty()) {
            userSkills = userSkillRepository.findByProfileId(profile.getId());
        }

        Map<String, Integer> currentProficiencies = new HashMap<>();
        for (UserSkill us : userSkills) {
            currentProficiencies.put(us.getSkill().getName().toLowerCase(), us.getProficiencyLevel());
        }

        List<SkillGapDto> gapList = new ArrayList<>();
        Set<String> processedSkills = new HashSet<>();

        // 1. Process defined role requirements
        for (Map.Entry<String, Integer> entry : requiredSkillsForRole.entrySet()) {
            String skillName = entry.getKey();
            processedSkills.add(skillName.toLowerCase());
            int requiredLevel = entry.getValue();
            int currentLevel = currentProficiencies.getOrDefault(skillName.toLowerCase(), 0);
            int gap = Math.max(0, requiredLevel - currentLevel);

            Skill skill = skillRepository.findByNameIgnoreCase(skillName).orElse(null);
            String category = skill != null && skill.getCategory() != null ? skill.getCategory().name() : "CORE_CS";

            SkillGapDto dto = new SkillGapDto();
            dto.setSkillName(skillName);
            dto.setCategory(category);
            dto.setCurrentProficiency(currentLevel);
            dto.setRequiredProficiency(requiredLevel);
            dto.setGap(gap);

            if (currentLevel >= requiredLevel) {
                dto.setStatus("MASTERED");
            } else if (currentLevel > 0) {
                dto.setStatus("IN_PROGRESS");
            } else {
                dto.setStatus("MISSING");
            }

            // Check unsatisfied prerequisites
            List<String> unsatisfiedPrereqs = new ArrayList<>();
            boolean isPrereq = false;
            String reason = "Direct core competency required for " + targetRole + ".";
            String source = skill != null && skill.getExternalSource() != null ? skill.getExternalSource() + " Taxonomy" : "Knowledge Base";

            if (skill != null) {
                List<SkillPrerequisite> prereqs = prerequisiteRepository.findBySkillId(skill.getId());
                for (SkillPrerequisite sp : prereqs) {
                    int prereqLevel = currentProficiencies.getOrDefault(sp.getPrerequisiteSkill().getName().toLowerCase(), 0);
                    if (prereqLevel < 50) {
                        unsatisfiedPrereqs.add(sp.getPrerequisiteSkill().getName() + " (" + prereqLevel + "%)");
                    }
                }

                // Check if this skill is a prerequisite for another active skill
                List<SkillPrerequisite> dependentOnThis = prerequisiteRepository.findByPrerequisiteSkillId(skill.getId());
                if (!dependentOnThis.isEmpty()) {
                    for (SkillPrerequisite dp : dependentOnThis) {
                        if (requiredSkillsForRole.containsKey(dp.getSkill().getName())) {
                            isPrereq = true;
                            reason = "Validated foundational prerequisite for " + dp.getSkill().getName() + " in prerequisite DAG.";
                            break;
                        }
                    }
                }
            }

            dto.setUnsatisfiedPrerequisites(unsatisfiedPrereqs);
            dto.setSkillRole(isPrereq ? "REQUIRED_PREREQUISITE" : "DIRECT_CORE");
            dto.setReason(reason);
            dto.setSource(source);
            dto.setConfidence(0.94);
            gapList.add(dto);
        }

        // 2. Include any active user skills for custom/unanticipated domains
        for (UserSkill us : userSkills) {
            if (us.getIsActive() != null && !us.getIsActive()) continue;
            String sName = us.getSkill().getName();
            if (!processedSkills.contains(sName.toLowerCase())) {
                processedSkills.add(sName.toLowerCase());
                int currentLevel = us.getProficiencyLevel() != null ? us.getProficiencyLevel() : 0;
                int requiredLevel = 75;
                int gap = Math.max(0, requiredLevel - currentLevel);

                SkillGapDto dto = new SkillGapDto();
                dto.setSkillName(sName);
                dto.setCategory(us.getSkill().getCategory() != null ? us.getSkill().getCategory().name() : "CORE_CS");
                dto.setCurrentProficiency(currentLevel);
                dto.setRequiredProficiency(requiredLevel);
                dto.setGap(gap);
                dto.setStatus(currentLevel >= requiredLevel ? "MASTERED" : (currentLevel > 0 ? "IN_PROGRESS" : "MISSING"));
                dto.setUnsatisfiedPrerequisites(new ArrayList<>());
                dto.setSkillRole("DIRECT_CORE");
                dto.setReason("Active goal competency for " + targetRole + ".");
                dto.setSource(us.getSkill().getExternalSource() != null ? us.getSkill().getExternalSource() : "Inferred");
                dto.setConfidence(0.90);
                gapList.add(dto);
            }
        }

        // Sort: Missing/high gaps first
        gapList.sort((a, b) -> Integer.compare(b.getGap(), a.getGap()));
        return gapList;
    }

    @Transactional
    public UserSkillDto updateSkillProficiency(String skillName, int proficiency) {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillName));

        UserSkill userSkill = userSkillRepository.findByProfileIdAndSkillId(profile.getId(), skill.getId())
                .orElseGet(() -> new UserSkill(profile, skill, proficiency, false, true, SkillProficiencySource.USER_PROVIDED));

        userSkill.setProficiencyLevel(Math.min(100, Math.max(0, proficiency)));
        userSkill.setIsActive(true);
        userSkill.setSource(SkillProficiencySource.USER_PROVIDED);
        userSkill = userSkillRepository.save(userSkill);

        return mapToUserSkillDto(userSkill);
    }

    /**
     * Universal Knowledge-Grounded Skill Requirement Engine:
     * Resolves role/topic requirements by querying the canonical taxonomy,
     * alias index, and prerequisite DAG relations.
     * Zero domain-specific hardcoded switch-cases.
     */
    public Map<String, Integer> getRoleSkillRequirements(String role) {
        Map<String, Integer> reqs = new LinkedHashMap<>();
        if (role == null || role.trim().isEmpty()) {
            role = "General Engineering Specialist";
        }
        String cleanRole = role.trim();

        // 1. Check direct skill alias index
        Optional<SkillAlias> aliasOpt = aliasRepository.findByAliasIgnoreCase(cleanRole.toLowerCase());
        Skill matchedSkill = null;
        if (aliasOpt.isPresent()) {
            matchedSkill = aliasOpt.get().getCanonicalSkill();
        } else {
            // Check direct canonical name match
            Optional<Skill> directSkillOpt = skillRepository.findByNameIgnoreCase(cleanRole);
            if (directSkillOpt.isPresent()) {
                matchedSkill = directSkillOpt.get();
            }
        }

        if (matchedSkill != null) {
            // Add matched core skill
            reqs.put(matchedSkill.getName(), matchedSkill.getDifficultyLevel() == Difficulty.ADVANCED ? 85 : 80);

            // Traverse direct prerequisites in DAG
            List<SkillPrerequisite> prereqs = prerequisiteRepository.findBySkillId(matchedSkill.getId());
            for (SkillPrerequisite sp : prereqs) {
                reqs.put(sp.getPrerequisiteSkill().getName(), 75);
            }

            // Traverse essential core relations
            List<SkillRelation> relations = relationRepository.findByTargetSkillId(matchedSkill.getId());
            for (SkillRelation rel : relations) {
                if (rel.getRelationType() == SkillRelationType.PREREQUISITE || rel.getRelationType() == SkillRelationType.ESSENTIAL_CORE) {
                    reqs.put(rel.getSourceSkill().getName(), 75);
                }
            }

            // Add domain companions if career goal
            if (cleanRole.toLowerCase().contains("engineer") || cleanRole.toLowerCase().contains("developer") || cleanRole.toLowerCase().contains("specialist")) {
                String matchedDomain = matchedSkill.getDomain();
                List<Skill> domainSkills = skillRepository.findAll().stream()
                        .filter(s -> s.getDomain() != null && s.getDomain().equalsIgnoreCase(matchedDomain))
                        .toList();
                for (Skill ds : domainSkills) {
                    if (reqs.size() < 7 && !reqs.containsKey(ds.getName())) {
                        reqs.put(ds.getName(), ds.getDifficultyLevel() == Difficulty.ADVANCED ? 80 : 75);
                    }
                }
            }
            return reqs;
        }

        // 2. Delegate to AI Service / Knowledge Resolver for unindexed / novel requests
        try {
            ExtractGoalResponse analysis = aiServiceClient.analyzeGoal(cleanRole);
            if (analysis != null) {
                List<String> combined = new ArrayList<>();
                if (analysis.getMissingSkills() != null) combined.addAll(analysis.getMissingSkills());
                if (analysis.getCoreSkills() != null) {
                    for (String cs : analysis.getCoreSkills()) {
                        if (!combined.contains(cs)) combined.add(cs);
                    }
                }
                if (analysis.getPrerequisiteSkills() != null) {
                    for (String ps : analysis.getPrerequisiteSkills()) {
                        if (!combined.contains(ps)) combined.add(ps);
                    }
                }

                if (!combined.isEmpty()) {
                    for (String s : combined) {
                        reqs.put(s, 80);
                    }
                    return reqs;
                }
            }
        } catch (Exception e) {
            // Fall through to domain-agnostic default
        }

        // 3. Fallback: create dynamic requirement node for clean concept
        reqs.put(cleanRole, 80);
        reqs.put(cleanRole + " Foundations", 75);
        reqs.put("Applied " + cleanRole, 80);
        return reqs;
    }

    private SkillDto mapToSkillDto(Skill skill) {
        SkillDto dto = new SkillDto();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setCategory(skill.getCategory().name());
        dto.setDescription(skill.getDescription());
        dto.setDifficultyLevel(skill.getDifficultyLevel().name());

        List<SkillPrerequisite> prereqs = prerequisiteRepository.findBySkillId(skill.getId());
        List<String> prereqNames = prereqs.stream()
                .map(p -> p.getPrerequisiteSkill().getName())
                .collect(Collectors.toList());
        dto.setPrerequisites(prereqNames);
        return dto;
    }

    private UserSkillDto mapToUserSkillDto(UserSkill userSkill) {
        Skill skill = userSkill.getSkill();
        return new UserSkillDto(
                skill.getId(),
                skill.getName(),
                skill.getCategory().name(),
                userSkill.getProficiencyLevel(),
                userSkill.getIsVerified(),
                userSkill.getIsActive() != null ? userSkill.getIsActive() : true,
                userSkill.getSource() != null ? userSkill.getSource().name() : "USER_PROVIDED"
        );
    }
}
