package com.learningpath.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.entity.*;
import com.learningpath.repository.*;
import com.learningpath.service.RoadmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillAliasRepository aliasRepository;

    @Autowired
    private SkillRelationRepository relationRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillPrerequisiteRepository prerequisiteRepository;

    @Autowired
    private LearningResourceRepository resourceRepository;

    @Autowired
    private ResourceSkillRepository resourceSkillRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentQuestionRepository questionRepository;

    @Autowired
    private RoadmapService roadmapService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.core.env.Environment environment;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (skillRepository.count() > 0) {
            return; // Seed data already loaded
        }

        // =========================================================================
        // 1. CANONICAL SKILL TAXONOMY (ESCO & O*NET GROUNDED ACROSS DIVERSE DOMAINS)
        // =========================================================================

        // Domain: ALGORITHMS_CS
        Skill dp = skillRepository.save(new Skill("Dynamic Programming", SkillCategory.CORE_CS, "Optimizing recursive solutions via subproblem caching, state transitions, and tabular recurrence relations.", Difficulty.ADVANCED, "ESCO", "http://data.europa.eu/esco/skill/dp-001", "ALGORITHMS_CS"));
        Skill recursion = skillRepository.save(new Skill("Recursion & Backtracking", SkillCategory.CORE_CS, "Recursive base cases, call stack mechanics, exhaustive search, and branch pruning.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/rec-002", "ALGORITHMS_CS"));
        Skill memoization = skillRepository.save(new Skill("Memoization & Tabulation", SkillCategory.CORE_CS, "Top-down lookup tables vs bottom-up multi-dimensional state array transitions.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/memo-003", "ALGORITHMS_CS"));
        Skill complexity = skillRepository.save(new Skill("Time & Space Complexity (Big-O)", SkillCategory.CORE_CS, "Asymptotic analysis, recursive tree expansion, and memory auxiliary bounds.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/bigo-004", "ALGORITHMS_CS"));
        Skill arraysStrings = skillRepository.save(new Skill("Arrays & Data Structures", SkillCategory.CORE_CS, "Contiguous memory allocations, hashing structures, and pointer manipulation.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/arrays-005", "ALGORITHMS_CS"));

        // Domain: MATHEMATICS
        Skill limits = skillRepository.save(new Skill("Limits & Continuity", SkillCategory.CORE_CS, "Epsilon-delta definitions, one-sided limits, and function behavior at asymptotes.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/math-001", "MATHEMATICS"));
        Skill calculusDiff = skillRepository.save(new Skill("Calculus & Derivatives", SkillCategory.CORE_CS, "Differential calculus, product/chain rules, tangent slopes, and optimization.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/math-002", "MATHEMATICS"));
        Skill calculusInt = skillRepository.save(new Skill("Integral Calculus", SkillCategory.CORE_CS, "Definite/indefinite integrals, Riemann sums, substitution, and area under curves.", Difficulty.ADVANCED, "ESCO", "http://data.europa.eu/esco/skill/math-003", "MATHEMATICS"));
        Skill linearAlgebra = skillRepository.save(new Skill("Linear Algebra & Matrices", SkillCategory.CORE_CS, "Vector spaces, dot products, matrix transformations, eigenvalues, and SVD.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/math-004", "MATHEMATICS"));

        // Domain: CREATIVE_3D & DESIGN
        Skill meshModeling = skillRepository.save(new Skill("3D Modeling & Mesh Topology", SkillCategory.ARCHITECTURE, "Polygonal vertex modeling, edge loops, subdivision surfaces, and clean topology in Blender.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/3d-001", "CREATIVE_3D"));
        Skill uvTexturing = skillRepository.save(new Skill("UV Unwrapping & PBR Texturing", SkillCategory.ARCHITECTURE, "Unwrapping 2D UV seams, PBR shader nodes, roughness, normals, and metallic maps.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/3d-002", "CREATIVE_3D"));
        Skill lighting3D = skillRepository.save(new Skill("3D Lighting & Rendering", SkillCategory.ARCHITECTURE, "Cycles/EEVEE photorealistic rendering, three-point lighting, and HDRI world environments.", Difficulty.ADVANCED, "ESCO", "http://data.europa.eu/esco/skill/3d-003", "CREATIVE_3D"));

        // Domain: PHOTOGRAPHY
        Skill exposureTriangle = skillRepository.save(new Skill("Exposure Triangle (Aperture, Shutter Speed, ISO)", SkillCategory.ARCHITECTURE, "Mastering optical light capture, motion blur, sensor noise, and depth of field.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/photo-001", "PHOTOGRAPHY"));
        Skill rawEditing = skillRepository.save(new Skill("RAW Image Editing & Color Grading", SkillCategory.ARCHITECTURE, "Non-destructive RAW sensor curve adjustment, histogram balancing, and LUT color grading.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/photo-002", "PHOTOGRAPHY"));

        // Domain: AUDIO_MUSIC
        Skill daw = skillRepository.save(new Skill("Digital Audio Workstation (DAW)", SkillCategory.ARCHITECTURE, "Multi-track audio recording, MIDI routing, automation lanes, and session management.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/audio-001", "AUDIO_MUSIC"));
        Skill audioMixing = skillRepository.save(new Skill("Audio Mixing & Mastering", SkillCategory.ARCHITECTURE, "Parametric EQ balancing, dynamic multi-band compression, stereo imaging, and LUFS limiting.", Difficulty.ADVANCED, "ESCO", "http://data.europa.eu/esco/skill/audio-002", "AUDIO_MUSIC"));

        // Domain: FINANCE
        Skill finStatements = skillRepository.save(new Skill("Financial Statement Analysis", SkillCategory.ARCHITECTURE, "Analyzing Income Statements, Balance Sheets, and Cash Flow Statements for corporate health.", Difficulty.BEGINNER, "ONET", "13-2051.00", "FINANCE"));
        Skill finModeling = skillRepository.save(new Skill("Financial Modeling & Valuation (DCF)", SkillCategory.ARCHITECTURE, "Building Discounted Cash Flow models, WACC calculations, and three-statement financial projections.", Difficulty.ADVANCED, "ONET", "13-2051.00", "FINANCE"));

        // Domain: DEVOPS_CLOUD
        Skill docker = skillRepository.save(new Skill("Docker & Containers", SkillCategory.DEVOPS, "OCI container image building, multi-stage Dockerfiles, and compose networks.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/devops-001", "DEVOPS_CLOUD"));
        Skill kubernetes = skillRepository.save(new Skill("Kubernetes Orchestration", SkillCategory.DEVOPS, "Declarative Kubernetes cluster orchestration, Pods, Deployments, Services, and Ingress controllers.", Difficulty.ADVANCED, "ESCO", "http://data.europa.eu/esco/skill/devops-002", "DEVOPS_CLOUD"));
        Skill linux = skillRepository.save(new Skill("Linux & Shell Scripting", SkillCategory.CORE_CS, "POSIX system commands, bash automation, filesystem permissions, and process signals.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/devops-003", "DEVOPS_CLOUD"));

        // Domain: AI_ML & RAG
        Skill python = skillRepository.save(new Skill("Python Programming", SkillCategory.LANGUAGE, "Idiomatic Python scripting, list comprehensions, and data wrangling.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/py-001", "AI_ML"));
        Skill promptEngineering = skillRepository.save(new Skill("Prompt Engineering & LLM APIs", SkillCategory.DATA_AI, "Few-shot prompting, structured JSON schema output, and OpenAI/Anthropic API integration.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/rag-001", "AI_ML"));
        Skill vectorEmbeddings = skillRepository.save(new Skill("Vector Databases & Embeddings", SkillCategory.DATA_AI, "Semantic embedding generation, cosine similarity, HNSW indexing, and ChromaDB/pgvector storage.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/rag-002", "AI_ML"));
        Skill ragArchitecture = skillRepository.save(new Skill("RAG Architecture & LangChain", SkillCategory.DATA_AI, "Retrieval-Augmented Generation architectures, chunking strategies, vector stores, and LangChain/LlamaIndex orchestrations.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/rag-003", "AI_ML"));
        Skill deepLearning = skillRepository.save(new Skill("Deep Learning & PyTorch", SkillCategory.DATA_AI, "Neural networks, backpropagation, and PyTorch tensor operations.", Difficulty.ADVANCED, "ESCO", "http://data.europa.eu/esco/skill/ai-002", "AI_ML"));

        // Domain: MOBILE
        Skill dart = skillRepository.save(new Skill("Dart Programming", SkillCategory.LANGUAGE, "Type-safe asynchronous Dart programming with Streams and Future patterns.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/mob-001", "MOBILE"));
        Skill flutter = skillRepository.save(new Skill("Flutter Framework & Widgets", SkillCategory.FRAMEWORK, "Reactive widget tree rendering, declarative layouts, and cross-platform app compilation.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/mob-002", "MOBILE"));
        Skill stateManagement = skillRepository.save(new Skill("State Management (Riverpod/Bloc)", SkillCategory.ARCHITECTURE, "Reactive immutable state stores, dependency injection, and state notifier patterns.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/mob-003", "MOBILE"));

        // Domain: WEB_DEVELOPMENT & JAVA
        Skill java = skillRepository.save(new Skill("Java", SkillCategory.LANGUAGE, "Core Java programming including OOP, Generics, and Concurrency.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/java-001", "WEB_DEVELOPMENT"));
        Skill oop = skillRepository.save(new Skill("Object-Oriented Programming (OOP)", SkillCategory.CORE_CS, "Encapsulation, Inheritance, Polymorphism, Abstraction, and SOLID principles.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/oop-001", "WEB_DEVELOPMENT"));
        Skill sql = skillRepository.save(new Skill("SQL & Relational Databases", SkillCategory.DATABASE, "Relational data modeling, ACID transactions, complex joins, and indexing.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/sql-001", "WEB_DEVELOPMENT"));
        Skill springBoot = skillRepository.save(new Skill("Spring Boot", SkillCategory.FRAMEWORK, "Enterprise backend architecture with Spring Boot REST services.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/spring-001", "WEB_DEVELOPMENT"));
        Skill restApis = skillRepository.save(new Skill("RESTful APIs", SkillCategory.ARCHITECTURE, "Designing scalable HTTP RESTful web services and API contracts.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/rest-001", "WEB_DEVELOPMENT"));
        Skill springSecurity = skillRepository.save(new Skill("Spring Security & JWT", SkillCategory.FRAMEWORK, "Stateless JWT authentication filter pipelines and authorization.", Difficulty.ADVANCED, "ESCO", "http://data.europa.eu/esco/skill/sec-001", "WEB_DEVELOPMENT"));
        Skill js = skillRepository.save(new Skill("JavaScript (ES6+)", SkillCategory.LANGUAGE, "Modern JavaScript language mechanics, closures, and async/await.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/js-001", "WEB_DEVELOPMENT"));
        Skill react = skillRepository.save(new Skill("React.js", SkillCategory.FRAMEWORK, "Declarative UI engineering with React hooks, components, and virtual DOM.", Difficulty.INTERMEDIATE, "ESCO", "http://data.europa.eu/esco/skill/react-001", "WEB_DEVELOPMENT"));
        Skill git = skillRepository.save(new Skill("Git & Version Control", SkillCategory.CORE_CS, "Branching strategies, Git workflows, PRs, and collaborative version control.", Difficulty.BEGINNER, "ESCO", "http://data.europa.eu/esco/skill/git-001", "WEB_DEVELOPMENT"));

        // =========================================================================
        // 2. SEED ALIASES (SYNONYM & ACRONYM CANONICALIZATION)
        // =========================================================================
        aliasRepository.save(new SkillAlias("dp", dp, "ACRONYM"));
        aliasRepository.save(new SkillAlias("dynamic programming", dp, "SYNONYM"));
        aliasRepository.save(new SkillAlias("recursion", recursion, "SYNONYM"));
        aliasRepository.save(new SkillAlias("memoization", memoization, "SYNONYM"));
        aliasRepository.save(new SkillAlias("tabulation", memoization, "SYNONYM"));
        aliasRepository.save(new SkillAlias("big-o", complexity, "SYNONYM"));
        aliasRepository.save(new SkillAlias("calculus", calculusDiff, "SYNONYM"));
        aliasRepository.save(new SkillAlias("derivatives", calculusDiff, "SYNONYM"));
        aliasRepository.save(new SkillAlias("differentiation", calculusDiff, "SYNONYM"));
        aliasRepository.save(new SkillAlias("integrals", calculusInt, "SYNONYM"));
        aliasRepository.save(new SkillAlias("integration", calculusInt, "SYNONYM"));
        aliasRepository.save(new SkillAlias("blender", meshModeling, "TOOL"));
        aliasRepository.save(new SkillAlias("3d modeling", meshModeling, "SYNONYM"));
        aliasRepository.save(new SkillAlias("photography", exposureTriangle, "SYNONYM"));
        aliasRepository.save(new SkillAlias("photo", exposureTriangle, "SYNONYM"));
        aliasRepository.save(new SkillAlias("music production", daw, "SYNONYM"));
        aliasRepository.save(new SkillAlias("music", daw, "SYNONYM"));
        aliasRepository.save(new SkillAlias("audio mixing", audioMixing, "SYNONYM"));
        aliasRepository.save(new SkillAlias("financial modeling", finModeling, "SYNONYM"));
        aliasRepository.save(new SkillAlias("dcf", finModeling, "ACRONYM"));
        aliasRepository.save(new SkillAlias("finance", finStatements, "SYNONYM"));
        aliasRepository.save(new SkillAlias("k8s", kubernetes, "ACRONYM"));
        aliasRepository.save(new SkillAlias("kubernetes", kubernetes, "SYNONYM"));
        aliasRepository.save(new SkillAlias("rag", ragArchitecture, "ACRONYM"));
        aliasRepository.save(new SkillAlias("retrieval augmented generation", ragArchitecture, "SYNONYM"));
        aliasRepository.save(new SkillAlias("vector db", vectorEmbeddings, "SYNONYM"));
        aliasRepository.save(new SkillAlias("vector database", vectorEmbeddings, "SYNONYM"));
        aliasRepository.save(new SkillAlias("vector databases", vectorEmbeddings, "SYNONYM"));
        aliasRepository.save(new SkillAlias("embeddings", vectorEmbeddings, "SYNONYM"));
        aliasRepository.save(new SkillAlias("llm", promptEngineering, "ACRONYM"));
        aliasRepository.save(new SkillAlias("large language model", promptEngineering, "SYNONYM"));
        aliasRepository.save(new SkillAlias("flutter", flutter, "SYNONYM"));
        aliasRepository.save(new SkillAlias("dart", dart, "SYNONYM"));

        // =========================================================================
        // 3. SEED DIRECTED ONTOLOGY RELATIONS & DAG PREREQUISITES
        // =========================================================================

        // Dynamic Programming Graph
        relationRepository.save(new SkillRelation(recursion, dp, SkillRelationType.PREREQUISITE, 1.0, "Top-down memoized DP builds directly on recursive call trees."));
        relationRepository.save(new SkillRelation(complexity, dp, SkillRelationType.PREREQUISITE, 0.9, "Essential for evaluating optimal substructure and subproblem caching trade-offs."));
        relationRepository.save(new SkillRelation(memoization, dp, SkillRelationType.ESSENTIAL_CORE, 1.0, "Direct core technique for state caching and table transitions."));
        prerequisiteRepository.save(new SkillPrerequisite(dp, recursion, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(dp, complexity, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(memoization, recursion, PrerequisiteStrength.REQUIRED));

        // Mathematics Graph
        relationRepository.save(new SkillRelation(limits, calculusDiff, SkillRelationType.PREREQUISITE, 1.0, "Limits provide the rigorous definition for differential slope."));
        relationRepository.save(new SkillRelation(calculusDiff, calculusInt, SkillRelationType.PREREQUISITE, 1.0, "Integration is the inverse operation of differentiation."));
        prerequisiteRepository.save(new SkillPrerequisite(calculusDiff, limits, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(calculusInt, calculusDiff, PrerequisiteStrength.REQUIRED));

        // 3D / Creative Graph
        relationRepository.save(new SkillRelation(meshModeling, uvTexturing, SkillRelationType.PREREQUISITE, 1.0, "Clean polygonal mesh topology is required before 2D UV seam unwrapping."));
        relationRepository.save(new SkillRelation(uvTexturing, lighting3D, SkillRelationType.PREREQUISITE, 0.9, "PBR material properties interact with 3D lighting shader calculations."));
        prerequisiteRepository.save(new SkillPrerequisite(uvTexturing, meshModeling, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(lighting3D, uvTexturing, PrerequisiteStrength.REQUIRED));

        // Photography Graph
        relationRepository.save(new SkillRelation(exposureTriangle, rawEditing, SkillRelationType.PREREQUISITE, 0.95, "Proper physical optical exposure is required before non-destructive RAW editing."));
        prerequisiteRepository.save(new SkillPrerequisite(rawEditing, exposureTriangle, PrerequisiteStrength.REQUIRED));

        // Audio Graph
        relationRepository.save(new SkillRelation(daw, audioMixing, SkillRelationType.PREREQUISITE, 1.0, "Multi-track DAW routing and session structuring are required before balancing frequencies."));
        prerequisiteRepository.save(new SkillPrerequisite(audioMixing, daw, PrerequisiteStrength.REQUIRED));

        // Finance Graph
        relationRepository.save(new SkillRelation(finStatements, finModeling, SkillRelationType.PREREQUISITE, 1.0, "Mastery of financial statements is mandatory to build forward-looking DCF forecasts."));
        prerequisiteRepository.save(new SkillPrerequisite(finModeling, finStatements, PrerequisiteStrength.REQUIRED));

        // DevOps Graph
        relationRepository.save(new SkillRelation(linux, docker, SkillRelationType.PREREQUISITE, 0.9, "Linux namespaces and cgroups form the foundation of container runtimes."));
        relationRepository.save(new SkillRelation(docker, kubernetes, SkillRelationType.PREREQUISITE, 1.0, "Container images are the atomic unit of deployment in Kubernetes pods."));
        prerequisiteRepository.save(new SkillPrerequisite(docker, linux, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(kubernetes, docker, PrerequisiteStrength.REQUIRED));

        // AI / RAG Graph
        relationRepository.save(new SkillRelation(python, promptEngineering, SkillRelationType.PREREQUISITE, 0.85, "Python SDKs are the primary client for LLM API integration."));
        relationRepository.save(new SkillRelation(python, vectorEmbeddings, SkillRelationType.PREREQUISITE, 0.9, "Vector operations and data preprocessing require Python."));
        relationRepository.save(new SkillRelation(promptEngineering, ragArchitecture, SkillRelationType.PREREQUISITE, 1.0, "Prompt engineering is required to synthesize contextual answers from retrieved chunks."));
        relationRepository.save(new SkillRelation(vectorEmbeddings, ragArchitecture, SkillRelationType.PREREQUISITE, 1.0, "Dense vector embeddings form the foundation of semantic retrieval in RAG."));
        prerequisiteRepository.save(new SkillPrerequisite(promptEngineering, python, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(vectorEmbeddings, python, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(ragArchitecture, promptEngineering, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(ragArchitecture, vectorEmbeddings, PrerequisiteStrength.REQUIRED));

        // Mobile Graph
        relationRepository.save(new SkillRelation(dart, flutter, SkillRelationType.PREREQUISITE, 1.0, "Dart is the programming language powering all Flutter widgets."));
        relationRepository.save(new SkillRelation(flutter, stateManagement, SkillRelationType.PREREQUISITE, 1.0, "Widget tree architecture is required to understand state notification flow."));
        prerequisiteRepository.save(new SkillPrerequisite(flutter, dart, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(stateManagement, flutter, PrerequisiteStrength.REQUIRED));

        // Web / Java Graph
        relationRepository.save(new SkillRelation(oop, java, SkillRelationType.PREREQUISITE, 0.9, "OOP principles are foundational to Java classes."));
        relationRepository.save(new SkillRelation(java, springBoot, SkillRelationType.PREREQUISITE, 1.0, "Spring Boot requires solid Java mastery."));
        relationRepository.save(new SkillRelation(sql, springBoot, SkillRelationType.PREREQUISITE, 0.85, "Database operations in Spring Boot require relational query concepts."));
        prerequisiteRepository.save(new SkillPrerequisite(java, oop, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(springBoot, java, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(springBoot, sql, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(react, js, PrerequisiteStrength.REQUIRED));

        // =========================================================================
        // 4. SEED MULTI-DOMAIN LEARNING RESOURCES
        // =========================================================================

        // DP Resource
        LearningResource resDp = resourceRepository.save(new LearningResource(
                "Dynamic Programming Masterclass: From Recursion to Memoization",
                "Deep dive into 1D and 2D dynamic programming, optimal substructure, state transitions, and tabular caching patterns.",
                ResourceType.COURSE,
                "https://algorithms.org/dp",
                "Algorithms Academy",
                Difficulty.ADVANCED,
                18.0, 4.9, 0.98
        ));
        resourceSkillRepository.save(new ResourceSkill(resDp, dp, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resDp, memoization, 0.9));

        // Calculus Resource
        LearningResource resCalc = resourceRepository.save(new LearningResource(
                "Calculus: Derivatives, Limits, and Rates of Change",
                "Intuitive geometric and algebraic derivations of differential calculus, slope limits, and practical optimization problems.",
                ResourceType.COURSE,
                "https://ocw.mit.edu/courses/mathematics/calculus",
                "MIT OpenCourseWare",
                Difficulty.INTERMEDIATE,
                20.0, 4.9, 0.99
        ));
        resourceSkillRepository.save(new ResourceSkill(resCalc, calculusDiff, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resCalc, limits, 0.9));

        // 3D Blender Resource
        LearningResource res3d = resourceRepository.save(new LearningResource(
                "Blender 3D Modeling & Mesh Topology Fundamentals",
                "Step-by-step masterclass in subdivision modeling, edge flow topology, modifier stacks, and 3D asset preparation.",
                ResourceType.TUTORIAL,
                "https://blender.org/learn",
                "Blender Official",
                Difficulty.BEGINNER,
                16.0, 4.8, 0.95
        ));
        resourceSkillRepository.save(new ResourceSkill(res3d, meshModeling, 1.0));

        // Photography Resource
        LearningResource resPhoto = resourceRepository.save(new LearningResource(
                "The Exposure Triangle: Aperture, Shutter Speed, and ISO Dynamics",
                "Master manual camera controls, sensor noise mitigation, motion freezing, and creative shallow depth of field.",
                ResourceType.TUTORIAL,
                "https://photographyacademy.com/exposure",
                "Photography Academy",
                Difficulty.BEGINNER,
                10.0, 4.8, 0.96
        ));
        resourceSkillRepository.save(new ResourceSkill(resPhoto, exposureTriangle, 1.0));

        // Music / Audio Resource
        LearningResource resAudio = resourceRepository.save(new LearningResource(
                "Modern Audio Engineering: DAW Routing, EQ Balancing, and Dynamic Compression",
                "Professional multi-track mixing in modern DAWs with frequency spectrum carving, saturation, and master bus limiting.",
                ResourceType.COURSE,
                "https://soundonrecord.com/mixing",
                "Sound On Record",
                Difficulty.INTERMEDIATE,
                15.0, 4.9, 0.97
        ));
        resourceSkillRepository.save(new ResourceSkill(resAudio, daw, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resAudio, audioMixing, 0.9));

        // Finance Resource
        LearningResource resFinance = resourceRepository.save(new LearningResource(
                "Financial Modeling & DCF Valuation in Excel",
                "Build dynamic Discounted Cash Flow models from SEC filings, forecast revenue drivers, and calculate Weighted Average Cost of Capital.",
                ResourceType.COURSE,
                "https://corporatefinance.org/dcf",
                "Corporate Finance Institute",
                Difficulty.ADVANCED,
                22.0, 4.9, 0.98
        ));
        resourceSkillRepository.save(new ResourceSkill(resFinance, finModeling, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resFinance, finStatements, 0.9));

        // Cloud / Kubernetes Resource
        LearningResource resK8s = resourceRepository.save(new LearningResource(
                "Kubernetes Cluster Architecture: Pods, Services, and Ingress",
                "Production Kubernetes orchestration covering YAML manifests, DaemonSets, StatefulSets, persistent volumes, and ingress routing.",
                ResourceType.DOCUMENTATION,
                "https://kubernetes.io/docs/home/",
                "Kubernetes Official Docs",
                Difficulty.ADVANCED,
                20.0, 4.9, 0.98
        ));
        resourceSkillRepository.save(new ResourceSkill(resK8s, kubernetes, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resK8s, docker, 0.9));

        // RAG Resource
        LearningResource resRag = resourceRepository.save(new LearningResource(
                "Production RAG Systems with LangChain & LlamaIndex",
                "Comprehensive architecture for Retrieval-Augmented Generation: document loading, recursive chunking, dense vector retrieval, and LLM synthesis.",
                ResourceType.COURSE,
                "https://python.langchain.com/docs/tutorials/rag/",
                "LangChain Official",
                Difficulty.INTERMEDIATE,
                22.0, 4.9, 0.98
        ));
        resourceSkillRepository.save(new ResourceSkill(resRag, ragArchitecture, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resRag, vectorEmbeddings, 0.9));

        // Flutter Resource
        LearningResource resFlutter = resourceRepository.save(new LearningResource(
                "Flutter & Dart: Cross-Platform Mobile Architecture",
                "Build fast native iOS and Android apps with declarative Flutter widgets, Riverpod state management, and animations.",
                ResourceType.COURSE,
                "https://flutter.dev/learn",
                "Flutter Docs",
                Difficulty.INTERMEDIATE,
                20.0, 4.9, 0.97
        ));
        resourceSkillRepository.save(new ResourceSkill(resFlutter, flutter, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resFlutter, dart, 0.9));

        // Java / Spring Boot Resource
        LearningResource resJava = resourceRepository.save(new LearningResource(
                "Modern Java & Spring Boot Enterprise Architecture",
                "Enterprise backend architecture with Spring Boot REST services, transactional databases, and clean architecture.",
                ResourceType.COURSE,
                "https://spring.io/guides",
                "Spring.io",
                Difficulty.INTERMEDIATE,
                25.0, 4.9, 0.98
        ));
        resourceSkillRepository.save(new ResourceSkill(resJava, springBoot, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resJava, java, 0.9));

        // =========================================================================
        // 5. SEED GOAL-ALIGNED PORTFOLIO PROJECTS
        // =========================================================================

        // DP Project
        projectRepository.save(new Project(
                "Dynamic Programming Algorithmic Solver & Memoization Engine",
                "Implement high-performance DP solvers for Classic Knapsack, Longest Common Subsequence, and Matrix Chain Multiplication with state profiling.",
                Difficulty.ADVANCED,
                "https://github.com/algorithms/dp-solver-suite",
                objectMapper.writeValueAsString(List.of("Dynamic Programming", "Recursion & Backtracking", "Memoization & Tabulation")),
                25.0,
                objectMapper.writeValueAsString(List.of(
                        "Formulate recursive mathematical recurrence relations for 3 classic problems",
                        "Implement top-down memoized cache with space complexity analysis",
                        "Convert solutions to bottom-up 1D/2D tabular array DP",
                        "Benchmark execution time and memory footprint against raw recursion"
                ))
        ));

        // Calculus Project
        projectRepository.save(new Project(
                "Numerical Differentiation & Gradient Optimization Engine",
                "Implement finite difference numerical approximations, gradient descent slope optimization, and polynomial root-finding algorithms.",
                Difficulty.INTERMEDIATE,
                "https://github.com/math/numerical-differentiation",
                objectMapper.writeValueAsString(List.of("Calculus & Derivatives", "Limits & Continuity")),
                20.0,
                objectMapper.writeValueAsString(List.of(
                        "Implement forward, backward, and central difference numerical derivatives",
                        "Build gradient descent optimizer for quadratic and multivariable loss surfaces",
                        "Verify convergence rates against analytical calculus derivatives"
                ))
        ));

        // 3D Blender Project
        projectRepository.save(new Project(
                "Hard-Surface 3D Sci-Fi Asset Modeling & PBR Rendering",
                "Model a production-ready 3D sci-fi vehicle asset in Blender with clean subdivision topology, UV unwrapping, and Cycles lighting render.",
                Difficulty.ADVANCED,
                "https://github.com/blender/scifi-hard-surface-asset",
                objectMapper.writeValueAsString(List.of("3D Modeling & Mesh Topology", "UV Unwrapping & PBR Texturing", "3D Lighting & Rendering")),
                30.0,
                objectMapper.writeValueAsString(List.of(
                        "Block out silhouette and refine hard-surface quad topology",
                        "Unwrap UV seams and pack islands with minimal distortion",
                        "Configure PBR materials with normal, roughness, and metalness maps",
                        "Set up studio three-point lighting and render in 4K resolution"
                ))
        ));

        // Photography Project
        projectRepository.save(new Project(
                "Landscape & Portrait Portfolio: Manual Exposure & Color Grading",
                "Capture and post-process a 10-image photography series demonstrating exposure triangle control and non-destructive RAW histogram balancing.",
                Difficulty.INTERMEDIATE,
                "https://github.com/photography/portfolio-series",
                objectMapper.writeValueAsString(List.of("Exposure Triangle (Aperture, Shutter Speed, ISO)", "RAW Image Editing & Color Grading")),
                18.0,
                objectMapper.writeValueAsString(List.of(
                        "Capture 5 high-dynamic-range scenes with zero blown highlights",
                        "Capture 5 shallow depth-of-field portraits using wide aperture",
                        "Color grade RAW captures with custom tone curves and balanced color temperature"
                ))
        ));

        // Music / Audio Project
        projectRepository.save(new Project(
                "Multi-Track Music Production & Studio Mixdown",
                "Compose, arrange, and mix a complete multi-instrument audio track with parametric EQ carving, parallel compression, and master bus limiting.",
                Difficulty.ADVANCED,
                "https://github.com/audio/multitrack-session",
                objectMapper.writeValueAsString(List.of("Digital Audio Workstation (DAW)", "Audio Mixing & Mastering")),
                24.0,
                objectMapper.writeValueAsString(List.of(
                        "Arrange 8+ MIDI and audio stems within DAW session",
                        "Carve conflicting frequencies using parametric equalizers",
                        "Apply sidechain and parallel dynamic compression to drum and bass elements",
                        "Master final stereo output to -14 LUFS integrated loudness"
                ))
        ));

        // Finance Project
        projectRepository.save(new Project(
                "Corporate Discounted Cash Flow (DCF) Financial Valuation Model",
                "Construct an institutional-grade 5-year DCF financial model in Excel for a public company using 10-K filings, WACC estimation, and sensitivity tables.",
                Difficulty.ADVANCED,
                "https://github.com/finance/dcf-valuation-model",
                objectMapper.writeValueAsString(List.of("Financial Modeling & Valuation (DCF)", "Financial Statement Analysis")),
                28.0,
                objectMapper.writeValueAsString(List.of(
                        "Normalize 3 years of historical income statements and balance sheets",
                        "Project 5-year Free Cash Flows to Firm (FCFF) based on revenue drivers",
                        "Calculate cost of equity via CAPM and Weighted Average Cost of Capital (WACC)",
                        "Construct sensitivity tables for perpetual growth rate vs discount rate"
                ))
        ));

        // Kubernetes Project
        projectRepository.save(new Project(
                "Production Multi-Tier Microservices Kubernetes Deployment",
                "Deploy a resilient microservices architecture onto a Kubernetes cluster with ConfigMaps, Secrets, Ingress, and Horizontal Pod Autoscalers.",
                Difficulty.ADVANCED,
                "https://github.com/devops/k8s-microservices-deployment",
                objectMapper.writeValueAsString(List.of("Kubernetes Orchestration", "Docker & Containers", "Linux & Shell Scripting")),
                30.0,
                objectMapper.writeValueAsString(List.of(
                        "Containerize services into minimal multi-stage Docker images",
                        "Write declarative Kubernetes Deployments, Services, and ConfigMaps",
                        "Configure Ingress NGINX with TLS termination",
                        "Set up Horizontal Pod Autoscaling (HPA) based on CPU/Memory thresholds"
                ))
        ));

        // RAG Project
        projectRepository.save(new Project(
                "Enterprise Document QA RAG System with Vector Search & LangChain",
                "Build a complete Retrieval-Augmented Generation pipeline that parses enterprise PDFs, computes embeddings in ChromaDB, and synthesizes answers with citations.",
                Difficulty.ADVANCED,
                "https://github.com/rag/enterprise-document-qa",
                objectMapper.writeValueAsString(List.of("RAG Architecture & LangChain", "Vector Databases & Embeddings", "Prompt Engineering & LLM APIs")),
                30.0,
                objectMapper.writeValueAsString(List.of(
                        "Build PDF document loader with semantic recursive chunking",
                        "Generate vector embeddings and store in ChromaDB with metadata filters",
                        "Implement hybrid search with reciprocal rank fusion",
                        "Generate grounded citations with prompt guardrails and evaluation"
                ))
        ));

        // Flutter Project
        projectRepository.save(new Project(
                "Cross-Platform Flutter Mobile Application with Riverpod State",
                "Develop a responsive cross-platform mobile application in Flutter featuring async state management, persistent caching, and smooth page transitions.",
                Difficulty.INTERMEDIATE,
                "https://github.com/flutter/crossplatform-mobile-app",
                objectMapper.writeValueAsString(List.of("Flutter Framework & Widgets", "Dart Programming", "State Management (Riverpod/Bloc)")),
                25.0,
                objectMapper.writeValueAsString(List.of(
                        "Structure responsive UI widget tree with custom theme styling",
                        "Implement global state management and dependency injection with Riverpod",
                        "Integrate asynchronous REST API client with local Hive cache",
                        "Build adaptive layouts for Android and iOS devices"
                ))
        ));

        // Java Backend Project
        projectRepository.save(new Project(
                "Enterprise Spring Boot Microservices Backend with JWT Security",
                "Build a high-performance REST API backend with Spring Boot 3, Spring Data JPA, PostgreSQL, and stateless JWT authentication filters.",
                Difficulty.ADVANCED,
                "https://github.com/backend/spring-boot-enterprise-backend",
                objectMapper.writeValueAsString(List.of("Spring Boot", "Java", "SQL & Relational Databases", "Spring Security & JWT")),
                30.0,
                objectMapper.writeValueAsString(List.of(
                        "Design normalized PostgreSQL relational schema and JPA entities",
                        "Implement Spring Security filter chain with JWT Bearer authentication",
                        "Create transactional CRUD REST endpoints with pagination and DTO mapping",
                        "Write unit and integration tests with MockMvc and Testcontainers"
                ))
        ));
    }
}
