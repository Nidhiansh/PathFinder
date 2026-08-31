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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (skillRepository.count() > 0) {
            return; // Seed data already loaded
        }

        // ==========================================
        // 1. SEED SKILLS
        // ==========================================
        Skill java = skillRepository.save(new Skill("Java", SkillCategory.LANGUAGE, "Core Java programming including OOP, Generics, and concurrency.", Difficulty.INTERMEDIATE));
        Skill oop = skillRepository.save(new Skill("Object-Oriented Programming (OOP)", SkillCategory.CORE_CS, "Encapsulation, Inheritance, Polymorphism, Abstraction, and SOLID principles.", Difficulty.BEGINNER));
        Skill dsa = skillRepository.save(new Skill("Data Structures & Algorithms", SkillCategory.CORE_CS, "Arrays, Trees, Graphs, Sorting, Dynamic Programming, and Big-O notation.", Difficulty.INTERMEDIATE));
        Skill sql = skillRepository.save(new Skill("SQL & Relational Databases", SkillCategory.DATABASE, "Relational data modeling, ACID transactions, complex joins, and indexing.", Difficulty.BEGINNER));
        Skill springBoot = skillRepository.save(new Skill("Spring Boot", SkillCategory.FRAMEWORK, "Modern enterprise Java development with Spring Boot framework.", Difficulty.INTERMEDIATE));
        Skill restApis = skillRepository.save(new Skill("RESTful APIs", SkillCategory.ARCHITECTURE, "Designing and building scalable HTTP RESTful web services and API contracts.", Difficulty.INTERMEDIATE));
        Skill jpa = skillRepository.save(new Skill("Spring Data JPA & Hibernate", SkillCategory.FRAMEWORK, "Object-Relational Mapping (ORM), entity lifecycles, and automated queries.", Difficulty.INTERMEDIATE));
        Skill springSecurity = skillRepository.save(new Skill("Spring Security & JWT", SkillCategory.FRAMEWORK, "Authentication, authorization, JWT tokens, and OAuth2 security.", Difficulty.ADVANCED));
        Skill docker = skillRepository.save(new Skill("Docker & Containers", SkillCategory.DEVOPS, "Containerizing applications, multi-stage builds, and Docker Compose networks.", Difficulty.INTERMEDIATE));
        Skill systemDesign = skillRepository.save(new Skill("System Design & Microservices", SkillCategory.ARCHITECTURE, "Scalable distributed architecture, caching with Redis, message queues, and load balancing.", Difficulty.ADVANCED));

        // Frontend & Fullstack Skills
        Skill js = skillRepository.save(new Skill("JavaScript (ES6+)", SkillCategory.LANGUAGE, "Modern JavaScript language mechanics, closures, promises, and async/await.", Difficulty.INTERMEDIATE));
        Skill react = skillRepository.save(new Skill("React.js", SkillCategory.FRAMEWORK, "Declarative UI engineering with React hooks, components, and virtual DOM.", Difficulty.INTERMEDIATE));
        Skill node = skillRepository.save(new Skill("Node.js & Express", SkillCategory.FRAMEWORK, "Event-driven asynchronous backend runtime with Express middleware.", Difficulty.INTERMEDIATE));
        Skill git = skillRepository.save(new Skill("Git & Version Control", SkillCategory.CORE_CS, "Branching strategies, Git workflows, PRs, and collaborative version control.", Difficulty.BEGINNER));

        // AI / ML & Generative AI / RAG Skills
        Skill python = skillRepository.save(new Skill("Python Programming", SkillCategory.LANGUAGE, "Python data structures, object orientation, and idiomatic scripting.", Difficulty.BEGINNER));
        Skill mathMl = skillRepository.save(new Skill("Mathematics & Statistics for ML", SkillCategory.DATA_AI, "Linear algebra, multivariate calculus, probability distributions, and hypothesis testing.", Difficulty.INTERMEDIATE));
        Skill numpyPandas = skillRepository.save(new Skill("NumPy & Pandas", SkillCategory.DATA_AI, "Vectorized numerical computation and structured tabular data wrangling.", Difficulty.INTERMEDIATE));
        Skill scikitLearn = skillRepository.save(new Skill("Scikit-Learn", SkillCategory.DATA_AI, "Supervised & unsupervised machine learning models, cross-validation, and metrics.", Difficulty.INTERMEDIATE));
        Skill deepLearning = skillRepository.save(new Skill("Deep Learning & PyTorch", SkillCategory.DATA_AI, "Neural networks, backpropagation, convolutional networks, and PyTorch tensors.", Difficulty.ADVANCED));
        Skill fastapi = skillRepository.save(new Skill("Model Deployment & FastAPI", SkillCategory.DATA_AI, "Serving machine learning models as high-throughput REST APIs.", Difficulty.INTERMEDIATE));
        Skill promptEngineering = skillRepository.save(new Skill("Prompt Engineering & LLM APIs", SkillCategory.DATA_AI, "Crafting structured prompts, few-shot examples, JSON schema enforcement, and tool calling with OpenAI/Anthropic APIs.", Difficulty.BEGINNER));
        Skill vectorEmbeddings = skillRepository.save(new Skill("Vector Databases & Embeddings", SkillCategory.DATA_AI, "Generating semantic text embeddings, cosine similarity metrics, and index structures (HNSW/IVF).", Difficulty.INTERMEDIATE));
        Skill ragArchitecture = skillRepository.save(new Skill("RAG Architecture & LangChain", SkillCategory.DATA_AI, "Retrieval-Augmented Generation architectures, chunking strategies, vector stores, and LangChain/LlamaIndex orchestrations.", Difficulty.INTERMEDIATE));
        Skill retrievalOpt = skillRepository.save(new Skill("Chunking, Reranking & Retrieval Optimization", SkillCategory.DATA_AI, "Hybrid search (BM25 + Dense), Cross-Encoder reranking, contextual compression, and document chunking optimization.", Difficulty.ADVANCED));
        Skill llmEval = skillRepository.save(new Skill("LLM Evaluation & Guardrails", SkillCategory.DATA_AI, "RAG assessment metrics (Faithfulness, Answer Relevance, Context Recall with Ragas), guardrails, and latency optimization.", Difficulty.ADVANCED));

        // Cloud & Infrastructure
        Skill cloudInfra = skillRepository.save(new Skill("Cloud Infrastructure & Kubernetes", SkillCategory.DEVOPS, "Kubernetes container orchestration, cloud primitives on AWS/GCP, and Infrastructure as Code.", Difficulty.ADVANCED));

        // ==========================================
        // 2. SEED PREREQUISITES (SKILL DAG)
        // ==========================================
        prerequisiteRepository.save(new SkillPrerequisite(java, oop, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(springBoot, java, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(restApis, springBoot, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(jpa, sql, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(jpa, springBoot, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(springSecurity, springBoot, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(systemDesign, restApis, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(systemDesign, docker, PrerequisiteStrength.RECOMMENDED));

        prerequisiteRepository.save(new SkillPrerequisite(react, js, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(node, js, PrerequisiteStrength.REQUIRED));

        prerequisiteRepository.save(new SkillPrerequisite(numpyPandas, python, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(scikitLearn, numpyPandas, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(scikitLearn, mathMl, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(deepLearning, scikitLearn, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(fastapi, python, PrerequisiteStrength.REQUIRED));

        prerequisiteRepository.save(new SkillPrerequisite(promptEngineering, python, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(vectorEmbeddings, python, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(ragArchitecture, promptEngineering, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(ragArchitecture, vectorEmbeddings, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(retrievalOpt, ragArchitecture, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(llmEval, ragArchitecture, PrerequisiteStrength.REQUIRED));
        prerequisiteRepository.save(new SkillPrerequisite(cloudInfra, docker, PrerequisiteStrength.REQUIRED));

        // ==========================================
        // 3. SEED LEARNING RESOURCES
        // ==========================================
        LearningResource resJava = resourceRepository.save(new LearningResource(
                "Modern Java: Collections, Streams, and Concurrency",
                "Comprehensive masterclass covering Java 21 LTS, Stream pipelines, Optional patterns, and concurrent executor pools.",
                ResourceType.COURSE,
                "https://dev.java/learn/",
                "Oracle Java Tutorials",
                Difficulty.INTERMEDIATE,
                16.0, 4.9, 0.96
        ));
        resourceSkillRepository.save(new ResourceSkill(resJava, java, 1.0));

        LearningResource resSql = resourceRepository.save(new LearningResource(
                "PostgreSQL High Performance & Schema Architecture",
                "Industry guide to relational data modeling, query optimization, B-Tree indexes, and transaction isolation levels.",
                ResourceType.TUTORIAL,
                "https://www.postgresql.org/docs/current/tutorial.html",
                "PostgreSQL Docs",
                Difficulty.INTERMEDIATE,
                12.0, 4.8, 0.94
        ));
        resourceSkillRepository.save(new ResourceSkill(resSql, sql, 1.0));

        LearningResource resSpringCore = resourceRepository.save(new LearningResource(
                "Building RESTful Web Services with Spring Boot",
                "Official Spring guide to creating production REST APIs with Spring MVC, Jackson serialization, and dependency injection.",
                ResourceType.DOCUMENTATION,
                "https://spring.io/guides/gs/rest-service/",
                "Spring.io",
                Difficulty.INTERMEDIATE,
                14.0, 4.9, 0.98
        ));
        resourceSkillRepository.save(new ResourceSkill(resSpringCore, springBoot, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resSpringCore, restApis, 0.9));

        LearningResource resJpa = resourceRepository.save(new LearningResource(
                "Mastering Spring Data JPA & Hibernate Performance",
                "Avoid N+1 query traps, understand detached entity states, custom JPQL queries, and transactional boundary management.",
                ResourceType.COURSE,
                "https://spring.io/guides/gs/accessing-data-jpa/",
                "Spring.io",
                Difficulty.INTERMEDIATE,
                15.0, 4.7, 0.92
        ));
        resourceSkillRepository.save(new ResourceSkill(resJpa, jpa, 1.0));

        LearningResource resSecurity = resourceRepository.save(new LearningResource(
                "Spring Security 6: Stateless JWT Authentication Architecture",
                "Implement robust JWT filter pipelines, BCrypt password hashing, role-based endpoint authorization, and CORS handling.",
                ResourceType.COURSE,
                "https://spring.io/guides/topicals/spring-security-architecture",
                "Spring.io",
                Difficulty.ADVANCED,
                18.0, 4.8, 0.95
        ));
        resourceSkillRepository.save(new ResourceSkill(resSecurity, springSecurity, 1.0));

        LearningResource resDocker = resourceRepository.save(new LearningResource(
                "Docker for Backend Developers: Multi-Stage Builds & Orchestration",
                "Create lightweight alpine JVM container images, manage environment variables, and orchestrate microservices with Docker Compose.",
                ResourceType.VIDEO,
                "https://docs.docker.com/get-started/",
                "Docker Docs",
                Difficulty.INTERMEDIATE,
                10.0, 4.7, 0.91
        ));
        resourceSkillRepository.save(new ResourceSkill(resDocker, docker, 1.0));

        LearningResource resSysDesign = resourceRepository.save(new LearningResource(
                "Distributed System Design: Scalability, Caching, and Message Queues",
                "Architecting high-availability systems with Redis caching, asynchronous event queues, database partitioning, and rate limiting.",
                ResourceType.BOOK,
                "https://github.com/donnemartin/system-design-primer",
                "System Design Primer",
                Difficulty.ADVANCED,
                24.0, 4.9, 0.99
        ));
        resourceSkillRepository.save(new ResourceSkill(resSysDesign, systemDesign, 1.0));

        // Frontend & AI Resources
        LearningResource resReact = resourceRepository.save(new LearningResource(
                "React 19 Official Documentation & Interactive Guide",
                "State management, server components, hooks lifecycle, and composable UI design patterns.",
                ResourceType.DOCUMENTATION,
                "https://react.dev/learn",
                "React Docs",
                Difficulty.INTERMEDIATE,
                20.0, 4.9, 0.97
        ));
        resourceSkillRepository.save(new ResourceSkill(resReact, react, 1.0));

        LearningResource resPython = resourceRepository.save(new LearningResource(
                "Applied Machine Learning with Scikit-Learn and PyTorch",
                "End-to-end pipeline covering data preprocessing, model selection, hyperparameter tuning, and deep learning architectures.",
                ResourceType.COURSE,
                "https://scikit-learn.org/stable/tutorial/index.html",
                "Scikit-Learn Docs",
                Difficulty.INTERMEDIATE,
                25.0, 4.8, 0.95
        ));
        resourceSkillRepository.save(new ResourceSkill(resPython, scikitLearn, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resPython, python, 0.8));

        // RAG & Generative AI Resources
        LearningResource resRag = resourceRepository.save(new LearningResource(
                "Production RAG Systems with LangChain & LlamaIndex",
                "Comprehensive architecture for Retrieval-Augmented Generation: document loading, recursive chunking, dense vector retrieval, and LLM synthesis.",
                ResourceType.COURSE,
                "https://python.langchain.com/docs/tutorials/rag/",
                "LangChain & LlamaIndex Official Guides",
                Difficulty.INTERMEDIATE,
                22.0, 4.9, 0.98
        ));
        resourceSkillRepository.save(new ResourceSkill(resRag, ragArchitecture, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resRag, vectorEmbeddings, 0.9));

        LearningResource resEmbeddings = resourceRepository.save(new LearningResource(
                "Semantic Search and Vector Databases with ChromaDB & pgvector",
                "Learn vector similarity metrics (Cosine, Dot Product), approximate nearest neighbor index structures (HNSW/IVF), and PostgreSQL pgvector integration.",
                ResourceType.TUTORIAL,
                "https://github.com/pgvector/pgvector",
                "pgvector & ChromaDB Docs",
                Difficulty.INTERMEDIATE,
                14.0, 4.8, 0.95
        ));
        resourceSkillRepository.save(new ResourceSkill(resEmbeddings, vectorEmbeddings, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resEmbeddings, retrievalOpt, 0.8));

        LearningResource resPrompt = resourceRepository.save(new LearningResource(
                "Advanced Prompt Engineering & Structured Tool Calling",
                "Techniques for chain-of-thought, ReAct framework, structured JSON output extraction with Pydantic, and multi-agent workflows.",
                ResourceType.DOCUMENTATION,
                "https://platform.openai.com/docs/guides/prompt-engineering",
                "OpenAI & Anthropic Guides",
                Difficulty.BEGINNER,
                10.0, 4.9, 0.96
        ));
        resourceSkillRepository.save(new ResourceSkill(resPrompt, promptEngineering, 1.0));

        LearningResource resLlmEval = resourceRepository.save(new LearningResource(
                "RAG Triad & LLM Evaluation with Ragas Framework",
                "Evaluating RAG systems with quantitative metrics: Faithfulness, Answer Relevance, and Context Recall. Guardrail synthesis and automated benchmarking.",
                ResourceType.COURSE,
                "https://docs.ragas.io/",
                "Ragas Evaluation Framework",
                Difficulty.ADVANCED,
                16.0, 4.8, 0.94
        ));
        resourceSkillRepository.save(new ResourceSkill(resLlmEval, llmEval, 1.0));
        resourceSkillRepository.save(new ResourceSkill(resLlmEval, retrievalOpt, 0.8));

        // ==========================================
        // 4. SEED PROJECTS
        // ==========================================
        projectRepository.save(new Project(
                "Java Multithreaded Web Crawler & Indexer",
                "Build a high-performance concurrent web crawler using Java CompletableFuture, ForkJoinPool, and custom thread-safe blocking queues.",
                Difficulty.INTERMEDIATE, 15.0,
                "Deliverables: Multi-threaded engine, rate-limiter, and searchable indexed file store.",
                "Rubric: Thread safety (30%), throughput efficiency (30%), code clean architecture (40%).",
                java, "https://github.com/spring-guides/gs-async-method"
        ));

        projectRepository.save(new Project(
                "E-Commerce Database Schema & Query Optimization Engine",
                "Design a complete normalized PostgreSQL relational database for an e-commerce platform with indexing and partition strategies.",
                Difficulty.INTERMEDIATE, 12.0,
                "Deliverables: Normalized DDL schema, sample dataset, and 10 analytical query benchmarks.",
                "Rubric: Normalization compliance (40%), query execution time (30%), constraint integrity (30%).",
                sql, "https://github.com/postgresql/postgresql"
        ));

        projectRepository.save(new Project(
                "Production-Ready E-Commerce REST API with Spring Boot & PostgreSQL",
                "Build a modular Spring Boot REST service with DTO validation, custom exception handler, pagination, and transactional service layer.",
                Difficulty.INTERMEDIATE, 20.0,
                "Deliverables: REST endpoints with OpenAPI/Swagger docs, JUnit service test suite, and JPA entities.",
                "Rubric: Clean Architecture separation (35%), error handling (25%), test coverage (40%).",
                springBoot, "https://github.com/spring-guides/gs-rest-service"
        ));

        projectRepository.save(new Project(
                "Enterprise JWT Authentication & RBAC Microservice",
                "Construct a dedicated authentication service implementing refresh tokens, BCrypt hashing, and role-based route guards.",
                Difficulty.ADVANCED, 16.0,
                "Deliverables: Stateless authentication provider, JWT filter pipeline, and password reset workflow.",
                "Rubric: Security robustness (50%), token revocation logic (25%), code test coverage (25%).",
                springSecurity, "https://github.com/spring-guides/gs-securing-web"
        ));

        projectRepository.save(new Project(
                "Multi-Container Microservices Deployment with Docker & Compose",
                "Containerize Spring Boot backend, PostgreSQL database, and Redis cache into an orchestrated multi-service environment.",
                Difficulty.INTERMEDIATE, 10.0,
                "Deliverables: Multi-stage Dockerfiles, docker-compose.yml configuration, and health check probes.",
                "Rubric: Image optimization (40%), secret isolation (30%), networking reliability (30%).",
                docker, "https://github.com/docker/awesome-compose"
        ));

        projectRepository.save(new Project(
                "High-Throughput Distributed URL Shortener (System Design)",
                "Architect and implement a scalable URL shortening service handling 10k requests/sec with Redis caching and Base62 encoding.",
                Difficulty.ADVANCED, 22.0,
                "Deliverables: System architecture diagram, Redis cache layer, and database sharding strategy.",
                "Rubric: Throughput benchmarking (40%), cache hit ratio (30%), architecture documentation (30%).",
                systemDesign, "https://github.com/donnemartin/system-design-primer"
        ));

        // RAG & AI Projects
        projectRepository.save(new Project(
                "Enterprise Document QA RAG System with Vector Search & LangChain",
                "Construct an end-to-end RAG application that ingests PDFs, performs semantic chunking, indexes vectors into ChromaDB, and answers queries with source citations.",
                Difficulty.INTERMEDIATE, 18.0,
                "Deliverables: Python LangChain pipeline, ChromaDB vector store, FastAPI querying endpoint, and Streamlit interactive UI.",
                "Rubric: Retrieval accuracy (35%), hallucination mitigation (35%), API performance (30%).",
                ragArchitecture, "https://github.com/langchain-ai/rag-from-scratch"
        ));

        projectRepository.save(new Project(
                "Multi-Source Semantic Knowledge Base with Hybrid BM25 & Vector Retrieval",
                "Implement a two-stage hybrid retrieval system with Cohere cross-encoder reranking, contextual compression, and automated evaluation.",
                Difficulty.ADVANCED, 20.0,
                "Deliverables: Hybrid search index, reranking pipeline, evaluation benchmarks, and Dockerized FastAPI microservice.",
                "Rubric: Precision@k / Recall@k metrics (40%), reranker latency (30%), code modularity (30%).",
                retrievalOpt, "https://github.com/run-llama/llama_index"
        ));

        // ==========================================
        // 5. SEED ASSESSMENTS
        // ==========================================
        Assessment assessJava = assessmentRepository.save(new Assessment(
                "Java Advanced Concepts & OOP Checkpoint",
                "Test your understanding of Java generics, streams, memory model, and concurrency.",
                java, Difficulty.INTERMEDIATE, 70, 15
        ));
        questionRepository.save(new AssessmentQuestion(assessJava,
                "Which interface does a parallel stream use under the hood in Java for thread management?",
                objectMapper.writeValueAsString(List.of("ThreadPoolExecutor", "ForkJoinPool", "ScheduledExecutorService", "SingleThreadExecutor")),
                1, "Parallel streams in Java utilize the common ForkJoinPool.commonPool() for work-stealing task execution."));
        questionRepository.save(new AssessmentQuestion(assessJava,
                "What is the time complexity of looking up a key in a well-balanced Java HashMap?",
                objectMapper.writeValueAsString(List.of("O(1) amortized, O(log n) when treeified", "O(n)", "O(log n) always", "O(1) worst case")),
                0, "Java 8+ treeifies bins with >8 collisions using Red-Black trees, giving O(1) average and O(log n) worst case."));
        questionRepository.save(new AssessmentQuestion(assessJava,
                "Which keyword prevents instruction reordering and ensures visibility across threads in Java?",
                objectMapper.writeValueAsString(List.of("transient", "synchronized only", "volatile", "final")),
                2, "The 'volatile' keyword establishes a happens-before relationship, preventing compiler/CPU reordering and ensuring memory visibility."));

        Assessment assessSql = assessmentRepository.save(new Assessment(
                "Relational SQL & Query Optimization Checkpoint",
                "Evaluate your SQL query design, index utilization, and transaction isolation knowledge.",
                sql, Difficulty.INTERMEDIATE, 70, 15
        ));
        questionRepository.save(new AssessmentQuestion(assessSql,
                "Which index type is best suited for range queries like `WHERE age BETWEEN 20 AND 30` in PostgreSQL?",
                objectMapper.writeValueAsString(List.of("Hash Index", "B-Tree Index", "GIN Index", "BRIN Index only")),
                1, "B-Tree indexes maintain sorted keys and are optimal for equality and range query operators (<, <=, =, >=, >, BETWEEN)."));
        questionRepository.save(new AssessmentQuestion(assessSql,
                "Which transaction isolation level prevents dirty reads and non-repeatable reads, but may allow phantom reads?",
                objectMapper.writeValueAsString(List.of("Read Uncommitted", "Read Committed", "Repeatable Read", "Serializable")),
                2, "Repeatable Read guarantees that any data read cannot change during the transaction, preventing dirty and non-repeatable reads."));

        Assessment assessSpringBoot = assessmentRepository.save(new Assessment(
                "Spring Boot REST & Architecture Checkpoint",
                "Verify your comprehension of Spring Boot dependency injection, bean scopes, and REST controllers.",
                springBoot, Difficulty.INTERMEDIATE, 70, 15
        ));
        questionRepository.save(new AssessmentQuestion(assessSpringBoot,
                "What is the default bean scope in Spring Framework / Spring Boot application contexts?",
                objectMapper.writeValueAsString(List.of("Prototype", "Singleton", "Request", "Session")),
                1, "By default, Spring beans are managed as singletons: one shared instance per Spring IoC container."));
        questionRepository.save(new AssessmentQuestion(assessSpringBoot,
                "Which annotation combines `@Controller` and `@ResponseBody` in Spring MVC?",
                objectMapper.writeValueAsString(List.of("@Service", "@RestController", "@Endpoint", "@WebComponent")),
                1, "@RestController is a convenience annotation that combines @Controller and @ResponseBody, serializing return objects directly to JSON/XML."));
        questionRepository.save(new AssessmentQuestion(assessSpringBoot,
                "How do you handle global controller exceptions gracefully in Spring Boot?",
                objectMapper.writeValueAsString(List.of("Using `@RestControllerAdvice` and `@ExceptionHandler`", "Writing try-catch in every controller method", "Throwing RuntimeException directly", "Configuring web.xml")),
                0, "@RestControllerAdvice with @ExceptionHandler provides centralized, clean global exception mapping to consistent JSON responses."));

        // RAG & Python Assessments
        Assessment assessRag = assessmentRepository.save(new Assessment(
                "RAG Architecture & Vector Search Checkpoint",
                "Verify your comprehension of semantic embeddings, vector indexing, retrieval reranking, and RAG evaluation.",
                ragArchitecture, Difficulty.INTERMEDIATE, 70, 15
        ));
        questionRepository.save(new AssessmentQuestion(assessRag,
                "What is the primary advantage of Hybrid Search combining BM25 keyword matching with Dense Vector Embeddings in RAG pipelines?",
                objectMapper.writeValueAsString(List.of(
                        "It balances exact keyword/acronym matching with deep semantic contextual relevance.",
                        "It reduces vector database memory requirements to zero.",
                        "It eliminates the need for prompt templates.",
                        "It allows searching without generating any vector embeddings."
                )),
                0, "Hybrid search leverages BM25 for precise term/lexical matches and dense vectors for conceptual semantic relevance, yielding optimal retrieval recall."));
        questionRepository.save(new AssessmentQuestion(assessRag,
                "What is the role of a Cross-Encoder Reranker in a two-stage RAG retrieval architecture?",
                objectMapper.writeValueAsString(List.of(
                        "It compresses PDF documents into Markdown format.",
                        "It scores the full query-document text pair with high precision on top-K candidates to prioritize the most relevant context.",
                        "It automatically fine-tunes the base foundation LLM.",
                        "It generates synthetic Q&A pairs for training datasets."
                )),
                1, "Cross-encoders compute full cross-attention between the query and candidate chunk, reordering candidates with higher accuracy than fast bi-encoder embeddings."));
        questionRepository.save(new AssessmentQuestion(assessRag,
                "In the RAG Triad evaluation framework (Ragas), what does 'Faithfulness' measure?",
                objectMapper.writeValueAsString(List.of(
                        "The execution speed of vector database similarity queries.",
                        "Whether all claims in the generated response can be inferred directly from the retrieved context without hallucinations.",
                        "The total number of parameters in the embedding model.",
                        "Whether the prompt contains system instructions."
                )),
                1, "Faithfulness measures factual consistency of the answer against retrieved context, identifying whether any hallucinated facts were introduced."));

        Assessment assessPython = assessmentRepository.save(new Assessment(
                "Python & AI Data Foundations Checkpoint",
                "Evaluate core Python programming, vector math, and data processing fundamentals.",
                python, Difficulty.BEGINNER, 70, 15
        ));
        questionRepository.save(new AssessmentQuestion(assessPython,
                "What is the primary benefit of using Python generators with the `yield` statement?",
                objectMapper.writeValueAsString(List.of(
                        "Lazy evaluation: generating items on the fly without loading large datasets into memory.",
                        "It forces code to run on GPU threads.",
                        "It converts Python scripts to C binaries automatically.",
                        "It makes variables global across all modules."
                )),
                0, "Generators evaluate lazily on-demand, enabling efficient streaming of massive documents or vector datasets with minimal memory footprint."));
        questionRepository.save(new AssessmentQuestion(assessPython,
                "Which similarity metric computes the cosine of the angle between two normalized vector embeddings?",
                objectMapper.writeValueAsString(List.of("Manhattan Distance", "Cosine Similarity", "Hamming Distance", "Jaccard Index")),
                1, "Cosine similarity measures the directional alignment between vector embeddings independent of magnitude, standard for semantic search."));

        // ==========================================
        // 6. SEED DEMO USERS
        // ==========================================
        // User 1: Alex Chen (Backend Java Developer)
        User user1 = userRepository.save(new User("demo_java", "alex.chen@example.com", passwordEncoder.encode("password123"), Role.ROLE_USER));
        LearnerProfile prof1 = profileRepository.save(new LearnerProfile(
                user1, "Alex Chen", "Backend Java Developer", "Prepare for Backend Software Engineering Internships and Master Spring Boot"
        ));
        prof1.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        prof1.setWeeklyHours(10);
        prof1.setPreferredStyle(LearningStyle.PRACTICAL);
        prof1.setStreakDays(7);
        prof1.setTotalHoursSpent(28.5);
        profileRepository.save(prof1);

        userSkillRepository.save(new UserSkill(prof1, java, 80, true));
        userSkillRepository.save(new UserSkill(prof1, sql, 60, false));
        userSkillRepository.save(new UserSkill(prof1, dsa, 65, false));
        userSkillRepository.save(new UserSkill(prof1, oop, 85, true));
        userSkillRepository.save(new UserSkill(prof1, springBoot, 20, false));
        userSkillRepository.save(new UserSkill(prof1, restApis, 30, false));
        userSkillRepository.save(new UserSkill(prof1, docker, 0, false));

        // User 2: Sarah Taylor (Full Stack)
        User user2 = userRepository.save(new User("demo_fullstack", "sarah.taylor@example.com", passwordEncoder.encode("password123"), Role.ROLE_USER));
        LearnerProfile prof2 = profileRepository.save(new LearnerProfile(
                user2, "Sarah Taylor", "Full Stack Developer", "Build end-to-end cloud platforms with React and Node.js"
        ));
        prof2.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        prof2.setWeeklyHours(12);
        prof2.setPreferredStyle(LearningStyle.VISUAL);
        prof2.setStreakDays(5);
        prof2.setTotalHoursSpent(18.0);
        profileRepository.save(prof2);

        userSkillRepository.save(new UserSkill(prof2, js, 85, true));
        userSkillRepository.save(new UserSkill(prof2, react, 75, true));
        userSkillRepository.save(new UserSkill(prof2, node, 40, false));
        userSkillRepository.save(new UserSkill(prof2, sql, 50, false));
        userSkillRepository.save(new UserSkill(prof2, git, 80, true));

        // Generate roadmap for Alex Chen
        roadmapService.generatePersonalizedRoadmap(user1);
    }
}
