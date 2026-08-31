# PathFinder AI - Personalized Learning Path Recommender

> **Autonomous AI-powered learning path engine that analyzes career targets, existing skills, and study constraints to generate, explain, and adaptively recalibrate prerequisite-aware roadmaps.**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.115-blue.svg)](https://fastapi.tiangolo.com)
[![React](https://img.shields.io/badge/React-18.3-cyan.svg)](https://reactjs.org)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple.svg)](https://vitejs.dev)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-sky.svg)](https://tailwindcss.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com)

---

## Key Highlights

- **Topological DAG Prerequisite Engine**: Directed Acyclic Graph dependency sequencing using Kahn's algorithm. Ensures zero prerequisite violations.
- **Multi-Factor Recommendation Scorer**: Weighted 6-factor mathematical ranking formula factoring skill gaps, goals, style, difficulty, quality, and vector cosine similarity.
- **Explainable AI (XAI)**: Visualizes exact mathematical factor weights and pedagogical rationales for every recommendation.
- **Adaptive Feedback Loops**: Fast-tracks downstream modules on high quiz scores ($\ge 90\%$) and dynamically recalculates timelines when weekly study hours change.
- **Conversational Copilot**: Context-aware AI assistant capable of in-chat action execution and schedule modifications.
- **Production Architecture**: Decoupled React frontend + Spring Boot backend + FastAPI ML service + PostgreSQL/H2 database.

---

## System Architecture

```
                                 +-----------------------------------------+
                                 |         React 18 + Vite Frontend        |
                                 |   (Tailwind CSS, Lucide, Recharts)      |
                                 |        http://localhost:5173            |
                                 +--------------------+--------------------+
                                                      | REST (JWT Auth)
                                                      v
                                 +-----------------------------------------+
                                 |        Spring Boot 3.4 Backend          |
                                 |     (Java 25, Spring Security, JPA)     |
                                 |        http://localhost:8080            |
                                 +-----------+-----------------+-----------+
                                             |                 |
                      JPA / Hibernate (SQL)  |                 | HTTP REST / RPC
                                             v                 v
             +-----------------------------------+    +-----------------------------------+
             │       PostgreSQL 16 Database      │    │      FastAPI AI & ML Service      │
             │   (H2 In-Memory Fallback Active)  │    │  (Scikit-Learn, Pandas, NumPy)    │
             │        localhost:5432             │    │        http://localhost:8000      │
             +-----------------------------------+    +-----------------------------------+
```

---

## Quickstart Guide

### Option 1: Docker Compose (All Services)

```bash
# Clone and start containers
docker-compose up --build -d

# Open in browser:
# Frontend: http://localhost:5173
# Backend API: http://localhost:8080
# AI Service: http://localhost:8000
```

### Option 2: Local Development

#### 1. Start FastAPI AI Service (Port 8000)
```bash
cd ai-service
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

#### 2. Start Spring Boot Backend (Port 8080)
```bash
cd backend
mvn spring-boot:run
```

#### 3. Start React Frontend (Port 5173)
```bash
cd frontend
npm install
npm run dev
```

---

## 1-Click Interactive Demo Personas

| Username | Password | Persona | Target Role | Starting Strengths |
| :--- | :--- | :--- | :--- | :--- |
| `demo_java` | `password123` | **Alex Chen** | Backend Java Developer | Java (80%), OOP (85%), SQL (60%), DSA (65%) |
| `demo_fullstack` | `password123` | **Sarah Taylor** | Full Stack Developer | JavaScript (85%), React (80%), CSS (75%) |

---

## REST API Overview

### Backend APIs (`http://localhost:8080/api`)
- `POST /auth/register` & `POST /auth/login`: JWT Authentication
- `GET /dashboard`: Aggregated personalized progress telemetry
- `GET /skills/gaps`: Detailed target role competency gap analysis
- `GET /recommendations`: Scored learning resources & capstone projects
- `POST /recommendations/{id}/feedback`: Upvote / downvote feedback loop
- `GET /roadmap` & `POST /roadmap/generate`: Topological phased roadmap
- `POST /roadmap/recalculate-time`: Dynamic timeline scaling based on weekly hours
- `GET /assessments/{id}` & `POST /assessments/{id}/submit`: Diagnostic evaluation
- `POST /chat`: Conversational AI Copilot with action receipts

### AI Engine APIs (`http://localhost:8000/ai`)
- `POST /ai/analyze-goal`: Natural language career goal entity parser
- `POST /ai/analyze-skill-gap`: Multi-competency gap evaluator
- `POST /ai/recommend`: Multi-factor TF-IDF vector recommendation scorer
- `POST /ai/generate-roadmap`: Prerequisite-aware topological roadmap synthesizer
- `POST /ai/adapt-roadmap`: Assessment score & pacing recalibration engine
- `POST /ai/chat`: Deterministic AI reasoning and structured action generator

---

## Verification & Testing

To run the automated 12-scenario end-to-end verification suite:

```bash
python scripts/verify_e2e_scenarios.py
```

Expected output:
```
======================================================================
AI-POWERED PERSONALIZED LEARNING PATH RECOMMENDER - E2E TEST SUITE
======================================================================
[PASS] 1. Frontend Web App Serving on http://localhost:5173 (HTTP 200)
[PASS] 2. User Registration & JWT Authentication
[PASS] 3. Protected Session Verified (/api/auth/me)
[PASS] 4. AI NLP Goal Extraction
[PASS] 5. Profile & Skill Matrix Persisted
[PASS] 6. DAG Roadmap Generated with Prerequisite Locking
[PASS] 7. Multi-Factor Scorer with Explainable AI reasoning
[PASS] 8. Milestone Completion Triggered & Progress Recalculated
[PASS] 9. Diagnostic Assessment Checkpoint Evaluated
[PASS] 10. AI Copilot Adaptive Pacing (Weekly hours 10h -> 5h -> 45 weeks)
[PASS] 11. AI Copilot Prerequisite Dependency Dialogue Verified
[PASS] 12. Personalized Dashboard Telemetry Verified
======================================================================
ALL 12/12 END-TO-END DEMO SCENARIO CHECKS PASSED WITH 100% SUCCESS!
======================================================================
```

---

## License
MIT License © 2026 PathFinder AI Team.
