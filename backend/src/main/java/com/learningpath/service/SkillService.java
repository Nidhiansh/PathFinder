package com.learningpath.service;

import com.learningpath.dto.SkillDto;
import com.learningpath.dto.SkillGapDto;
import com.learningpath.dto.UserSkillDto;
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
    private SkillPrerequisiteRepository prerequisiteRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private AuthService authService;

    public List<SkillDto> getAllSkills() {
        return skillRepository.findAll().stream().map(this::mapToSkillDto).collect(Collectors.toList());
    }

    public List<SkillGapDto> calculateSkillGaps() {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        String targetRole = profile.getTargetRole() != null && !profile.getTargetRole().trim().isEmpty() 
                ? profile.getTargetRole() : "Software Engineer";
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
            String category = skill != null ? skill.getCategory().name() : "CORE_CS";

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
            if (skill != null) {
                List<SkillPrerequisite> prereqs = prerequisiteRepository.findBySkillId(skill.getId());
                for (SkillPrerequisite sp : prereqs) {
                    int prereqLevel = currentProficiencies.getOrDefault(sp.getPrerequisiteSkill().getName().toLowerCase(), 0);
                    if (prereqLevel < 50) {
                        unsatisfiedPrereqs.add(sp.getPrerequisiteSkill().getName() + " (" + prereqLevel + "%)");
                    }
                }
            }
            dto.setUnsatisfiedPrerequisites(unsatisfiedPrereqs);
            gapList.add(dto);
        }

        // 2. Include any active user skills for custom/unanticipated domains
        for (UserSkill us : userSkills) {
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

    public Map<String, Integer> getRoleSkillRequirements(String role) {
        Map<String, Integer> reqs = new LinkedHashMap<>();
        if (role == null || role.trim().isEmpty()) {
            role = "General Engineering Specialist";
        }
        String lower = role.toLowerCase();

        if (lower.contains("rag") || lower.contains("generative") || lower.contains("llm") || lower.contains("langchain") || lower.contains("embedding")) {
            reqs.put("Prompt Engineering & LLM APIs", 85);
            reqs.put("Vector Databases & Embeddings", 90);
            reqs.put("RAG Architecture & LangChain", 90);
            reqs.put("Python Programming", 85);
            reqs.put("Chunking, Reranking & Retrieval Optimization", 80);
            reqs.put("LLM Evaluation & Guardrails", 80);
            reqs.put("Model Deployment & FastAPI", 70);
            reqs.put("Deep Learning & PyTorch", 70);
        } else if (lower.contains("backend") || lower.contains("java") || lower.contains("spring")) {
            reqs.put("Java", 85);
            reqs.put("Object-Oriented Programming (OOP)", 80);
            reqs.put("Data Structures & Algorithms", 75);
            reqs.put("SQL & Relational Databases", 75);
            reqs.put("Spring Boot", 80);
            reqs.put("RESTful APIs", 85);
            reqs.put("Spring Data JPA & Hibernate", 75);
            reqs.put("Spring Security & JWT", 70);
            reqs.put("Docker & Containers", 65);
            reqs.put("System Design & Microservices", 70);
        } else if (lower.contains("flutter") || lower.contains("dart") || lower.contains("mobile")) {
            reqs.put("Dart Programming", 85);
            reqs.put("Flutter Framework & Widgets", 85);
            reqs.put("State Management (Riverpod/Bloc)", 80);
            reqs.put("Mobile Navigation & Routing", 75);
            reqs.put("REST API Integration & Local Storage", 80);
            reqs.put("Cross-Platform App Deployment", 70);
        } else if (lower.contains("blockchain") || lower.contains("solidity") || lower.contains("web3") || lower.contains("crypto") || lower.contains("smart contract")) {
            reqs.put("Solidity Programming", 85);
            reqs.put("Smart Contracts & EVM", 85);
            reqs.put("Web3.js & Ethers.js", 80);
            reqs.put("DeFi & Token Standards", 75);
            reqs.put("Security Auditing & Hardhat", 75);
        } else if (lower.contains("vision") || lower.contains("opencv") || lower.contains("image processing")) {
            reqs.put("Python Programming", 85);
            reqs.put("OpenCV Image Processing", 85);
            reqs.put("Convolutional Neural Networks (CNNs)", 80);
            reqs.put("Object Detection & YOLO", 80);
            reqs.put("PyTorch Vision Models", 75);
        } else if (lower.contains("data engineering") || lower.contains("etl") || lower.contains("spark") || lower.contains("kafka")) {
            reqs.put("Python Programming", 85);
            reqs.put("SQL & Relational Databases", 85);
            reqs.put("Apache Spark & Distributed Computing", 85);
            reqs.put("Kafka & Event Streaming", 80);
            reqs.put("Data Warehousing & ETL Pipelines", 80);
            reqs.put("Airflow Orchestration", 75);
        } else if (lower.contains("security") || lower.contains("cybersecurity") || lower.contains("ethical hacking")) {
            reqs.put("Networking Fundamentals & TCP/IP", 85);
            reqs.put("Linux Systems & Shell Scripting", 85);
            reqs.put("Web Application Security (OWASP Top 10)", 85);
            reqs.put("Cryptography Fundamentals", 80);
            reqs.put("Penetration Testing & Network Scanning", 75);
        } else if (lower.contains("fullstack") || lower.contains("full-stack") || lower.contains("react") || lower.contains("frontend") || lower.contains("web")) {
            reqs.put("JavaScript (ES6+)", 85);
            reqs.put("React.js", 80);
            reqs.put("Node.js & Express", 75);
            reqs.put("SQL & Relational Databases", 70);
            reqs.put("RESTful APIs", 85);
            reqs.put("Git & Version Control", 80);
            reqs.put("Docker & Containers", 60);
            reqs.put("System Design & Microservices", 65);
        } else if (lower.contains("devops") || lower.contains("cloud") || lower.contains("kubernetes") || lower.contains("k8s")) {
            reqs.put("Docker & Containers", 85);
            reqs.put("Cloud Infrastructure & Kubernetes", 85);
            reqs.put("Git & Version Control", 80);
            reqs.put("System Design & Microservices", 75);
        } else if (lower.contains("ai") || lower.contains("machine learning") || lower.contains("data science")) {
            reqs.put("Python Programming", 90);
            reqs.put("Data Structures & Algorithms", 75);
            reqs.put("Mathematics & Statistics for ML", 80);
            reqs.put("NumPy & Pandas", 85);
            reqs.put("Scikit-Learn", 80);
            reqs.put("Deep Learning & PyTorch", 75);
            reqs.put("Model Deployment & FastAPI", 70);
            reqs.put("SQL & Relational Databases", 65);
            reqs.put("Docker & Containers", 65);
        } else {
            reqs.put("Programming Fundamentals", 80);
            reqs.put("Data Structures & Algorithms", 75);
            reqs.put("RESTful APIs", 75);
            reqs.put("Git & Version Control", 75);
            reqs.put("System Design & Architecture", 70);
        }
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
