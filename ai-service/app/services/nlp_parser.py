import re
from typing import List, Dict, Any
from app.models.schemas import GoalAnalysisResponse

class NLPGoalParser:
    def __init__(self):
        pass

    def parse_goal(self, prompt: str) -> GoalAnalysisResponse:
        p_lower = prompt.lower()
        
        # 1. Target Role & Category Extraction
        if any(w in p_lower for w in ["rag", "retrieval augmented", "llm", "generative ai", "genai", "langchain", "vector db", "embedding", "gpt"]):
            target_role = "Generative AI & RAG Engineer"
            extracted_skills = ["Python Programming", "Foundations of AI & ML"]
            missing_skills = ["Prompt Engineering & LLM APIs", "Vector Databases & Embeddings", "RAG Architecture & LangChain", "NumPy & Pandas", "Deep Learning & PyTorch", "Model Deployment & FastAPI", "LLM Fine-Tuning & Evaluation"]
            estimated_months = 6
            learning_pace = "10 hours/week (Hands-On)"
            ai_summary = "Specialized engineering roadmap covering Retrieval-Augmented Generation (RAG), semantic vector embeddings, chunking strategies, LangChain/LlamaIndex pipelines, and production LLM serving."
        elif any(w in p_lower for w in ["flutter", "dart", "mobile app", "mobile development", "android", "ios"]):
            target_role = "Flutter Mobile Developer"
            extracted_skills = ["Dart Programming", "Mobile Navigation & Routing"]
            missing_skills = ["Flutter Framework & Widgets", "State Management (Riverpod/Bloc)", "REST API Integration & Local Storage", "Cross-Platform App Deployment"]
            estimated_months = 6
            learning_pace = "10 hours/week (Hands-On)"
            ai_summary = "Comprehensive mobile app development path focusing on Dart programming, Flutter reactive widgets, state management architectures, and cross-platform mobile deployment."
        elif any(w in p_lower for w in ["blockchain", "solidity", "web3", "crypto", "smart contract", "ethereum"]):
            target_role = "Blockchain & Smart Contract Engineer"
            extracted_skills = ["Solidity Programming", "Smart Contracts & EVM"]
            missing_skills = ["Web3.js & Ethers.js", "DeFi & Token Standards", "Security Auditing & Hardhat"]
            estimated_months = 6
            learning_pace = "12 hours/week (Intensive)"
            ai_summary = "Decentralized application engineering covering Solidity smart contracts, EVM mechanics, Web3 frontend integration, and cryptographic security auditing."
        elif any(w in p_lower for w in ["computer vision", "vision", "opencv", "image processing", "yolo"]):
            target_role = "Computer Vision Engineer"
            extracted_skills = ["Python Programming", "OpenCV Image Processing"]
            missing_skills = ["Convolutional Neural Networks (CNNs)", "Object Detection & YOLO", "PyTorch Vision Models"]
            estimated_months = 6
            learning_pace = "10 hours/week (Hands-On)"
            ai_summary = "Specialized vision engineering curriculum covering image transformations, OpenCV filtering, deep convolutional neural networks, and real-time object detection."
        elif any(w in p_lower for w in ["data engineering", "etl", "spark", "kafka", "data warehouse", "data pipeline"]):
            target_role = "Data Engineer"
            extracted_skills = ["Python Programming", "SQL & Relational Databases"]
            missing_skills = ["Apache Spark & Distributed Computing", "Kafka & Event Streaming", "Data Warehousing & ETL Pipelines", "Airflow Orchestration"]
            estimated_months = 6
            learning_pace = "10 hours/week (Structured)"
            ai_summary = "Scalable data systems engineering covering distributed compute with Apache Spark, real-time event streaming with Kafka, and automated data pipelines."
        elif any(w in p_lower for w in ["cybersecurity", "security", "ethical hacking", "penetration testing", "infosec"]):
            target_role = "Cybersecurity Engineer"
            extracted_skills = ["Networking Fundamentals & TCP/IP", "Linux Systems & Shell Scripting"]
            missing_skills = ["Web Application Security (OWASP Top 10)", "Cryptography Fundamentals", "Penetration Testing & Network Scanning"]
            estimated_months = 6
            learning_pace = "10 hours/week (Hands-On)"
            ai_summary = "Information security curriculum covering network protocols, defensive architecture, OWASP web application security, and penetration testing."
        elif any(w in p_lower for w in ["backend", "java", "spring", "microservice"]):
            target_role = "Backend Java Developer"
            extracted_skills = ["Java", "SQL & Relational Databases", "Object-Oriented Programming (OOP)"]
            missing_skills = ["Spring Boot", "RESTful APIs", "Spring Security & JWT", "Spring Data JPA & Hibernate", "Docker & Containers", "System Design & Microservices"]
            estimated_months = 6
            learning_pace = "10 hours/week (Structured)"
            ai_summary = "Focused on modern enterprise Java backend engineering with Spring Boot microservices ecosystem, transactional databases, and distributed architecture."
        elif any(w in p_lower for w in ["fullstack", "full-stack", "react", "frontend", "web", "javascript", "typescript", "node"]):
            target_role = "Full Stack Developer"
            extracted_skills = ["JavaScript (ES6+)", "HTML5 & CSS3", "Git & Version Control"]
            missing_skills = ["React.js", "Node.js & Express", "SQL & Relational Databases", "RESTful APIs", "Docker & Containers"]
            estimated_months = 6
            learning_pace = "12 hours/week (Intensive)"
            ai_summary = "End-to-end full-stack web application development combining React frontend interfaces with Node/Express/PostgreSQL backend services."
        elif any(w in p_lower for w in ["devops", "cloud", "kubernetes", "docker", "aws", "ci/cd", "k8s"]):
            target_role = "DevOps & Cloud Engineer"
            extracted_skills = ["Linux & Shell Scripting", "Git & Version Control", "Networking Fundamentals"]
            missing_skills = ["Docker & Containers", "Kubernetes Orchestration", "CI/CD Pipelines", "Terraform & IaC", "AWS Cloud Infrastructure"]
            estimated_months = 6
            learning_pace = "10 hours/week (Hands-On)"
            ai_summary = "Modern cloud infrastructure, container orchestration, automated continuous deployment pipelines, and Infrastructure as Code."
        elif any(w in p_lower for w in ["ai", "machine learning", "data science", "neural", "deep learning", "nlp"]) and not ("artificial intelligence only" in p_lower):
            target_role = "AI / ML Engineer"
            extracted_skills = ["Python Programming", "Mathematics & Statistics for ML", "NumPy & Pandas"]
            missing_skills = ["Scikit-Learn", "Deep Learning & PyTorch", "Vector Databases & Embeddings", "Model Deployment & FastAPI", "SQL & Relational Databases"]
            estimated_months = 8
            learning_pace = "10 hours/week (Standard)"
            ai_summary = "Comprehensive path spanning mathematical ML fundamentals, classical predictive algorithms, deep neural networks, and model deployment."
        else:
            target_role = "General Software Specialist"
            extracted_skills = ["Programming Fundamentals", "Data Structures & Algorithms", "Git & Version Control"]
            missing_skills = ["RESTful APIs", "System Design & Architecture"]
            estimated_months = 6
            learning_pace = "8 hours/week (Self-Paced)"
            ai_summary = "Engineering roadmap focused on foundational software architecture and clean design principles."

        # 2. Experience Level Detection
        # First check advanced signals
        if any(w in p_lower for w in ["senior", "expert", "advanced", "lead", "architect"]):
            exp_level = "ADVANCED"
        # Beginner signals: explicit keywords OR natural language "know X only", "just know", limited knowledge phrases
        elif (
            any(w in p_lower for w in [
                "beginner", "starting out", "no experience", "zero experience",
                "novice", "only know", "just know", "basics only", "from scratch",
                "i am new", "i'm new", "completely new", "don't know much",
                "don't know anything", "just starting", "learning from zero"
            ]) or
            re.search(r'\bi know\b.{0,40}\bonly\b', p_lower) or
            re.search(r'\bonly know\b.{0,30}(basics|fundamentals|concept|definition|what it is)', p_lower) or
            re.search(r'\bno\s+(prior|previous|background)\s+experience\b', p_lower)
        ):
            exp_level = "BEGINNER"
        else:
            exp_level = "INTERMEDIATE"

        # 3. Timeline Extraction (e.g. "6 months", "3 months", "1 year")
        months_match = re.search(r'(\d+)\s*(?:month|mo)', p_lower)
        if months_match:
            try:
                estimated_months = int(months_match.group(1))
            except:
                pass

        return GoalAnalysisResponse(
            target_role=target_role,
            career_goal=prompt,
            experience_level=exp_level,
            estimated_months=estimated_months,
            extracted_skills=extracted_skills,
            missing_skills=missing_skills,
            learning_pace=learning_pace,
            ai_summary=ai_summary
        )
