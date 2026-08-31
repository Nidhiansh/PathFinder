package com.learningpath.service;

import com.learningpath.dto.ExtractGoalRequest;
import com.learningpath.dto.ExtractGoalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public ExtractGoalResponse analyzeGoal(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> requestBody = Map.of("prompt", prompt);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ExtractGoalResponse response = restTemplate.postForObject(
                    aiServiceUrl + "/ai/analyze-goal",
                    request,
                    ExtractGoalResponse.class
            );
            if (response != null && response.getTargetRole() != null) {
                return response;
            }
        } catch (Exception e) {
            // Fallback to internal NLP goal parser
        }
        return fallbackAnalyzeGoal(prompt);
    }

    public Map<String, Object> generateChatResponse(String message, Map<String, Object> context) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("context", context);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            Map response = restTemplate.postForObject(
                    aiServiceUrl + "/ai/chat",
                    request,
                    Map.class
            );
            if (response != null && response.containsKey("reply")) {
                return response;
            }
        } catch (Exception e) {
            // Fallback to internal conversational logic
        }
        return fallbackChatResponse(message, context);
    }

    private ExtractGoalResponse fallbackAnalyzeGoal(String prompt) {
        ExtractGoalResponse resp = new ExtractGoalResponse();
        resp.setCareerGoal(prompt);

        String lower = prompt.toLowerCase();
        List<String> extractedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        // 1. RAG & Generative AI check (Priority over generic AI)
        if (lower.matches(".*\\b(rag|retrieval augmented|llm|generative ai|genai|langchain|vector db|vector database|embedding|embeddings|gpt)\\b.*")) {
            resp.setTargetRole("Generative AI & RAG Engineer");
            extractedSkills.addAll(List.of("Python Programming", "Prompt Engineering & LLM APIs"));
            missingSkills.addAll(List.of("Vector Databases & Embeddings", "RAG Architecture & LangChain", "Chunking, Reranking & Retrieval Optimization", "LLM Evaluation & Guardrails", "Model Deployment & FastAPI"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Hands-On (10 hours/week)");
            resp.setAiSummary("Specialized engineering roadmap covering Retrieval-Augmented Generation (RAG), vector embeddings, chunking strategies, LangChain/LlamaIndex pipelines, and production LLM evaluation.");
        } else if (lower.matches(".*\\b(flutter|dart|mobile app|mobile development|android|ios)\\b.*")) {
            resp.setTargetRole("Flutter Mobile Developer");
            extractedSkills.addAll(List.of("Dart Programming", "Mobile Navigation & Routing"));
            missingSkills.addAll(List.of("Flutter Framework & Widgets", "State Management (Riverpod/Bloc)", "REST API Integration & Local Storage", "Cross-Platform App Deployment"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Hands-On (10 hours/week)");
            resp.setAiSummary("Comprehensive mobile app development path focusing on Dart programming, Flutter reactive widgets, state management architectures, and cross-platform mobile deployment.");
        } else if (lower.matches(".*\\b(blockchain|solidity|web3|crypto|smart contract|ethereum)\\b.*")) {
            resp.setTargetRole("Blockchain & Smart Contract Engineer");
            extractedSkills.addAll(List.of("Solidity Programming", "Smart Contracts & EVM"));
            missingSkills.addAll(List.of("Web3.js & Ethers.js", "DeFi & Token Standards", "Security Auditing & Hardhat"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Intensive (12 hours/week)");
            resp.setAiSummary("Decentralized application engineering covering Solidity smart contracts, EVM mechanics, Web3 frontend integration, and cryptographic security auditing.");
        } else if (lower.matches(".*\\b(computer vision|vision|opencv|image processing|yolo)\\b.*")) {
            resp.setTargetRole("Computer Vision Engineer");
            extractedSkills.addAll(List.of("Python Programming", "OpenCV Image Processing"));
            missingSkills.addAll(List.of("Convolutional Neural Networks (CNNs)", "Object Detection & YOLO", "PyTorch Vision Models"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Hands-On (10 hours/week)");
            resp.setAiSummary("Specialized vision engineering curriculum covering image transformations, OpenCV filtering, deep convolutional neural networks, and real-time object detection.");
        } else if (lower.matches(".*\\b(data engineering|etl|spark|kafka|data warehouse|data pipeline)\\b.*")) {
            resp.setTargetRole("Data Engineer");
            extractedSkills.addAll(List.of("Python Programming", "SQL & Relational Databases"));
            missingSkills.addAll(List.of("Apache Spark & Distributed Computing", "Kafka & Event Streaming", "Data Warehousing & ETL Pipelines", "Airflow Orchestration"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Structured (10 hours/week)");
            resp.setAiSummary("Scalable data systems engineering covering distributed compute with Apache Spark, real-time event streaming with Kafka, and automated data pipelines.");
        } else if (lower.matches(".*\\b(cybersecurity|security|ethical hacking|penetration testing|infosec)\\b.*")) {
            resp.setTargetRole("Cybersecurity Engineer");
            extractedSkills.addAll(List.of("Networking Fundamentals & TCP/IP", "Linux Systems & Shell Scripting"));
            missingSkills.addAll(List.of("Web Application Security (OWASP Top 10)", "Cryptography Fundamentals", "Penetration Testing & Network Scanning"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Hands-On (10 hours/week)");
            resp.setAiSummary("Information security curriculum covering network protocols, defensive architecture, OWASP web application security, and penetration testing.");
        } else if (lower.matches(".*\\b(backend|java|spring|microservices|microservice)\\b.*")) {
            resp.setTargetRole("Backend Java Developer");
            extractedSkills.addAll(List.of("Java", "SQL & Relational Databases", "Object-Oriented Programming (OOP)"));
            missingSkills.addAll(List.of("Spring Boot", "RESTful APIs", "Spring Security & JWT", "Spring Data JPA & Hibernate", "Docker & Containers", "System Design & Microservices"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Moderate (10 hours/week)");
            resp.setAiSummary("Focused on modern enterprise Java backend engineering with Spring Boot microservices ecosystem, relational data modeling, and distributed architecture.");
        } else if (lower.matches(".*\\b(fullstack|full-stack|react|frontend|javascript|typescript|node|web dev|web)\\b.*")) {
            resp.setTargetRole("Full Stack Developer");
            extractedSkills.addAll(List.of("JavaScript (ES6+)", "Git & Version Control"));
            missingSkills.addAll(List.of("React.js", "Node.js & Express", "SQL & Relational Databases", "RESTful APIs", "Docker & Containers"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Intensive (12 hours/week)");
            resp.setAiSummary("Extracted goal for modern full-stack web application development combining React frontend interfaces with Node/Express/PostgreSQL backend services.");
        } else if (lower.matches(".*\\b(devops|cloud|kubernetes|docker|aws|ci/cd|k8s)\\b.*")) {
            resp.setTargetRole("DevOps & Cloud Engineer");
            extractedSkills.addAll(List.of("Docker & Containers", "Git & Version Control"));
            missingSkills.addAll(List.of("Cloud Infrastructure & Kubernetes", "System Design & Microservices"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Hands-On (10 hours/week)");
            resp.setAiSummary("Modern cloud infrastructure, container orchestration with Kubernetes, and scalable systems.");
        } else if (lower.matches(".*\\b(machine learning|deep learning|data science|neural|pytorch|scikit)\\b.*") || 
                   (lower.matches(".*\\bai\\b.*") && !lower.contains("artificial intelligence only"))) {
            resp.setTargetRole("AI / ML Engineer");
            extractedSkills.addAll(List.of("Python Programming", "Mathematics & Statistics for ML", "NumPy & Pandas"));
            missingSkills.addAll(List.of("Scikit-Learn", "Deep Learning & PyTorch", "Model Deployment & FastAPI", "SQL & Relational Databases"));
            resp.setEstimatedMonths(8);
            resp.setLearningPace("Standard (10 hours/week)");
            resp.setAiSummary("Identified AI/ML path covering mathematical foundations, classical machine learning algorithms, deep neural architectures, and model deployment.");
        } else {
            resp.setTargetRole("General Software Specialist");
            extractedSkills.addAll(List.of("Programming Fundamentals", "Data Structures & Algorithms", "Git & Version Control"));
            missingSkills.addAll(List.of("RESTful APIs", "System Design & Architecture"));
            resp.setEstimatedMonths(6);
            resp.setLearningPace("Self-Paced (8 hours/week)");
            resp.setAiSummary("Engineering roadmap focused on foundational software architecture and clean design principles.");
        }

        // 2. Experience Level Detection
        if (lower.matches(".*\\b(senior|expert|advanced|lead|architect)\\b.*")) {
            resp.setExperienceLevel("ADVANCED");
        } else if (lower.matches(".*\\b(beginner|starting|no experience|zero experience|novice|only know|just know|basics only|from scratch|new)\\b.*") ||
                   lower.matches(".*\\bknow\\b.*\\bonly\\b.*")) {
            resp.setExperienceLevel("BEGINNER");
        } else {
            resp.setExperienceLevel("INTERMEDIATE");
        }

        resp.setExtractedSkills(extractedSkills);
        resp.setMissingSkills(missingSkills);
        return resp;
    }

    private Map<String, Object> fallbackChatResponse(String message, Map<String, Object> context) {
        String lower = message.toLowerCase();
        Map<String, Object> result = new HashMap<>();

        if (lower.contains("rag") || lower.contains("langchain") || lower.contains("vector")) {
            result.put("reply", "Retrieval-Augmented Generation (RAG) combines semantic vector search with LLMs to provide grounded, citation-backed answers from private documentation without hallucinations.");
            result.put("suggestedAction", "VIEW_RESOURCE");
            result.put("quickReplies", List.of("What is hybrid search?", "Show me the RAG project", "What should I learn next?"));
        } else if (lower.contains("what should i learn next") || lower.contains("what to learn")) {
            result.put("reply", "Based on your current active roadmap, you should proceed with your available milestone topics and take the diagnostic checkpoint quiz when ready.");
            result.put("suggestedAction", "START_NEXT_ITEM");
            result.put("quickReplies", List.of("Show me the project for this phase", "Take the checkpoint assessment", "Explain recommendations"));
        } else {
            result.put("reply", "I'm your AI Learning Copilot. I'm actively monitoring your active profile, skill gaps, roadmap milestones, and assessments. Ask me what to learn next, why a topic is sequenced, or to adapt your weekly pace.");
            result.put("suggestedAction", "GENERAL");
            result.put("quickReplies", List.of("What should I learn next?", "Explain my roadmap sequence", "I only have 5 hours this week"));
        }

        return result;
    }

    public List<com.learningpath.dto.ProjectDto> generateProjects(
            String targetRole,
            String careerGoal,
            String experienceLevel,
            List<String> skills,
            List<String> skillGaps,
            List<String> roadmapPhases,
            String customTopic
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("target_role", targetRole);
            requestBody.put("career_goal", careerGoal);
            requestBody.put("experience_level", experienceLevel != null ? experienceLevel : "INTERMEDIATE");
            requestBody.put("skills", skills != null ? skills : List.of());
            requestBody.put("skill_gaps", skillGaps != null ? skillGaps : List.of());
            requestBody.put("roadmap_phases", roadmapPhases != null ? roadmapPhases : List.of());
            requestBody.put("custom_topic", customTopic);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            List<Map<String, Object>> response = restTemplate.postForObject(
                    aiServiceUrl + "/ai/generate-projects",
                    request,
                    List.class
            );

            if (response != null && !response.isEmpty()) {
                List<com.learningpath.dto.ProjectDto> dtoList = new ArrayList<>();
                for (Map<String, Object> item : response) {
                    com.learningpath.dto.ProjectDto dto = new com.learningpath.dto.ProjectDto();
                    dto.setId(((Number) item.getOrDefault("id", 20001)).longValue());
                    dto.setTitle((String) item.getOrDefault("title", "Hands-On Milestone Project"));
                    dto.setDescription((String) item.getOrDefault("description", ""));
                    dto.setDifficulty((String) item.getOrDefault("difficulty", "INTERMEDIATE"));
                    dto.setEstimatedHours(((Number) item.getOrDefault("estimated_hours", 15.0)).doubleValue());
                    dto.setDeliverables((String) item.getOrDefault("deliverables", "Deliverables"));
                    dto.setRubric((String) item.getOrDefault("rubric", "Rubric"));
                    dto.setPrimarySkillName((String) item.getOrDefault("primary_skill", "Software Engineering"));
                    dto.setGithubTemplateUrl((String) item.getOrDefault("github_template_url", "https://github.com"));
                    dto.setIsAiGenerated(true);
                    dto.setRoadmapPhase((String) item.getOrDefault("roadmap_phase", "Active Milestone"));
                    dto.setScore(((Number) item.getOrDefault("score", 92.0)).doubleValue());
                    dto.setExplanation((String) item.getOrDefault("explanation", "Recommended portfolio project."));
                    
                    Object sList = item.get("skills");
                    if (sList instanceof List) {
                        dto.setSkills((List<String>) sList);
                    } else {
                        dto.setSkills(List.of(dto.getPrimarySkillName()));
                    }
                    dtoList.add(dto);
                }
                return dtoList;
            }
        } catch (Exception e) {
            // Fallback to internal deterministic generator
        }
        return fallbackGenerateProjects(targetRole, careerGoal, experienceLevel, skills, roadmapPhases);
    }

    private List<com.learningpath.dto.ProjectDto> fallbackGenerateProjects(
            String targetRole,
            String careerGoal,
            String experienceLevel,
            List<String> skills,
            List<String> roadmapPhases
    ) {
        String roleLower = (targetRole != null ? targetRole : "Software Engineer").toLowerCase();
        List<com.learningpath.dto.ProjectDto> list = new ArrayList<>();
        String primarySkill = skills != null && !skills.isEmpty() ? skills.get(0) : "Software Architecture";
        String secondarySkill = skills != null && skills.size() > 1 ? skills.get(1) : primarySkill;

        if (roleLower.contains("flutter") || roleLower.contains("mobile") || roleLower.contains("dart")) {
            com.learningpath.dto.ProjectDto p1 = new com.learningpath.dto.ProjectDto();
            p1.setId(20001L);
            p1.setTitle("Flutter Responsive Component Library & Dynamic Theme Engine");
            p1.setDescription("Construct a clean, modular UI component library in Flutter with Material 3, dynamic light/dark theming, custom canvas widgets, and responsive layout adapters.");
            p1.setDifficulty("BEGINNER");
            p1.setEstimatedHours(12.0);
            p1.setDeliverables("Reusable widget package, storybook catalog, and tablet/mobile adaptive layouts.");
            p1.setRubric("Widget composition (40%), responsive breakpoint handling (30%), code clean architecture (30%).");
            p1.setPrimarySkillName("Flutter Framework & Widgets");
            p1.setSkills(List.of("Dart Programming", "Flutter Framework & Widgets"));
            p1.setGithubTemplateUrl("https://github.com/flutter/samples");
            p1.setRoadmapPhase("Phase 1: Foundation & UI Architecture");
            p1.setIsAiGenerated(true);
            p1.setScore(96.5);
            p1.setExplanation("Directly targets foundational Flutter widget composition and Dart reactive programming patterns.");
            list.add(p1);

            com.learningpath.dto.ProjectDto p2 = new com.learningpath.dto.ProjectDto();
            p2.setId(20002L);
            p2.setTitle("Real-Time Personal Expense & Portfolio Tracker with Riverpod");
            p2.setDescription("Build an asynchronous, offline-first personal expense and portfolio manager utilizing Riverpod state management, SQLite local caching, and REST API sync.");
            p2.setDifficulty("INTERMEDIATE");
            p2.setEstimatedHours(18.0);
            p2.setDeliverables("State-managed mobile app, interactive financial charts, SQLite migration scripts, and mock API service.");
            p2.setRubric("Riverpod provider architecture (35%), offline sync robustness (35%), UI performance (30%).");
            p2.setPrimarySkillName("State Management (Riverpod/Bloc)");
            p2.setSkills(List.of("Flutter Framework & Widgets", "State Management (Riverpod/Bloc)", "Local Database Storage"));
            p2.setGithubTemplateUrl("https://github.com/flutter/gallery");
            p2.setRoadmapPhase("Phase 2: State Management & Persistence");
            p2.setIsAiGenerated(true);
            p2.setScore(94.0);
            p2.setExplanation("Essential intermediate milestone to master reactive state management and local device data persistence.");
            list.add(p2);

            com.learningpath.dto.ProjectDto p3 = new com.learningpath.dto.ProjectDto();
            p3.setId(20003L);
            p3.setTitle("Production-Ready Cross-Platform Flutter Capstone with CI/CD");
            p3.setDescription("Architect a production-grade multi-platform mobile application featuring OAuth2 authentication, push notifications, comprehensive unit/widget tests, and automated Fastlane deployment pipelines.");
            p3.setDifficulty("ADVANCED");
            p3.setEstimatedHours(24.0);
            p3.setDeliverables("End-to-end mobile app, automated test suite (>80% coverage), GitHub Actions workflow, and release bundles.");
            p3.setRubric("Clean architecture separation (35%), test coverage & CI/CD reliability (35%), security best practices (30%).");
            p3.setPrimarySkillName("Cross-Platform App Deployment");
            p3.setSkills(List.of("Flutter Framework & Widgets", "Cross-Platform App Deployment", "Testing & CI/CD"));
            p3.setGithubTemplateUrl("https://github.com/flutter/flutter");
            p3.setRoadmapPhase("Phase 3: Production Deployment & Capstone");
            p3.setIsAiGenerated(true);
            p3.setScore(91.5);
            p3.setExplanation("Capstone project validating complete mobile engineering lifecycle from architecture to production CI/CD.");
            list.add(p3);
        } else if (roleLower.contains("kubernetes") || roleLower.contains("devops") || roleLower.contains("cloud") || roleLower.contains("k8s")) {
            com.learningpath.dto.ProjectDto p1 = new com.learningpath.dto.ProjectDto();
            p1.setId(20011L);
            p1.setTitle("Multi-Service Containerization & Microservice Mesh with Docker Compose");
            p1.setDescription("Containerize a polyglot microservices system with multi-stage Dockerfiles, private container registries, environment isolation, and health check monitoring.");
            p1.setDifficulty("INTERMEDIATE");
            p1.setEstimatedHours(14.0);
            p1.setDeliverables("Optimized Dockerfiles (<100MB images), docker-compose multi-service orchestrations, and secret management configs.");
            p1.setRubric("Image optimization (40%), container security non-root execution (30%), networking reliability (30%).");
            p1.setPrimarySkillName("Docker & Containers");
            p1.setSkills(List.of("Docker & Containers", "Microservices Networking"));
            p1.setGithubTemplateUrl("https://github.com/docker/awesome-compose");
            p1.setRoadmapPhase("Phase 1: Containerization Foundations");
            p1.setIsAiGenerated(true);
            p1.setScore(95.0);
            p1.setExplanation("Establishes production container packaging standards before orchestrating with Kubernetes.");
            list.add(p1);

            com.learningpath.dto.ProjectDto p2 = new com.learningpath.dto.ProjectDto();
            p2.setId(20012L);
            p2.setTitle("High-Availability Kubernetes Cluster Orchestration with Helm & Ingress");
            p2.setDescription("Deploy a distributed cloud-native application on Kubernetes featuring custom Helm charts, NGINX Ingress controllers, TLS cert-manager, Horizontal Pod Autoscaling (HPA), and ConfigMaps.");
            p2.setDifficulty("ADVANCED");
            p2.setEstimatedHours(20.0);
            p2.setDeliverables("Modular Helm chart repository, K8s manifests, HPA load testing script, and ingress configuration.");
            p2.setRubric("Helm templating quality (35%), autoscaling responsiveness under load (35%), cluster resilience (30%).");
            p2.setPrimarySkillName("Cloud Infrastructure & Kubernetes");
            p2.setSkills(List.of("Cloud Infrastructure & Kubernetes", "Docker & Containers", "Helm Package Manager"));
            p2.setGithubTemplateUrl("https://github.com/kubernetes/examples");
            p2.setRoadmapPhase("Phase 2: Cluster Orchestration & Ingress");
            p2.setIsAiGenerated(true);
            p2.setScore(96.0);
            p2.setExplanation("Validates hands-on cloud orchestration, deployment strategies, and traffic management.");
            list.add(p2);
        } else if (roleLower.contains("blockchain") || roleLower.contains("solidity") || roleLower.contains("smart contract") || roleLower.contains("web3")) {
            com.learningpath.dto.ProjectDto p1 = new com.learningpath.dto.ProjectDto();
            p1.setId(20021L);
            p1.setTitle("Secure ERC-20 & Fractional Asset Escrow Smart Contract Suite");
            p1.setDescription("Develop and audit an EVM-compliant smart contract suite in Solidity implementing OpenZeppelin standards, reentrancy guards, multi-party escrow, and custom events.");
            p1.setDifficulty("INTERMEDIATE");
            p1.setEstimatedHours(14.0);
            p1.setDeliverables("Solidity smart contracts, Hardhat automated unit test suite with 100% branch coverage, and gas optimization benchmarks.");
            p1.setRubric("Security audit compliance (45%), gas consumption efficiency (30%), test coverage (25%).");
            p1.setPrimarySkillName("Solidity Programming");
            p1.setSkills(List.of("Solidity Programming", "Smart Contracts & EVM"));
            p1.setGithubTemplateUrl("https://github.com/OpenZeppelin/openzeppelin-contracts");
            p1.setRoadmapPhase("Phase 1: Smart Contract Architecture");
            p1.setIsAiGenerated(true);
            p1.setScore(95.5);
            p1.setExplanation("Teaches fundamental EVM execution model, secure contract patterns, and gas efficiency.");
            list.add(p1);

            com.learningpath.dto.ProjectDto p2 = new com.learningpath.dto.ProjectDto();
            p2.setId(20022L);
            p2.setTitle("Full-Stack Web3 Decentralized App (DApp) with Ethers.js & IPFS");
            p2.setDescription("Construct an end-to-end decentralized application featuring MetaMask wallet connectivity, state synchronization via Ethers.js, decentralized storage on IPFS, and contract interaction guards.");
            p2.setDifficulty("ADVANCED");
            p2.setEstimatedHours(20.0);
            p2.setDeliverables("Web3 frontend interface, Ethers.js integration layer, IPFS pinning gateway integration, and deployment scripts for Sepolia testnet.");
            p2.setRubric("Wallet state handling (35%), decentralized data integrity (35%), user UX during transaction latency (30%).");
            p2.setPrimarySkillName("Web3.js & Ethers.js");
            p2.setSkills(List.of("Solidity Programming", "Web3.js & Ethers.js", "Smart Contracts & EVM"));
            p2.setGithubTemplateUrl("https://github.com/dappuniversity/starter_kit");
            p2.setRoadmapPhase("Phase 2: Full-Stack Web3 Integration");
            p2.setIsAiGenerated(true);
            p2.setScore(93.5);
            p2.setExplanation("Connects smart contract backend with modern reactive frontend interfaces.");
            list.add(p2);
        } else if (roleLower.contains("vision") || roleLower.contains("opencv") || roleLower.contains("image")) {
            com.learningpath.dto.ProjectDto p1 = new com.learningpath.dto.ProjectDto();
            p1.setId(20031L);
            p1.setTitle("Real-Time Image Processing & Morphological Defect Inspection Pipeline");
            p1.setDescription("Build an automated quality inspection system with OpenCV implementing Gaussian filtering, Canny edge detection, contour analysis, and real-time video stream ingestion.");
            p1.setDifficulty("INTERMEDIATE");
            p1.setEstimatedHours(14.0);
            p1.setDeliverables("Python OpenCV processing pipeline, automated feature extraction engine, and interactive benchmark dashboard.");
            p1.setRubric("Image transformation accuracy (40%), frames-per-second throughput (30%), robust noise filtering (30%).");
            p1.setPrimarySkillName("OpenCV Image Processing");
            p1.setSkills(List.of("Python Programming", "OpenCV Image Processing"));
            p1.setGithubTemplateUrl("https://github.com/opencv/opencv");
            p1.setRoadmapPhase("Phase 1: Feature Extraction & Filters");
            p1.setIsAiGenerated(true);
            p1.setScore(95.0);
            p1.setExplanation("Validates core digital image processing operations and OpenCV API fundamentals.");
            list.add(p1);

            com.learningpath.dto.ProjectDto p2 = new com.learningpath.dto.ProjectDto();
            p2.setId(20032L);
            p2.setTitle("Deep Learning Object Detection & Multi-Object Tracking System with YOLO");
            p2.setDescription("Train and deploy a high-accuracy object detection and spatial tracking system using YOLO / PyTorch, featuring bounding box regression, ByteTrack multi-target association, and Streamlit visualization.");
            p2.setDifficulty("ADVANCED");
            p2.setEstimatedHours(22.0);
            p2.setDeliverables("Trained PyTorch model weights, real-time video tracking inference loop, evaluation mAP benchmarks, and containerized deployment.");
            p2.setRubric("Mean Average Precision (mAP) score (40%), inference latency optimization (30%), tracking stability (30%).");
            p2.setPrimarySkillName("Object Detection & YOLO");
            p2.setSkills(List.of("OpenCV Image Processing", "Convolutional Neural Networks (CNNs)", "Object Detection & YOLO"));
            p2.setGithubTemplateUrl("https://github.com/ultralytics/ultralytics");
            p2.setRoadmapPhase("Phase 2: Deep Vision & Object Tracking");
            p2.setIsAiGenerated(true);
            p2.setScore(96.0);
            p2.setExplanation("Capstone vision application integrating modern neural networks with real-time video streams.");
            list.add(p2);
        } else if (roleLower.contains("data engineer") || roleLower.contains("spark") || roleLower.contains("kafka")) {
            com.learningpath.dto.ProjectDto p1 = new com.learningpath.dto.ProjectDto();
            p1.setId(20041L);
            p1.setTitle("Real-Time Distributed Event Streaming Engine with Apache Kafka & Spark");
            p1.setDescription("Design an end-to-end distributed streaming data pipeline ingesting high-volume event streams through Kafka, processing micro-batches with Spark Streaming, and persisting deduplicated records.");
            p1.setDifficulty("INTERMEDIATE");
            p1.setEstimatedHours(16.0);
            p1.setDeliverables("Kafka producer/consumer configurations, PySpark structured streaming job scripts, and schema evolution handlers.");
            p1.setRubric("Stream processing latency (40%), exactly-once semantics compliance (30%), schema validation (30%).");
            p1.setPrimarySkillName("Apache Spark & Distributed Computing");
            p1.setSkills(List.of("SQL & Relational Databases", "Apache Spark & Distributed Computing", "Kafka & Event Streaming"));
            p1.setGithubTemplateUrl("https://github.com/apache/spark");
            p1.setRoadmapPhase("Phase 1: Streaming Ingestion & Distributed Processing");
            p1.setIsAiGenerated(true);
            p1.setScore(96.0);
            p1.setExplanation("Fundamental data engineering build validating real-time streaming architectures.");
            list.add(p1);

            com.learningpath.dto.ProjectDto p2 = new com.learningpath.dto.ProjectDto();
            p2.setId(20042L);
            p2.setTitle("Enterprise Delta Lakehouse Architecture & Automated Airflow Orchestration");
            p2.setDescription("Construct an enterprise medallion architecture (Bronze, Silver, Gold layers) using Delta Lake, PySpark transformations, data quality testing with Great Expectations, and scheduled DAG orchestration via Apache Airflow.");
            p2.setDifficulty("ADVANCED");
            p2.setEstimatedHours(22.0);
            p2.setDeliverables("Airflow DAG definitions, Delta Lake schema definitions, automated data quality test suite, and analytical SQL view models.");
            p2.setRubric("Data pipeline idempotency (35%), data quality test coverage (35%), partition & indexing performance (30%).");
            p2.setPrimarySkillName("Data Modeling & Lakehouse Architecture");
            p2.setSkills(List.of("Apache Spark & Distributed Computing", "Kafka & Event Streaming", "Data Modeling & Lakehouse Architecture"));
            p2.setGithubTemplateUrl("https://github.com/apache/airflow");
            p2.setRoadmapPhase("Phase 2: Lakehouse Modeling & Orchestration");
            p2.setIsAiGenerated(true);
            p2.setScore(94.0);
            p2.setExplanation("Production capstone covering end-to-end batch/streaming data lakehouse architecture.");
            list.add(p2);
        } else if (roleLower.contains("rag") || roleLower.contains("generative") || roleLower.contains("llm")) {
            com.learningpath.dto.ProjectDto p1 = new com.learningpath.dto.ProjectDto();
            p1.setId(20051L);
            p1.setTitle("Enterprise Document QA RAG System with Vector Search & LangChain");
            p1.setDescription("Construct an end-to-end RAG application that ingests multi-format documents, performs semantic chunking, indexes embeddings into ChromaDB, and synthesizes answers with source citations.");
            p1.setDifficulty("INTERMEDIATE");
            p1.setEstimatedHours(18.0);
            p1.setDeliverables("Python LangChain pipeline, ChromaDB vector store, FastAPI querying endpoint, and Streamlit interactive UI.");
            p1.setRubric("Retrieval accuracy (35%), hallucination mitigation (35%), API performance (30%).");
            p1.setPrimarySkillName("RAG Architecture & LangChain");
            p1.setSkills(List.of("Prompt Engineering & LLM APIs", "Vector Databases & Embeddings", "RAG Architecture & LangChain"));
            p1.setGithubTemplateUrl("https://github.com/langchain-ai/rag-from-scratch");
            p1.setRoadmapPhase("Phase 1: Semantic Ingestion & RAG Pipeline");
            p1.setIsAiGenerated(true);
            p1.setScore(98.0);
            p1.setExplanation("Directly validates semantic vector retrieval and context-augmented synthesis.");
            list.add(p1);

            com.learningpath.dto.ProjectDto p2 = new com.learningpath.dto.ProjectDto();
            p2.setId(20052L);
            p2.setTitle("Multi-Source Semantic Knowledge Base with Hybrid BM25 & Vector Retrieval");
            p2.setDescription("Implement a two-stage hybrid retrieval system with Cohere cross-encoder reranking, contextual compression, and automated RAG Triad evaluation using Ragas.");
            p2.setDifficulty("ADVANCED");
            p2.setEstimatedHours(22.0);
            p2.setDeliverables("Hybrid retriever implementation, reranking pipeline, evaluation benchmarks (Faithfulness, Context Precision), and FastAPI service.");
            p2.setRubric("Retrieval recall enhancement (40%), evaluation score rigor (30%), latency optimization (30%).");
            p2.setPrimarySkillName("Chunking, Reranking & Retrieval Optimization");
            p2.setSkills(List.of("Vector Databases & Embeddings", "RAG Architecture & LangChain", "Chunking, Reranking & Retrieval Optimization"));
            p2.setGithubTemplateUrl("https://github.com/explodinggradients/ragas");
            p2.setRoadmapPhase("Phase 2: Hybrid Retrieval & Evaluation");
            p2.setIsAiGenerated(true);
            p2.setScore(96.5);
            p2.setExplanation("Advanced RAG engineering addressing precision reranking and continuous evaluation.");
            list.add(p2);
        } else {
            String domainLabel = targetRole.replace("Developer", "").replace("Engineer", "").trim();
            com.learningpath.dto.ProjectDto p1 = new com.learningpath.dto.ProjectDto();
            p1.setId(20091L);
            p1.setTitle(domainLabel + " Foundational Application & Component Prototype");
            p1.setDescription("Construct a modular, clean-architecture prototype in " + primarySkill + " establishing core data models, error handling strategies, and interactive interfaces.");
            p1.setDifficulty("INTERMEDIATE");
            p1.setEstimatedHours(14.0);
            p1.setDeliverables("Working repository in " + primarySkill + ", unit tests, and design documentation.");
            p1.setRubric("Architecture modularity (40%), test coverage (30%), clean coding conventions (30%).");
            p1.setPrimarySkillName(primarySkill);
            p1.setSkills(skills != null && skills.size() >= 2 ? skills.subList(0, 2) : List.of(primarySkill));
            p1.setGithubTemplateUrl("https://github.com");
            p1.setRoadmapPhase("Phase 1: Foundation & Core Architecture");
            p1.setIsAiGenerated(true);
            p1.setScore(92.0);
            p1.setExplanation("Foundational milestone project to cement core " + primarySkill + " proficiencies.");
            list.add(p1);

            com.learningpath.dto.ProjectDto p2 = new com.learningpath.dto.ProjectDto();
            p2.setId(20092L);
            p2.setTitle("Production-Grade " + domainLabel + " Enterprise System with Testing & CI/CD");
            p2.setDescription("Architect a production-grade application for " + targetRole + " incorporating persistence, asynchronous processing, security controls, and automated deployment pipelines.");
            p2.setDifficulty("ADVANCED");
            p2.setEstimatedHours(22.0);
            p2.setDeliverables("Full-stack " + domainLabel + " application, automated test suite, containerization configs, and deployment scripts.");
            p2.setRubric("System reliability (35%), security best practices (35%), test automation (30%).");
            p2.setPrimarySkillName(secondarySkill);
            p2.setSkills(skills != null && skills.size() >= 3 ? skills.subList(0, 3) : List.of(primarySkill, secondarySkill));
            p2.setGithubTemplateUrl("https://github.com");
            p2.setRoadmapPhase("Phase 2: Production Capstone");
            p2.setIsAiGenerated(true);
            p2.setScore(94.5);
            p2.setExplanation("Advanced capstone validating end-to-end competency for " + targetRole + ".");
            list.add(p2);
        }

        return list;
    }
}

