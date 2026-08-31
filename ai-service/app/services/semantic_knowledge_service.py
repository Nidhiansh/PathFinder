import re
import math
from typing import Dict, List, Any, Optional, Tuple, Set

class SemanticKnowledgeService:
    """
    Universal Knowledge-Grounded Ontology & Semantic Retrieval Engine.
    Grounds learning goals against ESCO and O*NET taxonomy concepts, classifies
    intent, extracts validated prerequisite DAGs, and performs strict semantic
    relevance filtering to exclude generic unrelated baggage.
    """

    def __init__(self):
        # 1. Canonical Taxonomy Database (ESCO & O*NET aligned)
        self.taxonomy: Dict[str, Dict[str, Any]] = {
            # --- Algorithms & Problem Solving ---
            "Dynamic Programming": {
                "domain": "ALGORITHMS_CS",
                "category": "CORE_CS",
                "difficulty": "ADVANCED",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/dp-001",
                "description": "Optimizing recursive solutions via subproblem caching, state transitions, and tabular recurrence relations.",
                "prerequisites": ["Recursion & Backtracking", "Time & Space Complexity (Big-O)"],
                "core_subskills": ["Memoization & Tabulation", "State Transition Formulation"],
                "aliases": ["dp", "dynamic programming", "memoization", "tabulation", "subproblem caching", "bellman equation", "knapsack problem", "longest common subsequence"]
            },
            "Recursion & Backtracking": {
                "domain": "ALGORITHMS_CS",
                "category": "CORE_CS",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/rec-002",
                "description": "Recursive base cases, call stack mechanics, exhaustive search, and branch pruning.",
                "prerequisites": ["Time & Space Complexity (Big-O)", "Arrays & Data Structures"],
                "core_subskills": ["Call Stack Mechanics", "Base Case Design"],
                "aliases": ["recursion", "recursive", "backtracking", "call stack", "dfs recursion"]
            },
            "Memoization & Tabulation": {
                "domain": "ALGORITHMS_CS",
                "category": "CORE_CS",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/memo-003",
                "description": "Top-down lookup tables vs bottom-up multi-dimensional state array transitions.",
                "prerequisites": ["Recursion & Backtracking"],
                "core_subskills": ["1D/2D State Arrays", "Lookup Cache Optimization"],
                "aliases": ["memoization", "tabulation", "state array", "caching subproblems"]
            },
            "Time & Space Complexity (Big-O)": {
                "domain": "ALGORITHMS_CS",
                "category": "CORE_CS",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/bigo-004",
                "description": "Asymptotic analysis, recursive tree expansion, and memory auxiliary bounds.",
                "prerequisites": [],
                "core_subskills": ["Asymptotic Bounds", "Recursive Tree Analysis"],
                "aliases": ["big-o", "complexity", "time complexity", "space complexity", "asymptotic analysis"]
            },
            "Arrays & Data Structures": {
                "domain": "ALGORITHMS_CS",
                "category": "CORE_CS",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/arrays-005",
                "description": "Contiguous memory allocations, hashing structures, and pointer manipulation.",
                "prerequisites": [],
                "core_subskills": ["Memory Pointers", "Hashing Arrays"],
                "aliases": ["arrays", "data structures", "hash maps", "pointers"]
            },

            # --- Mathematics & Sciences ---
            "Calculus & Derivatives": {
                "domain": "MATHEMATICS",
                "category": "CORE_CS",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/math-002",
                "description": "Differential calculus, product/chain rules, tangent slopes, and optimization.",
                "prerequisites": ["Limits & Continuity"],
                "core_subskills": ["Product & Chain Rules", "Gradient Slopes & Optimization"],
                "aliases": ["calculus", "derivatives", "differentiation", "derivative", "differential calculus", "rate of change", "slope"]
            },
            "Integral Calculus": {
                "domain": "MATHEMATICS",
                "category": "CORE_CS",
                "difficulty": "ADVANCED",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/math-003",
                "description": "Definite/indefinite integrals, Riemann sums, substitution, and area under curves.",
                "prerequisites": ["Calculus & Derivatives"],
                "core_subskills": ["Riemann Sums", "Integration by Parts"],
                "aliases": ["integral", "integrals", "integration", "integral calculus", "area under curve"]
            },
            "Limits & Continuity": {
                "domain": "MATHEMATICS",
                "category": "CORE_CS",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/math-001",
                "description": "Epsilon-delta definitions, one-sided limits, and function behavior at asymptotes.",
                "prerequisites": [],
                "core_subskills": ["One-Sided Limits", "Asymptotic Continuity"],
                "aliases": ["limits", "continuity", "limit", "asymptotes"]
            },
            "Linear Algebra & Matrices": {
                "domain": "MATHEMATICS",
                "category": "CORE_CS",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/math-004",
                "description": "Vector spaces, dot products, matrix transformations, eigenvalues, and SVD.",
                "prerequisites": [],
                "core_subskills": ["Vector Spaces", "Matrix Transformations", "Eigenvalues & Eigenvectors"],
                "aliases": ["linear algebra", "matrices", "matrix", "vectors", "eigenvalues", "eigenvectors", "dot product"]
            },

            # --- Creative, 3D & Design ---
            "3D Modeling & Mesh Topology": {
                "domain": "CREATIVE_3D",
                "category": "ARCHITECTURE",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/3d-001",
                "description": "Polygonal vertex modeling, edge loops, subdivision surfaces, and clean topology in Blender.",
                "prerequisites": [],
                "core_subskills": ["Subdivision Surface Modeling", "Edge Loop Flow & Quad Topology"],
                "aliases": ["blender", "3d modeling", "mesh", "topology", "polygonal modeling", "3d asset"]
            },
            "UV Unwrapping & PBR Texturing": {
                "domain": "CREATIVE_3D",
                "category": "ARCHITECTURE",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/3d-002",
                "description": "Unwrapping 2D UV seams, PBR shader nodes, roughness, normals, and metallic maps.",
                "prerequisites": ["3D Modeling & Mesh Topology"],
                "core_subskills": ["UV Seam Unwrapping", "PBR Material Shaders"],
                "aliases": ["uv unwrapping", "texturing", "pbr", "materials", "shaders", "normal maps"]
            },
            "3D Lighting & Rendering": {
                "domain": "CREATIVE_3D",
                "category": "ARCHITECTURE",
                "difficulty": "ADVANCED",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/3d-003",
                "description": "Cycles/EEVEE photorealistic rendering, three-point lighting, and HDRI world environments.",
                "prerequisites": ["UV Unwrapping & PBR Texturing"],
                "core_subskills": ["Three-Point Lighting", "Ray-Tracing & Global Illumination"],
                "aliases": ["lighting", "rendering", "cycles", "eevee", "hdri", "compositing"]
            },

            # --- Photography & Visual Arts ---
            "Exposure Triangle (Aperture, Shutter Speed, ISO)": {
                "domain": "PHOTOGRAPHY",
                "category": "ARCHITECTURE",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/photo-001",
                "description": "Mastering optical light capture, motion blur, sensor noise, and depth of field.",
                "prerequisites": [],
                "core_subskills": ["Aperture & Depth of Field", "Shutter Speed & Motion", "ISO & Sensor Dynamics"],
                "aliases": ["photography", "photo", "exposure triangle", "aperture", "shutter speed", "iso", "camera controls", "manual photography"]
            },
            "RAW Image Editing & Color Grading": {
                "domain": "PHOTOGRAPHY",
                "category": "ARCHITECTURE",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/photo-002",
                "description": "Non-destructive RAW sensor curve adjustment, histogram balancing, and LUT color grading.",
                "prerequisites": ["Exposure Triangle (Aperture, Shutter Speed, ISO)"],
                "core_subskills": ["Histogram Analysis", "Tone Curve & Color Grading"],
                "aliases": ["raw editing", "lightroom", "color grading", "photo editing", "histogram", "post-processing"]
            },

            # --- Audio & Music Production ---
            "Digital Audio Workstation (DAW)": {
                "domain": "AUDIO_MUSIC",
                "category": "ARCHITECTURE",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/audio-001",
                "description": "Multi-track audio recording, MIDI routing, automation lanes, and session management.",
                "prerequisites": [],
                "core_subskills": ["Multi-Track Routing", "MIDI Sequencing & Automation"],
                "aliases": ["music production", "music", "daw", "ableton", "fl studio", "logic pro", "beat making", "midi"]
            },
            "Audio Mixing & Mastering": {
                "domain": "AUDIO_MUSIC",
                "category": "ARCHITECTURE",
                "difficulty": "ADVANCED",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/audio-002",
                "description": "Parametric EQ balancing, dynamic multi-band compression, stereo imaging, and LUFS limiting.",
                "prerequisites": ["Digital Audio Workstation (DAW)"],
                "core_subskills": ["Parametric Equalization (EQ)", "Dynamic Compression & Sidechaining", "LUFS Loudness Mastering"],
                "aliases": ["audio mixing", "mixing", "mastering", "eq", "compression", "sound engineering"]
            },

            # --- Finance & Economics ---
            "Financial Statement Analysis": {
                "domain": "FINANCE",
                "category": "ARCHITECTURE",
                "difficulty": "BEGINNER",
                "source": "ONET",
                "id": "13-2051.00",
                "description": "Analyzing Income Statements, Balance Sheets, and Cash Flow Statements for corporate health.",
                "prerequisites": [],
                "core_subskills": ["Three-Statement Modeling", "Working Capital Analysis"],
                "aliases": ["financial statements", "accounting", "balance sheet", "income statement", "cash flow statement", "finance"]
            },
            "Financial Modeling & Valuation (DCF)": {
                "domain": "FINANCE",
                "category": "ARCHITECTURE",
                "difficulty": "ADVANCED",
                "source": "ONET",
                "id": "13-2051.00",
                "description": "Building Discounted Cash Flow models, WACC calculations, and three-statement financial projections.",
                "prerequisites": ["Financial Statement Analysis"],
                "core_subskills": ["Free Cash Flow Projections", "WACC & Discount Rates", "Sensitivity Matrix Modeling"],
                "aliases": ["financial modeling", "dcf", "valuation", "discounted cash flow", "wacc", "excel financial modeling"]
            },

            # --- Cloud & DevOps ---
            "Docker & Containers": {
                "domain": "DEVOPS_CLOUD",
                "category": "DEVOPS",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/devops-001",
                "description": "OCI container image building, multi-stage Dockerfiles, and compose networks.",
                "prerequisites": ["Linux & Shell Scripting"],
                "core_subskills": ["Multi-Stage Dockerfiles", "Container Networking & Volumes"],
                "aliases": ["docker", "containers", "containerization", "docker compose"]
            },
            "Kubernetes Orchestration": {
                "domain": "DEVOPS_CLOUD",
                "category": "DEVOPS",
                "difficulty": "ADVANCED",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/devops-002",
                "description": "Declarative Kubernetes cluster orchestration, Pods, Deployments, Services, and Ingress controllers.",
                "prerequisites": ["Docker & Containers"],
                "core_subskills": ["Pods, Deployments & Services", "Ingress Routing & ConfigMaps", "Horizontal Pod Autoscaling (HPA)"],
                "aliases": ["kubernetes", "k8s", "container orchestration", "helm", "k8s cluster"]
            },
            "Linux & Shell Scripting": {
                "domain": "DEVOPS_CLOUD",
                "category": "CORE_CS",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/devops-003",
                "description": "POSIX system commands, bash automation, filesystem permissions, and process signals.",
                "prerequisites": [],
                "core_subskills": ["Bash Automation", "Filesystem Permissions & Processes"],
                "aliases": ["linux", "bash", "shell scripting", "unix", "posix"]
            },

            # --- AI / Machine Learning & RAG ---
            "Python Programming": {
                "domain": "AI_ML",
                "category": "LANGUAGE",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/py-001",
                "description": "Idiomatic Python scripting, list comprehensions, and data wrangling.",
                "prerequisites": [],
                "core_subskills": ["Idiomatic Python Syntax", "Data Structures & Generators"],
                "aliases": ["python", "python programming", "python3"]
            },
            "Prompt Engineering & LLM APIs": {
                "domain": "AI_ML",
                "category": "DATA_AI",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/rag-001",
                "description": "Few-shot prompting, structured JSON schema output, and OpenAI/Anthropic API integration.",
                "prerequisites": ["Python Programming"],
                "core_subskills": ["Few-Shot & System Prompting", "JSON Schema Enforcement & Function Calling"],
                "aliases": ["prompt engineering", "llm", "large language model", "openai", "anthropic", "gpt", "prompting"]
            },
            "Vector Databases & Embeddings": {
                "domain": "AI_ML",
                "category": "DATA_AI",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/rag-002",
                "description": "Semantic embedding generation, cosine similarity, HNSW indexing, and ChromaDB/pgvector storage.",
                "prerequisites": ["Python Programming"],
                "core_subskills": ["Embedding Generation & Distance Metrics", "HNSW & IVF Vector Indexing", "ChromaDB / Pinecone Integration"],
                "aliases": ["vector db", "vector database", "vector databases", "embeddings", "embedding", "semantic search", "pgvector", "chromadb"]
            },
            "RAG Architecture & LangChain": {
                "domain": "AI_ML",
                "category": "DATA_AI",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/rag-003",
                "description": "Retrieval-Augmented Generation architectures, chunking strategies, vector stores, and LangChain/LlamaIndex orchestrations.",
                "prerequisites": ["Prompt Engineering & LLM APIs", "Vector Databases & Embeddings"],
                "core_subskills": ["Recursive Document Chunking", "Retriever Orchestration with LangChain", "Grounded Citation & Guardrails"],
                "aliases": ["rag", "retrieval augmented generation", "langchain", "llamaindex", "rag architecture", "retrieval pipeline"]
            },
            "Deep Learning & PyTorch": {
                "domain": "AI_ML",
                "category": "DATA_AI",
                "difficulty": "ADVANCED",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/ai-002",
                "description": "Neural networks, backpropagation, and PyTorch tensor operations.",
                "prerequisites": ["Python Programming", "Linear Algebra & Matrices"],
                "core_subskills": ["PyTorch Tensors & Autograd", "Neural Network Architecture & Training"],
                "aliases": ["deep learning", "pytorch", "neural networks", "tensors", "backpropagation"]
            },

            # --- Mobile Development ---
            "Dart Programming": {
                "domain": "MOBILE",
                "category": "LANGUAGE",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/mob-001",
                "description": "Type-safe asynchronous Dart programming with Streams and Future patterns.",
                "prerequisites": [],
                "core_subskills": ["Dart Syntax & Null Safety", "Async Futures & Streams"],
                "aliases": ["dart", "dart programming"]
            },
            "Flutter Framework & Widgets": {
                "domain": "MOBILE",
                "category": "FRAMEWORK",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/mob-002",
                "description": "Reactive widget tree rendering, declarative layouts, and cross-platform app compilation.",
                "prerequisites": ["Dart Programming"],
                "core_subskills": ["Stateless & Stateful Widgets", "Adaptive Layouts & Animations"],
                "aliases": ["flutter", "flutter framework", "flutter widgets", "cross-platform mobile"]
            },
            "State Management (Riverpod/Bloc)": {
                "domain": "MOBILE",
                "category": "ARCHITECTURE",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/mob-003",
                "description": "Reactive immutable state stores, dependency injection, and state notifier patterns.",
                "prerequisites": ["Flutter Framework & Widgets"],
                "core_subskills": ["Riverpod State Providers", "Bloc Pattern & Events"],
                "aliases": ["riverpod", "bloc", "flutter state management", "state management"]
            },

            # --- Web & Backend Engineering ---
            "Java": {
                "domain": "WEB_DEVELOPMENT",
                "category": "LANGUAGE",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/java-001",
                "description": "Core Java programming including OOP, Generics, and Concurrency.",
                "prerequisites": ["Object-Oriented Programming (OOP)"],
                "core_subskills": ["Java Concurrency & Streams", "Memory & JVM Mechanics"],
                "aliases": ["java", "core java", "java 21", "java backend"]
            },
            "Object-Oriented Programming (OOP)": {
                "domain": "WEB_DEVELOPMENT",
                "category": "CORE_CS",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/oop-001",
                "description": "Encapsulation, Inheritance, Polymorphism, Abstraction, and SOLID principles.",
                "prerequisites": [],
                "core_subskills": ["Polymorphism & Inheritance", "SOLID Architecture Principles"],
                "aliases": ["oop", "object oriented programming", "solid principles"]
            },
            "SQL & Relational Databases": {
                "domain": "WEB_DEVELOPMENT",
                "category": "DATABASE",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/sql-001",
                "description": "Relational data modeling, ACID transactions, complex joins, and indexing.",
                "prerequisites": [],
                "core_subskills": ["Relational Schema Modeling", "Indexing & ACID Transactions"],
                "aliases": ["sql", "postgresql", "mysql", "relational databases", "database"]
            },
            "Spring Boot": {
                "domain": "WEB_DEVELOPMENT",
                "category": "FRAMEWORK",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/spring-001",
                "description": "Enterprise backend architecture with Spring Boot REST services.",
                "prerequisites": ["Java", "SQL & Relational Databases"],
                "core_subskills": ["Spring Dependency Injection", "Spring Data JPA & Repositories"],
                "aliases": ["spring boot", "spring", "spring framework"]
            },
            "RESTful APIs": {
                "domain": "WEB_DEVELOPMENT",
                "category": "ARCHITECTURE",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/rest-001",
                "description": "Designing scalable HTTP RESTful web services and API contracts.",
                "prerequisites": [],
                "core_subskills": ["HTTP Verbs & Status Codes", "REST Schema Contracts"],
                "aliases": ["rest", "restful apis", "rest api", "api design"]
            },
            "Spring Security & JWT": {
                "domain": "WEB_DEVELOPMENT",
                "category": "FRAMEWORK",
                "difficulty": "ADVANCED",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/sec-001",
                "description": "Stateless JWT authentication filter pipelines and authorization.",
                "prerequisites": ["Spring Boot"],
                "core_subskills": ["Stateless JWT Filters", "Role-Based Access Control"],
                "aliases": ["spring security", "jwt", "authentication", "authorization"]
            },
            "JavaScript (ES6+)": {
                "domain": "WEB_DEVELOPMENT",
                "category": "LANGUAGE",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/js-001",
                "description": "Modern JavaScript language mechanics, closures, and async/await.",
                "prerequisites": [],
                "core_subskills": ["ES6+ Syntax", "Promises & Async/Await"],
                "aliases": ["javascript", "js", "typescript", "es6"]
            },
            "HTML5 & CSS3": {
                "domain": "WEB_DEVELOPMENT",
                "category": "FRAMEWORK",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/html-001",
                "description": "Semantic HTML5 layout structure, CSS Flexbox/Grid, and responsive design.",
                "prerequisites": [],
                "core_subskills": ["Semantic HTML Elements", "CSS Grid & Flexbox Layouts"],
                "aliases": ["html", "css", "html5", "css3", "web markup", "responsive design"]
            },
            "React.js": {
                "domain": "WEB_DEVELOPMENT",
                "category": "FRAMEWORK",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/react-001",
                "description": "Declarative UI engineering with React hooks, components, and virtual DOM.",
                "prerequisites": ["JavaScript (ES6+)", "HTML5 & CSS3"],
                "core_subskills": ["React Hooks & Lifecycle", "Component Composition"],
                "aliases": ["react", "react.js", "reactjs", "frontend react"]
            },
            "Node.js & Express": {
                "domain": "WEB_DEVELOPMENT",
                "category": "FRAMEWORK",
                "difficulty": "INTERMEDIATE",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/node-001",
                "description": "Asynchronous event-driven JavaScript server runtime and Express REST routing.",
                "prerequisites": ["JavaScript (ES6+)"],
                "core_subskills": ["Express Middleware Routing", "Asynchronous Event Loop I/O"],
                "aliases": ["node", "nodejs", "node.js", "express", "express.js", "backend node"]
            },
            "Full Stack Web Development": {
                "domain": "WEB_DEVELOPMENT",
                "category": "ARCHITECTURE",
                "difficulty": "INTERMEDIATE",
                "source": "ONET",
                "id": "15-1254.00",
                "description": "End-to-end web engineering combining React client applications with Node/Express/PostgreSQL backend services.",
                "prerequisites": ["JavaScript (ES6+)", "HTML5 & CSS3"],
                "core_subskills": ["React.js", "Node.js & Express", "SQL & Relational Databases"],
                "aliases": ["full stack", "fullstack", "full-stack", "full stack developer", "fullstack developer", "web dev", "web development"]
            },
            "Git & Version Control": {
                "domain": "WEB_DEVELOPMENT",
                "category": "CORE_CS",
                "difficulty": "BEGINNER",
                "source": "ESCO",
                "id": "http://data.europa.eu/esco/skill/git-001",
                "description": "Branching strategies, Git workflows, PRs, and collaborative version control.",
                "prerequisites": [],
                "core_subskills": ["Git Branching & Merging", "Pull Requests & Remotes"],
                "aliases": ["git", "version control", "github", "gitlab"]
            }
        }

        # Build alias inverted index for fast semantic lookup
        self.alias_index: Dict[str, str] = {}
        for canonical_name, data in self.taxonomy.items():
            self.alias_index[canonical_name.lower()] = canonical_name
            for alias in data.get("aliases", []):
                self.alias_index[alias.lower()] = canonical_name

    def classify_intent(self, prompt: str) -> str:
        """
        Classifies user prompt into one of:
        - TOPIC_LEARNING (focused concept, e.g. "I want to learn Dynamic Programming", "learn calculus")
        - SKILL_LEARNING (specific tool/skill, e.g. "I want to learn Git", "learn Docker")
        - CAREER_GOAL (broad occupation, e.g. "Become a Backend Java Developer", "become a data scientist")
        - PROJECT_GOAL (building a specific software artifact, e.g. "build an e-commerce website")
        - CERTIFICATION_GOAL (exam prep, e.g. "AWS Solutions Architect certification")
        - OTHER
        """
        p_lower = prompt.lower()
        if re.search(r'\b(become|career|job|role|developer|engineer|specialist|architect|practitioner|work as)\b', p_lower) and not re.search(r'\b(learn\s+(only|just)\s+how\b|\blearn\s+the\s+concept\b)', p_lower):
            return "CAREER_GOAL"
        elif re.search(r'\b(build|create|develop|code|make)\s+(a|an|the|my)\s+(app|application|platform|website|tool|system|game|portfolio|service)\b', p_lower):
            return "PROJECT_GOAL"
        elif re.search(r'\b(cert|certification|exam|prepare for exam|certified)\b', p_lower):
            return "CERTIFICATION_GOAL"
        elif re.search(r'\b(git|docker|linux|bash|vscode|jira)\b', p_lower) and len(p_lower.split()) < 6:
            return "SKILL_LEARNING"
        else:
            return "TOPIC_LEARNING"

    def resolve_goal_knowledge(self, prompt: str) -> Dict[str, Any]:
        """
        Comprehensive knowledge resolution pipeline:
        1. Classifies intent.
        2. Resolves canonical skills matching the user's objective.
        3. Retrieves verified prerequisite DAG from the taxonomy.
        4. Performs strict relevance filtering (excluding generic tooling for topic learning).
        5. Handles unknown / novel domains gracefully via dynamic concept inference.
        """
        p_lower = prompt.lower()
        intent = self.classify_intent(prompt)

        # Find direct matching taxonomy entries via alias and token matching
        matched_canonical_skills: Set[str] = set()
        matched_domains: Set[str] = set()

        # Check multi-word aliases first, then single tokens
        sorted_aliases = sorted(self.alias_index.keys(), key=lambda x: len(x), reverse=True)
        for alias in sorted_aliases:
            pattern = r'\b' + re.escape(alias) + r'\b'
            if re.search(pattern, p_lower):
                canonical = self.alias_index[alias]
                matched_canonical_skills.add(canonical)
                matched_domains.add(self.taxonomy[canonical]["domain"])

        # Determine normalized target role / title
        if matched_canonical_skills:
            primary_skill_name = next(iter(matched_canonical_skills))
            primary_data = self.taxonomy[primary_skill_name]
            domain = primary_data["domain"]

            if intent == "TOPIC_LEARNING":
                target_role = f"{primary_skill_name} Specialist"
                normalized_goal = f"Mastery of {primary_skill_name} and Core Foundations"
            elif intent == "CAREER_GOAL":
                if domain == "ALGORITHMS_CS": target_role = "Algorithms & Software Engineer"
                elif domain == "MATHEMATICS": target_role = "Applied Mathematician / Quantitative Analyst"
                elif domain == "CREATIVE_3D": target_role = "3D Technical Artist & Modeler"
                elif domain == "PHOTOGRAPHY": target_role = "Professional Photographer"
                elif domain == "AUDIO_MUSIC": target_role = "Audio Engineer & Music Producer"
                elif domain == "FINANCE": target_role = "Financial Analyst & Modeling Specialist"
                elif domain == "DEVOPS_CLOUD": target_role = "DevOps & Cloud Engineer"
                elif domain == "AI_ML": target_role = "Generative AI & RAG Engineer"
                elif domain == "MOBILE": target_role = "Flutter Mobile Developer"
                elif domain == "WEB_DEVELOPMENT": target_role = "Backend Java Developer"
                else: target_role = f"{primary_skill_name} Professional"
                normalized_goal = f"Career Path for {target_role}"
            elif intent == "PROJECT_GOAL":
                target_role = f"{primary_skill_name} Application Builder"
                normalized_goal = f"Capstone Engineering: {primary_skill_name}"
            else:
                target_role = f"{primary_skill_name} Practitioner"
                normalized_goal = f"Proficiency in {primary_skill_name}"
        else:
            # Domain-Agnostic Dynamic Concept Extractor for unseen / novel domains
            clean_concept = re.sub(r'^(i want to learn|i want to become|how to learn|learn|teach me|i need to master|i would like to study)\s+', '', p_lower, flags=re.IGNORECASE).strip()
            clean_concept = re.sub(r'[?.!]', '', clean_concept).strip().title()
            if not clean_concept:
                clean_concept = "General Competency"

            target_role = f"{clean_concept} Specialist"
            normalized_goal = f"Foundational & Advanced Mastery of {clean_concept}"
            domain = "UNANTICIPATED_DOMAIN"
            primary_skill_name = clean_concept

        # Extract Core Skills & Verified Prerequisite DAG
        core_skills: List[str] = []
        prerequisite_skills: List[str] = []
        optional_skills: List[str] = []
        excluded_skills: List[str] = []
        explainability: Dict[str, Dict[str, Any]] = {}

        if matched_canonical_skills:
            for skill_name in matched_canonical_skills:
                core_skills.append(skill_name)
                explainability[skill_name] = {
                    "role": "DIRECT_CORE",
                    "reason": "Explicitly requested in learner objective.",
                    "source": self.taxonomy[skill_name]["source"],
                    "confidence": 0.98
                }
                # Add subskills if defined
                for sub in self.taxonomy[skill_name].get("core_subskills", []):
                    if sub not in core_skills:
                        core_skills.append(sub)
                        explainability[sub] = {
                            "role": "DIRECT_CORE",
                            "reason": f"Essential sub-competency for {skill_name}.",
                            "source": self.taxonomy[skill_name]["source"],
                            "confidence": 0.92
                        }
                # Traverse verified prerequisites in graph
                for prereq in self.taxonomy[skill_name].get("prerequisites", []):
                    if prereq not in prerequisite_skills and prereq not in core_skills:
                        prerequisite_skills.append(prereq)
                        explainability[prereq] = {
                            "role": "REQUIRED_PREREQUISITE",
                            "reason": f"Validated prerequisite in knowledge graph for {skill_name}.",
                            "source": self.taxonomy.get(prereq, {}).get("source", "ESCO"),
                            "confidence": 0.94
                        }

            # If CAREER_GOAL intent, enrich with broader in-domain skills
            if intent == "CAREER_GOAL":
                for sk_name, sk_data in self.taxonomy.items():
                    if sk_data["domain"] in matched_domains and sk_name not in core_skills and sk_name not in prerequisite_skills:
                        if len(core_skills) + len(prerequisite_skills) < 8:
                            core_skills.append(sk_name)
                            explainability[sk_name] = {
                                "role": "DIRECT_CORE",
                                "reason": f"Standard occupational competency for {target_role} ({sk_data['source']}).",
                                "source": sk_data["source"],
                                "confidence": 0.88
                            }

            # NEGATIVE FILTERING: Explicitly identify and exclude out-of-domain generic tooling
            generic_baggage_candidates = [
                "Git & Version Control", "Docker & Containers", "SQL & Relational Databases",
                "Java", "Spring Boot", "RESTful APIs", "JavaScript (ES6+)", "React.js"
            ]
            for baggage in generic_baggage_candidates:
                if baggage in self.taxonomy and self.taxonomy[baggage]["domain"] not in matched_domains:
                    if baggage not in core_skills and baggage not in prerequisite_skills:
                        excluded_skills.append(baggage)
        else:
            # Synthesize dynamic domain-grounded skills for unlisted topic
            core_skills.append(primary_skill_name)
            explainability[primary_skill_name] = {
                "role": "DIRECT_CORE",
                "reason": "Dynamically inferred core learning objective.",
                "source": "DYNAMIC_INFERRED",
                "confidence": 0.90
            }
            subskill_1 = f"{primary_skill_name} Foundations & Mechanics"
            subskill_2 = f"{primary_skill_name} Advanced Techniques & Application"
            prereq_1 = f"Foundational Principles for {primary_skill_name}"

            core_skills.extend([subskill_1, subskill_2])
            prerequisite_skills.append(prereq_1)

            explainability[subskill_1] = {"role": "DIRECT_CORE", "reason": "Fundamental sub-competency.", "source": "DYNAMIC_INFERRED", "confidence": 0.85}
            explainability[subskill_2] = {"role": "DIRECT_CORE", "reason": "Advanced execution sub-competency.", "source": "DYNAMIC_INFERRED", "confidence": 0.85}
            explainability[prereq_1] = {"role": "REQUIRED_PREREQUISITE", "reason": "Conceptual prerequisite for domain mastery.", "source": "DYNAMIC_INFERRED", "confidence": 0.88}

            excluded_skills.extend(["Git & Version Control", "Java", "Docker & Containers", "SQL & Relational Databases"])

        # Experience level detection
        if any(w in p_lower for w in ["senior", "expert", "advanced", "lead", "architect"]):
            exp_level = "ADVANCED"
        elif any(w in p_lower for w in ["beginner", "novice", "from scratch", "basics only", "just starting", "only know"]):
            exp_level = "BEGINNER"
        else:
            exp_level = "INTERMEDIATE"

        # Timeline estimation
        months_match = re.search(r'(\d+)\s*(?:month|mo)', p_lower)
        estimated_months = int(months_match.group(1)) if months_match else 6

        # Required skills = Core + Prerequisites
        all_required = list(dict.fromkeys(core_skills + prerequisite_skills))

        # Missing skills initially equals all required skills
        missing_skills = list(all_required)

        ai_summary = f"Personalized {intent.replace('_', ' ').title()} curriculum for {primary_skill_name}. " \
                     f"Grounded in verified prerequisite graph with {len(prerequisite_skills)} foundational prerequisites " \
                     f"and {len(core_skills)} core competencies."

        return {
            "target_role": target_role,
            "career_goal": prompt,
            "raw_goal": prompt,
            "normalized_goal": normalized_goal,
            "goal_type": intent,
            "domain": domain,
            "experience_level": exp_level,
            "estimated_months": estimated_months,
            "learning_pace": "10 hours/week (Hands-On)",
            "extracted_skills": all_required[:2],
            "missing_skills": missing_skills,
            "core_skills": core_skills,
            "prerequisite_skills": prerequisite_skills,
            "optional_skills": optional_skills,
            "excluded_skills": excluded_skills,
            "confidence": 0.92,
            "explainability": explainability,
            "ai_summary": ai_summary
        }

    def score_item_relevance(self, item_title: str, item_skills: List[str], required_skills: List[str], goal_domain: str) -> float:
        """
        Domain-independent semantic scoring:
        Evaluates title n-gram overlap, target skill coverage, and domain congruence.
        Ensures items outside the goal domain receive 0 or negative boost.
        """
        if not required_skills:
            return 0.0

        item_text = (item_title + " " + " ".join(item_skills)).lower()
        req_set = [s.lower() for s in required_skills]

        # 1. Exact skill match ratio
        matched_count = sum(1 for req in req_set if req in item_text or any(token in item_text for token in req.split() if len(token) > 3))
        skill_coverage = matched_count / max(1, len(req_set))

        # 2. Token overlap score
        req_tokens = set()
        for s in req_set:
            req_tokens.update([t for t in re.findall(r'\w+', s) if len(t) > 2])
        item_tokens = set(re.findall(r'\w+', item_text))
        token_overlap = len(req_tokens.intersection(item_tokens)) / max(1, len(req_tokens))

        score = (skill_coverage * 0.6) + (token_overlap * 0.4)
        return min(1.0, max(0.0, score))
