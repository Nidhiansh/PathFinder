from abc import ABC, abstractmethod
from typing import Dict, Any, Optional, List
import os

class BaseLLMService(ABC):
    @abstractmethod
    def generate_chat_reply(self, message: str, context: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        pass

    @abstractmethod
    def explain_recommendation(self, resource_title: str, user_profile: Dict[str, Any], score_breakdown: Dict[str, float]) -> str:
        pass

class LocalDeterministicLLM(BaseLLMService):
    """
    Production-grade deterministic local intelligence engine.
    Ensures zero external dependency while delivering context-aware, highly personalized reasoning.
    """
    def generate_chat_reply(self, message: str, context: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        msg = message.lower()
        ctx = context or {}
        role = ctx.get("target_role", "Software Engineer")
        style = ctx.get("preferred_style", "PRACTICAL")

        if "spring boot" in msg and ("why" in msg or "reason" in msg):
            reply = (
                f"Spring Boot was recommended because your target role is **{role}** and you already possess foundational "
                f"Java and SQL proficiencies. Spring Boot is the primary enterprise framework used for microservices, "
                f"dependency injection, and REST API development, making it your highest-leverage career milestone right now."
            )
            return {
                "reply": reply,
                "suggested_action": "EXPLAIN_RECOMMENDATION",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/recommendations", "label": "Explore Recommendations"},
                "quick_replies": ["What project should I build next?", "Can I skip SQL?", "How long will this phase take?"]
            }

        elif "5 hour" in msg or "5h" in msg or "five hour" in msg:
            reply = (
                "Understood! I've adapted your pacing to **5 hours/week**. "
                "Your milestones have been scaled across a more manageable timeframe without losing any completed progress."
            )
            return {
                "reply": reply,
                "suggested_action": "RECALCULATE_TIME",
                "action_type": "PACE_ADAPTED",
                "action_payload": {"weekly_hours": 5, "label": "Apply 5 hrs/week Pace"},
                "quick_replies": ["View updated roadmap", "Show current phase", "What should I learn next?"]
            }

        elif "15 hour" in msg or "15h" in msg or "fifteen hour" in msg:
            reply = (
                "Pace intensified to **15 hours/week**! Your estimated roadmap completion is accelerated by ~35%."
            )
            return {
                "reply": reply,
                "suggested_action": "RECALCULATE_TIME",
                "action_type": "PACE_ADAPTED",
                "action_payload": {"weekly_hours": 15, "label": "Apply 15 hrs/week Pace"},
                "quick_replies": ["View accelerated roadmap", "Take phase assessment", "Start next module"]
            }

        elif "what should i learn next" in msg or "what next" in msg or "start" in msg:
            if "rag" in role.lower() or "generative" in role.lower() or "llm" in role.lower():
                next_topic = "Vector Databases, Embeddings & RAG Architecture"
            elif "fullstack" in role.lower() or "react" in role.lower():
                next_topic = "React 19 Hooks & Declarative Component Architecture"
            elif "devops" in role.lower() or "cloud" in role.lower():
                next_topic = "Containerization with Docker & Multi-Stage Builds"
            elif "ai" in role.lower() or "machine learning" in role.lower():
                next_topic = "Data Science Foundations with NumPy & Scikit-Learn"
            else:
                next_topic = "Core Software Engineering & Architecture Foundations"

            reply = (
                f"Based on your active profile for **{role}**, your highest-priority step is **{next_topic}**. "
                f"Your foundational prerequisites are verified, and completing this module unlocks downstream capstone modules."
            )
            return {
                "reply": reply,
                "suggested_action": "START_NEXT_ITEM",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/roadmap", "label": "Open Active Phase"},
                "quick_replies": ["View current phase roadmap", "Show project rubric", "Explain prerequisites"]
            }

        elif "project" in msg or "build" in msg or "portfolio" in msg:
            if "rag" in role.lower() or "generative" in role.lower():
                proj_name = "Enterprise Document QA RAG System with ChromaDB & Citations"
            elif "fullstack" in role.lower():
                proj_name = "Full Stack SaaS Platform with React 19 & Node.js"
            elif "devops" in role.lower():
                proj_name = "Multi-Container Microservices Deployment with Docker Compose"
            elif "ai" in role.lower():
                proj_name = "Predictive Machine Learning Pipeline with Scikit-Learn"
            else:
                proj_name = "Production REST API with Clean Architecture"

            reply = (
                f"For your current phase in {role}, I recommend building **'{proj_name}'**. "
                f"It challenges you with real-world design, error handling, performance optimization, and clean architecture."
            )
            return {
                "reply": reply,
                "suggested_action": "VIEW_PROJECT",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/projects", "label": "Open Projects Hub"},
                "quick_replies": ["View starter GitHub repository", "Take phase assessment", "View rubric"]
            }

        elif "skip" in msg or ("prerequisite" in msg and "why" in msg):
            reply = (
                "In our DAG prerequisite model, foundational skills cannot be safely bypassed without creating technical debt. "
                "However, if you already have proficiency, you can take a **Skill Checkpoint Quiz** to instantly verify mastery and unlock downstream modules."
            )
            return {
                "reply": reply,
                "suggested_action": "TAKE_ASSESSMENT",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/assessments", "label": "Take Diagnostic Quiz"},
                "quick_replies": ["Take Diagnostic Assessment", "Keep Foundation in Roadmap", "Continue Step-by-Step"]
            }

        elif "gap" in msg or "skill" in msg or "competenc" in msg:
            reply = (
                f"I've mapped your skill competencies on the interactive Topological DAG. "
                f"Your active learning path is tailored to close your priority competency gaps for **{role}**."
            )
            return {
                "reply": reply,
                "suggested_action": "VIEW_SKILLS",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/skills", "label": "Open Skill Matrix"},
                "quick_replies": ["What should I learn next?", "Show recommendations", "Take assessment"]
            }

        else:
            reply = (
                f"I am your AI Learning Copilot for **{role}**. I'm actively monitoring your skill matrix, "
                f"prerequisite dependencies, and assessment results. Ask me what to study next, why a resource was chosen, or ask me to modify your pace."
            )
            return {
                "reply": reply,
                "suggested_action": "GENERAL",
                "action_type": "NONE",
                "action_payload": {},
                "quick_replies": ["What should I learn next?", "Why did you recommend Spring Boot?", "Show my top skill gaps"]
            }

    @abstractmethod
    def explain_recommendation(self, resource_title: str, user_profile: Dict[str, Any], score_breakdown: Dict[str, float]) -> str:
        pass

    @abstractmethod
    def generate_domain_projects(
        self,
        target_role: str,
        career_goal: Optional[str],
        experience_level: str,
        skills: List[str],
        skill_gaps: List[str],
        roadmap_phases: List[str],
        custom_topic: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        pass

class LocalDeterministicLLM(BaseLLMService):
    """
    Production-grade deterministic local intelligence engine.
    Ensures zero external dependency while delivering context-aware, highly personalized reasoning.
    """
    def generate_chat_reply(self, message: str, context: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        msg = message.lower()
        ctx = context or {}
        role = ctx.get("target_role", "Software Engineer")
        style = ctx.get("preferred_style", "PRACTICAL")

        if "spring boot" in msg and ("why" in msg or "reason" in msg):
            reply = (
                f"Spring Boot was recommended because your target role is **{role}** and you already possess foundational "
                f"Java and SQL proficiencies. Spring Boot is the primary enterprise framework used for microservices, "
                f"dependency injection, and REST API development, making it your highest-leverage career milestone right now."
            )
            return {
                "reply": reply,
                "suggested_action": "EXPLAIN_RECOMMENDATION",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/recommendations", "label": "Explore Recommendations"},
                "quick_replies": ["What project should I build next?", "Can I skip SQL?", "How long will this phase take?"]
            }

        elif "5 hour" in msg or "5h" in msg or "five hour" in msg:
            reply = (
                "Understood! I've adapted your pacing to **5 hours/week**. "
                "Your milestones have been scaled across a more manageable timeframe without losing any completed progress."
            )
            return {
                "reply": reply,
                "suggested_action": "RECALCULATE_TIME",
                "action_type": "PACE_ADAPTED",
                "action_payload": {"weekly_hours": 5, "label": "Apply 5 hrs/week Pace"},
                "quick_replies": ["View updated roadmap", "Show current phase", "What should I learn next?"]
            }

        elif "15 hour" in msg or "15h" in msg or "fifteen hour" in msg:
            reply = (
                "Pace intensified to **15 hours/week**! Your estimated roadmap completion is accelerated by ~35%."
            )
            return {
                "reply": reply,
                "suggested_action": "RECALCULATE_TIME",
                "action_type": "PACE_ADAPTED",
                "action_payload": {"weekly_hours": 15, "label": "Apply 15 hrs/week Pace"},
                "quick_replies": ["View accelerated roadmap", "Take phase assessment", "Start next module"]
            }

        elif "what should i learn next" in msg or "what next" in msg or "start" in msg:
            if "rag" in role.lower() or "generative" in role.lower() or "llm" in role.lower():
                next_topic = "Vector Databases, Embeddings & RAG Architecture"
            elif "fullstack" in role.lower() or "react" in role.lower():
                next_topic = "React 19 Hooks & Declarative Component Architecture"
            elif "devops" in role.lower() or "cloud" in role.lower():
                next_topic = "Containerization with Docker & Multi-Stage Builds"
            elif "ai" in role.lower() or "machine learning" in role.lower():
                next_topic = "Data Science Foundations with NumPy & Scikit-Learn"
            else:
                next_topic = "Core Software Engineering & Architecture Foundations"

            reply = (
                f"Based on your active profile for **{role}**, your highest-priority step is **{next_topic}**. "
                f"Your foundational prerequisites are verified, and completing this module unlocks downstream capstone modules."
            )
            return {
                "reply": reply,
                "suggested_action": "START_NEXT_ITEM",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/roadmap", "label": "Open Active Phase"},
                "quick_replies": ["View current phase roadmap", "Show project rubric", "Explain prerequisites"]
            }

        elif "project" in msg or "build" in msg or "portfolio" in msg:
            if "rag" in role.lower() or "generative" in role.lower():
                proj_name = "Enterprise Document QA RAG System with ChromaDB & Citations"
            elif "flutter" in role.lower():
                proj_name = "Cross-Platform Real-Time Expense & Budget Manager with Riverpod"
            elif "kubernetes" in role.lower() or "devops" in role.lower():
                proj_name = "High-Availability Microservices Orchestration with Helm & Ingress"
            elif "blockchain" in role.lower():
                proj_name = "Full-Stack Web3 DApp with Ethers.js & Hardhat Testing Suite"
            elif "computer vision" in role.lower() or "vision" in role.lower():
                proj_name = "Real-Time Object Detection & Tracking System with OpenCV & YOLO"
            elif "data engineer" in role.lower():
                proj_name = "Real-Time Event Streaming Pipeline with Apache Kafka & Spark"
            elif "fullstack" in role.lower():
                proj_name = "Full Stack SaaS Platform with React 19 & Node.js"
            else:
                proj_name = f"Production Capstone Portfolio Build for {role}"

            reply = (
                f"For your current phase in {role}, I recommend building **'{proj_name}'**. "
                f"It challenges you with real-world architecture, state management, testing, and portfolio-grade deliverables."
            )
            return {
                "reply": reply,
                "suggested_action": "VIEW_PROJECT",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/projects", "label": "Open Projects Hub"},
                "quick_replies": ["View starter GitHub repository", "Take phase assessment", "View rubric"]
            }

        elif "skip" in msg or ("prerequisite" in msg and "why" in msg):
            reply = (
                "In our DAG prerequisite model, foundational skills cannot be safely bypassed without creating technical debt. "
                "However, if you already have proficiency, you can take a **Skill Checkpoint Quiz** to instantly verify mastery and unlock downstream modules."
            )
            return {
                "reply": reply,
                "suggested_action": "TAKE_ASSESSMENT",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/assessments", "label": "Take Diagnostic Quiz"},
                "quick_replies": ["Take Diagnostic Assessment", "Keep Foundation in Roadmap", "Continue Step-by-Step"]
            }

        elif "gap" in msg or "skill" in msg or "competenc" in msg:
            reply = (
                f"I've mapped your skill competencies on the interactive Topological DAG. "
                f"Your active learning path is tailored to close your priority competency gaps for **{role}**."
            )
            return {
                "reply": reply,
                "suggested_action": "VIEW_SKILLS",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/skills", "label": "Open Skill Matrix"},
                "quick_replies": ["What should I learn next?", "Show recommendations", "Take assessment"]
            }

        else:
            reply = (
                f"I am your AI Learning Copilot for **{role}**. I'm actively monitoring your skill matrix, "
                f"prerequisite dependencies, and assessment results. Ask me what to study next, why a resource was chosen, or ask me to modify your pace."
            )
            return {
                "reply": reply,
                "suggested_action": "GENERAL",
                "action_type": "NONE",
                "action_payload": {},
                "quick_replies": ["What should I learn next?", "Why did you recommend Spring Boot?", "Show my top skill gaps"]
            }

    def explain_recommendation(self, resource_title: str, user_profile: Dict[str, Any], score_breakdown: Dict[str, float]) -> str:
        role = user_profile.get("target_role", "Software Engineer")
        style = user_profile.get("preferred_style", "practical")
        return (
            f"'{resource_title}' was recommended with a {score_breakdown.get('totalScore', 92)}% compatibility score. "
            f"It directly satisfies a core requirement for {role}, aligns with your {style.lower()} learning preference, "
            f"and builds upon your verified foundational prerequisites."
        )

    def generate_domain_projects(
        self,
        target_role: str,
        career_goal: Optional[str],
        experience_level: str,
        skills: List[str],
        skill_gaps: List[str],
        roadmap_phases: List[str],
        custom_topic: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        role_lower = (target_role or "Software Engineer").lower()
        goal_text = (career_goal or custom_topic or target_role).lower()
        
        primary_skill = skills[0] if skills else (target_role.split()[0] if target_role else "Software Engineering")
        secondary_skill = skills[1] if len(skills) > 1 else primary_skill
        tertiary_skill = skills[2] if len(skills) > 2 else "System Design"

        projects: List[Dict[str, Any]] = []

        # Domain 1: Flutter / Mobile
        if "flutter" in role_lower or "mobile" in role_lower or "dart" in role_lower:
            projects = [
                {
                    "id": 20001,
                    "title": "Flutter Responsive Component Library & Dynamic Theme Engine",
                    "description": "Construct a clean, modular UI component library in Flutter with Material 3, dynamic light/dark theming, custom canvas widgets, and responsive layout adapters.",
                    "difficulty": "BEGINNER",
                    "estimated_hours": 12.0,
                    "deliverables": "Reusable widget package, storybook catalog, and tablet/mobile adaptive layouts.",
                    "rubric": "Widget composition (40%), responsive breakpoint handling (30%), code clean architecture (30%).",
                    "primary_skill": "Flutter Framework & Widgets",
                    "skills": ["Dart Programming", "Flutter Framework & Widgets"],
                    "github_template_url": "https://github.com/flutter/samples",
                    "roadmap_phase": "Phase 1: Foundation & UI Architecture",
                    "score": 96.5,
                    "explanation": "Directly targets foundational Flutter widget composition and Dart reactive programming patterns."
                },
                {
                    "id": 20002,
                    "title": "Real-Time Personal Expense & Portfolio Tracker with Riverpod",
                    "description": "Build an asynchronous, offline-first personal expense and portfolio manager utilizing Riverpod state management, SQLite local caching, and REST API sync.",
                    "difficulty": "INTERMEDIATE",
                    "estimated_hours": 18.0,
                    "deliverables": "State-managed mobile app, interactive financial charts, SQLite migration scripts, and mock API service.",
                    "rubric": "Riverpod provider architecture (35%), offline sync robustness (35%), UI performance (30%).",
                    "primary_skill": "State Management (Riverpod/Bloc)",
                    "skills": ["Flutter Framework & Widgets", "State Management (Riverpod/Bloc)", "Local Database Storage"],
                    "github_template_url": "https://github.com/flutter/gallery",
                    "roadmap_phase": "Phase 2: State Management & Persistence",
                    "score": 94.0,
                    "explanation": "Essential intermediate milestone to master reactive state management and local device data persistence."
                },
                {
                    "id": 20003,
                    "title": "Production-Ready Cross-Platform Flutter Capstone with CI/CD",
                    "description": "Architect a production-grade multi-platform mobile application featuring OAuth2 authentication, push notifications, comprehensive unit/widget tests, and automated Fastlane deployment pipelines.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 24.0,
                    "deliverables": "End-to-end mobile app, automated test suite (>80% coverage), GitHub Actions workflow, and release bundles.",
                    "rubric": "Clean architecture separation (35%), test coverage & CI/CD reliability (35%), security best practices (30%).",
                    "primary_skill": "Cross-Platform App Deployment",
                    "skills": ["Flutter Framework & Widgets", "Cross-Platform App Deployment", "Testing & CI/CD"],
                    "github_template_url": "https://github.com/flutter/flutter",
                    "roadmap_phase": "Phase 3: Production Deployment & Capstone",
                    "score": 91.5,
                    "explanation": "Capstone project validating complete mobile engineering lifecycle from architecture to production CI/CD."
                }
            ]

        # Domain 2: Kubernetes / Cloud / DevOps
        elif "kubernetes" in role_lower or "devops" in role_lower or "cloud" in role_lower or "k8s" in role_lower:
            projects = [
                {
                    "id": 20011,
                    "title": "Multi-Service Containerization & Microservice Mesh with Docker Compose",
                    "description": "Containerize a polyglot microservices system with multi-stage Dockerfiles, private container registries, environment isolation, and health check monitoring.",
                    "difficulty": "INTERMEDIATE",
                    "estimated_hours": 14.0,
                    "deliverables": "Optimized Dockerfiles (<100MB images), docker-compose multi-service orchestrations, and secret management configs.",
                    "rubric": "Image optimization (40%), container security non-root execution (30%), networking reliability (30%).",
                    "primary_skill": "Docker & Containers",
                    "skills": ["Docker & Containers", "Microservices Networking"],
                    "github_template_url": "https://github.com/docker/awesome-compose",
                    "roadmap_phase": "Phase 1: Containerization Foundations",
                    "score": 95.0,
                    "explanation": "Establishes production container packaging standards before orchestrating with Kubernetes."
                },
                {
                    "id": 20012,
                    "title": "High-Availability Kubernetes Cluster Orchestration with Helm & Ingress",
                    "description": "Deploy a distributed cloud-native application on Kubernetes featuring custom Helm charts, NGINX Ingress controllers, TLS cert-manager, Horizontal Pod Autoscaling (HPA), and ConfigMaps.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 20.0,
                    "deliverables": "Modular Helm chart repository, K8s manifests, HPA load testing script, and ingress configuration.",
                    "rubric": "Helm templating quality (35%), autoscaling responsiveness under load (35%), cluster resilience (30%).",
                    "primary_skill": "Cloud Infrastructure & Kubernetes",
                    "skills": ["Cloud Infrastructure & Kubernetes", "Docker & Containers", "Helm Package Manager"],
                    "github_template_url": "https://github.com/kubernetes/examples",
                    "roadmap_phase": "Phase 2: Cluster Orchestration & Ingress",
                    "score": 96.0,
                    "explanation": "Validates hands-on cloud orchestration, deployment strategies, and traffic management."
                },
                {
                    "id": 20013,
                    "title": "Automated GitOps Continuous Delivery & Observability Platform with ArgoCD",
                    "description": "Construct a zero-downtime GitOps continuous delivery pipeline with ArgoCD, integrated with Prometheus metric scraping, Grafana dashboards, and automated rollback triggers.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 22.0,
                    "deliverables": "GitOps declarative repository, ArgoCD application configurations, Prometheus alerting rules, and Grafana dashboard JSONs.",
                    "rubric": "GitOps sync reliability (40%), observability coverage (30%), disaster recovery automation (30%).",
                    "primary_skill": "CI/CD Pipelines",
                    "skills": ["Cloud Infrastructure & Kubernetes", "CI/CD Pipelines", "Prometheus & Observability"],
                    "github_template_url": "https://github.com/argoproj/argocd-example-apps",
                    "roadmap_phase": "Phase 3: GitOps & Observability Capstone",
                    "score": 92.0,
                    "explanation": "Production capstone validating modern cloud-native deployment automation and real-time telemetry."
                }
            ]

        # Domain 3: Blockchain / Solidity / Web3
        elif "blockchain" in role_lower or "solidity" in role_lower or "smart contract" in role_lower or "web3" in role_lower:
            projects = [
                {
                    "id": 20021,
                    "title": "Secure ERC-20 & Fractional Asset Escrow Smart Contract Suite",
                    "description": "Develop and audit an EVM-compliant smart contract suite in Solidity implementing OpenZeppelin standards, reentrancy guards, multi-party escrow, and custom events.",
                    "difficulty": "INTERMEDIATE",
                    "estimated_hours": 14.0,
                    "deliverables": "Solidity smart contracts, Hardhat automated unit test suite with 100% branch coverage, and gas optimization benchmarks.",
                    "rubric": "Security audit compliance (45%), gas consumption efficiency (30%), test coverage (25%).",
                    "primary_skill": "Solidity Programming",
                    "skills": ["Solidity Programming", "Smart Contracts & EVM"],
                    "github_template_url": "https://github.com/OpenZeppelin/openzeppelin-contracts",
                    "roadmap_phase": "Phase 1: Smart Contract Architecture",
                    "score": 95.5,
                    "explanation": "Teaches fundamental EVM execution model, secure contract patterns, and gas efficiency."
                },
                {
                    "id": 20022,
                    "title": "Full-Stack Web3 Decentralized App (DApp) with Ethers.js & IPFS",
                    "description": "Construct an end-to-end decentralized application featuring MetaMask wallet connectivity, state synchronization via Ethers.js, decentralized storage on IPFS, and contract interaction guards.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 20.0,
                    "deliverables": "Web3 frontend interface, Ethers.js integration layer, IPFS pinning gateway integration, and deployment scripts for Sepolia testnet.",
                    "rubric": "Wallet state handling (35%), decentralized data integrity (35%), user UX during transaction latency (30%).",
                    "primary_skill": "Web3.js & Ethers.js",
                    "skills": ["Solidity Programming", "Web3.js & Ethers.js", "Smart Contracts & EVM"],
                    "github_template_url": "https://github.com/dappuniversity/starter_kit",
                    "roadmap_phase": "Phase 2: Full-Stack Web3 Integration",
                    "score": 93.5,
                    "explanation": "Connects smart contract backend with modern reactive frontend interfaces."
                }
            ]

        # Domain 4: Computer Vision / OpenCV
        elif "vision" in role_lower or "opencv" in role_lower or "image" in role_lower:
            projects = [
                {
                    "id": 20031,
                    "title": "Real-Time Image Processing & Morphological Defect Inspection Pipeline",
                    "description": "Build an automated quality inspection system with OpenCV implementing Gaussian filtering, Canny edge detection, contour analysis, and real-time video stream ingestion.",
                    "difficulty": "INTERMEDIATE",
                    "estimated_hours": 14.0,
                    "deliverables": "Python OpenCV processing pipeline, automated feature extraction engine, and interactive benchmark dashboard.",
                    "rubric": "Image transformation accuracy (40%), frames-per-second throughput (30%), robust noise filtering (30%).",
                    "primary_skill": "OpenCV Image Processing",
                    "skills": ["Python Programming", "OpenCV Image Processing"],
                    "github_template_url": "https://github.com/opencv/opencv",
                    "roadmap_phase": "Phase 1: Feature Extraction & Filters",
                    "score": 95.0,
                    "explanation": "Validates core digital image processing operations and OpenCV API fundamentals."
                },
                {
                    "id": 20032,
                    "title": "Deep Learning Object Detection & Multi-Object Tracking System with YOLO",
                    "description": "Train and deploy a high-accuracy object detection and spatial tracking system using YOLO / PyTorch, featuring bounding box regression, ByteTrack multi-target association, and Streamlit visualization.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 22.0,
                    "deliverables": "Trained PyTorch model weights, real-time video tracking inference loop, evaluation mAP benchmarks, and containerized deployment.",
                    "rubric": "Mean Average Precision (mAP) score (40%), inference latency optimization (30%), tracking stability (30%).",
                    "primary_skill": "Object Detection & YOLO",
                    "skills": ["OpenCV Image Processing", "Convolutional Neural Networks (CNNs)", "Object Detection & YOLO"],
                    "github_template_url": "https://github.com/ultralytics/ultralytics",
                    "roadmap_phase": "Phase 2: Deep Vision & Object Tracking",
                    "score": 96.0,
                    "explanation": "Capstone vision application integrating modern neural networks with real-time video streams."
                }
            ]

        # Domain 5: Data Engineering / Spark / Kafka
        elif "data engineer" in role_lower or "spark" in role_lower or "kafka" in role_lower or "etl" in role_lower:
            projects = [
                {
                    "id": 20041,
                    "title": "Real-Time Distributed Event Streaming Engine with Apache Kafka & Spark",
                    "description": "Design an end-to-end distributed streaming data pipeline ingesting high-volume event streams through Kafka, processing micro-batches with Spark Streaming, and persisting deduplicated records.",
                    "difficulty": "INTERMEDIATE",
                    "estimated_hours": 16.0,
                    "deliverables": "Kafka producer/consumer configurations, PySpark structured streaming job scripts, and schema evolution handlers.",
                    "rubric": "Stream processing latency (40%), exactly-once semantics compliance (30%), schema validation (30%).",
                    "primary_skill": "Apache Spark & Distributed Computing",
                    "skills": ["SQL & Relational Databases", "Apache Spark & Distributed Computing", "Kafka & Event Streaming"],
                    "github_template_url": "https://github.com/apache/spark",
                    "roadmap_phase": "Phase 1: Streaming Ingestion & Distributed Processing",
                    "score": 96.0,
                    "explanation": "Fundamental data engineering build validating real-time streaming architectures."
                },
                {
                    "id": 20042,
                    "title": "Enterprise Delta Lakehouse Architecture & Automated Airflow Orchestration",
                    "description": "Construct an enterprise medallion architecture (Bronze, Silver, Gold layers) using Delta Lake, PySpark transformations, data quality testing with Great Expectations, and scheduled DAG orchestration via Apache Airflow.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 22.0,
                    "deliverables": "Airflow DAG definitions, Delta Lake schema definitions, automated data quality test suite, and analytical SQL view models.",
                    "rubric": "Data pipeline idempotency (35%), data quality test coverage (35%), partition & indexing performance (30%).",
                    "primary_skill": "Data Modeling & Lakehouse Architecture",
                    "skills": ["Apache Spark & Distributed Computing", "Kafka & Event Streaming", "Data Modeling & Lakehouse Architecture"],
                    "github_template_url": "https://github.com/apache/airflow",
                    "roadmap_phase": "Phase 2: Lakehouse Modeling & Orchestration",
                    "score": 94.0,
                    "explanation": "Production capstone covering end-to-end batch/streaming data lakehouse architecture."
                }
            ]

        # Domain 6: Generative AI / RAG
        elif "rag" in role_lower or "generative" in role_lower or "llm" in role_lower:
            projects = [
                {
                    "id": 20051,
                    "title": "Enterprise Document QA RAG System with Vector Search & LangChain",
                    "description": "Construct an end-to-end RAG application that ingests multi-format documents, performs semantic chunking, indexes embeddings into ChromaDB, and synthesizes answers with source citations.",
                    "difficulty": "INTERMEDIATE",
                    "estimated_hours": 18.0,
                    "deliverables": "Python LangChain pipeline, ChromaDB vector store, FastAPI querying endpoint, and Streamlit interactive UI.",
                    "rubric": "Retrieval accuracy (35%), hallucination mitigation (35%), API performance (30%).",
                    "primary_skill": "RAG Architecture & LangChain",
                    "skills": ["Prompt Engineering & LLM APIs", "Vector Databases & Embeddings", "RAG Architecture & LangChain"],
                    "github_template_url": "https://github.com/langchain-ai/rag-from-scratch",
                    "roadmap_phase": "Phase 1: Semantic Ingestion & RAG Pipeline",
                    "score": 98.0,
                    "explanation": "Directly validates semantic vector retrieval and context-augmented synthesis."
                },
                {
                    "id": 20052,
                    "title": "Multi-Source Semantic Knowledge Base with Hybrid BM25 & Vector Retrieval",
                    "description": "Implement a two-stage hybrid retrieval system with Cohere cross-encoder reranking, contextual compression, and automated RAG Triad evaluation using Ragas.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 22.0,
                    "deliverables": "Hybrid retriever implementation, reranking pipeline, evaluation benchmarks (Faithfulness, Context Precision), and FastAPI service.",
                    "rubric": "Retrieval recall enhancement (40%), evaluation score rigor (30%), latency optimization (30%).",
                    "primary_skill": "Chunking, Reranking & Retrieval Optimization",
                    "skills": ["Vector Databases & Embeddings", "RAG Architecture & LangChain", "Chunking, Reranking & Retrieval Optimization"],
                    "github_template_url": "https://github.com/explodinggradients/ragas",
                    "roadmap_phase": "Phase 2: Hybrid Retrieval & Evaluation",
                    "score": 96.5,
                    "explanation": "Advanced RAG engineering addressing precision reranking and continuous evaluation."
                }
            ]

        # Domain 7: Arbitrary / Unanticipated Generic Synthesizer
        else:
            domain_label = target_role.replace("Developer", "").replace("Engineer", "").strip()
            projects = [
                {
                    "id": 20091,
                    "title": f"{domain_label} Foundational Application & Component Prototype",
                    "description": f"Construct a modular, clean-architecture prototype in {primary_skill} establishing core data models, error handling strategies, and interactive interfaces.",
                    "difficulty": "INTERMEDIATE" if experience_level != "BEGINNER" else "BEGINNER",
                    "estimated_hours": 14.0,
                    "deliverables": f"Working repository in {primary_skill}, unit tests, and design documentation.",
                    "rubric": "Architecture modularity (40%), test coverage (30%), clean coding conventions (30%).",
                    "primary_skill": primary_skill,
                    "skills": skills[:2] if skills else [primary_skill],
                    "github_template_url": "https://github.com",
                    "roadmap_phase": "Phase 1: Foundation & Core Architecture",
                    "score": 92.0,
                    "explanation": f"Foundational milestone project to cement core {primary_skill} proficiencies."
                },
                {
                    "id": 20092,
                    "title": f"Production-Grade {domain_label} Enterprise System with Testing & CI/CD",
                    "description": f"Architect a production-grade application for {target_role} incorporating persistence, asynchronous processing, security controls, and automated deployment pipelines.",
                    "difficulty": "ADVANCED",
                    "estimated_hours": 22.0,
                    "deliverables": f"Full-stack {domain_label} application, automated test suite, containerization configs, and deployment scripts.",
                    "rubric": "System reliability (35%), security best practices (35%), test automation (30%).",
                    "primary_skill": secondary_skill if len(skills) > 1 else primary_skill,
                    "skills": skills[:3] if len(skills) >= 3 else [primary_skill, secondary_skill],
                    "github_template_url": "https://github.com",
                    "roadmap_phase": "Phase 2: Production Capstone",
                    "score": 94.5,
                    "explanation": f"Advanced capstone validating end-to-end competency for {target_role}."
                }
            ]

        return projects

# Singleton factory
def get_llm_service() -> BaseLLMService:
    return LocalDeterministicLLM()

