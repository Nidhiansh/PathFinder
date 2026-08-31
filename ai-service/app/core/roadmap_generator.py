import math
from typing import List, Dict, Any, Optional
from app.models.schemas import (
    LearnerProfileInput, RoadmapGenerationResponse,
    RoadmapPhaseResponse, RoadmapItemResponse, ResourceInput
)
from app.core.skill_graph import SkillGraph
from app.core.scorer import RecommendationScorer

class RoadmapGenerator:
    def __init__(self, skill_graph: SkillGraph, scorer: RecommendationScorer):
        self.skill_graph = skill_graph
        self.scorer = scorer

    def generate_roadmap(
            self,
            profile: LearnerProfileInput,
            candidate_resources: Optional[List[ResourceInput]] = None
    ) -> RoadmapGenerationResponse:
        target_role = profile.target_role or "Backend Java Developer"
        role_skills_map = self.skill_graph.get_role_skills(target_role)

        # Build learner current skills dictionary (lowercased)
        current_skills_map = {
            s.skill_name.lower(): s.proficiency_level for s in profile.skills
        }

        # Determine all required skills and topologically sort them
        target_skill_names = list(role_skills_map.keys())
        ordered_skills = self.skill_graph.topological_sort(target_skill_names)

        # Group ordered skills into structured learning phases
        phases: List[RoadmapPhaseResponse] = []
        phase_groups = self._group_skills_into_phases(ordered_skills, target_role)

        total_hours = 0.0
        is_previous_phase_cleared = True

        for idx, group in enumerate(phase_groups, start=1):
            phase_title = group["title"]
            phase_desc = group["description"]
            phase_skills = group["skills"]

            # Evaluate phase status based on learner current proficiency
            skills_mastered = all(
                current_skills_map.get(s.lower(), 0) >= role_skills_map.get(s, 70)
                for s in phase_skills
            )
            
            if skills_mastered:
                phase_status = "COMPLETED"
            elif is_previous_phase_cleared:
                phase_status = "AVAILABLE"
                is_previous_phase_cleared = False
            else:
                phase_status = "LOCKED"

            phase_items: List[RoadmapItemResponse] = []
            phase_hours = 0.0
            order_idx = 1

            for s in phase_skills:
                # 1. Main Resource
                res_url = self._get_resource_url(s)
                res_hours = self._get_estimated_hours(s)
                phase_hours += res_hours

                s_prof = current_skills_map.get(s.lower(), 0)
                is_item_completed = s_prof >= role_skills_map.get(s, 70)
                item_status = "COMPLETED" if is_item_completed else ("AVAILABLE" if phase_status == "AVAILABLE" and order_idx == 1 else ("LOCKED" if phase_status == "LOCKED" else "AVAILABLE"))

                phase_items.append(RoadmapItemResponse(
                    id=order_idx,
                    item_type="RESOURCE",
                    title=f"Master {s}: Core Architecture & Practical Patterns",
                    url=res_url,
                    estimated_hours=res_hours,
                    order_index=order_idx,
                    status=item_status,
                    recommendation_score=94.0 if s_prof < 50 else 82.0,
                    recommendation_reason=f"Target skill for {target_role} addressing your current mastery level ({s_prof}%).",
                    required_prerequisites=[p[0] for p in self.skill_graph.get_prerequisites_for_skill(s)],
                    is_locked=(item_status == "LOCKED")
                ))
                order_idx += 1

            # 2. Phase Milestone Project
            proj_title = self._get_project_title(phase_skills[0] if phase_skills else "Software")
            proj_hours = 12.0
            phase_hours += proj_hours
            phase_items.append(RoadmapItemResponse(
                id=order_idx,
                item_type="PROJECT",
                title=f"Milestone Project: {proj_title}",
                url="https://github.com",
                estimated_hours=proj_hours,
                order_index=order_idx,
                status="AVAILABLE" if phase_status == "AVAILABLE" else "LOCKED",
                recommendation_score=96.0,
                recommendation_reason=f"Portfolio capstone to demonstrate verifiable competency in {', '.join(phase_skills[:2])}.",
                required_prerequisites=phase_skills[:1],
                is_locked=(phase_status == "LOCKED")
            ))
            order_idx += 1

            # 3. Assessment Checkpoint
            phase_items.append(RoadmapItemResponse(
                id=order_idx,
                item_type="ASSESSMENT",
                title=f"Checkpoint Assessment: {phase_skills[0] if phase_skills else 'Phase'} Verification Quiz",
                url=f"/assessments/{idx}",
                estimated_hours=0.5,
                order_index=order_idx,
                status="AVAILABLE" if phase_status == "AVAILABLE" else "LOCKED",
                recommendation_score=98.0,
                recommendation_reason="Adaptive checkpoint: scoring >90% enables skipping prerequisites for downstream modules.",
                required_prerequisites=phase_skills,
                is_locked=(phase_status == "LOCKED")
            ))

            total_hours += phase_hours
            phases.append(RoadmapPhaseResponse(
                phase_number=idx,
                title=f"Phase {idx}: {phase_title}",
                description=phase_desc,
                status=phase_status,
                estimated_hours=round(phase_hours, 1),
                items=phase_items
            ))

        weekly_hours = max(1, profile.weekly_hours or 10)
        estimated_weeks = math.ceil(total_hours / weekly_hours)

        return RoadmapGenerationResponse(
            title=f"{target_role} Accelerated Career Roadmap",
            target_role=target_role,
            total_estimated_hours=round(total_hours, 1),
            estimated_weeks=estimated_weeks,
            phases=phases
        )

    def _group_skills_into_phases(self, ordered_skills: List[str], role: str) -> List[Dict[str, Any]]:
        groups = []
        if not ordered_skills:
            return groups

        total_skills = len(ordered_skills)
        num_phases = min(5, max(2, math.ceil(total_skills / 2.5)))
        chunk_size = math.ceil(total_skills / num_phases)

        for i in range(0, total_skills, chunk_size):
            phase_num = (i // chunk_size) + 1
            sub_skills = ordered_skills[i:i + chunk_size]
            
            if phase_num == 1:
                title = f"{sub_skills[0]} & Core Foundations"
                desc = f"Master foundational principles, fundamental workflows, and core concepts in {', '.join(sub_skills)}."
            elif i + chunk_size >= total_skills:
                title = f"{sub_skills[0]} & Advanced Production Capstone"
                desc = f"Architect end-to-end production systems, integration patterns, and optimization for {', '.join(sub_skills)}."
            else:
                title = f"{sub_skills[0]} & Applied Engineering"
                desc = f"Deep dive into practical implementation patterns, tooling, and ecosystem integration for {', '.join(sub_skills)}."

            groups.append({
                "title": title,
                "description": desc,
                "skills": sub_skills
            })

        return [g for g in groups if g["skills"]]

    def _get_resource_url(self, skill: str) -> str:
        s = skill.lower()
        if "rag" in s or "langchain" in s: return "https://python.langchain.com/docs/tutorials/rag/"
        if "vector" in s or "embedding" in s: return "https://github.com/pgvector/pgvector"
        if "prompt" in s: return "https://platform.openai.com/docs/guides/prompt-engineering"
        if "eval" in s: return "https://docs.ragas.io/"
        if "java" in s: return "https://dev.java/learn/"
        if "sql" in s or "database" in s: return "https://www.postgresql.org/docs/current/tutorial.html"
        if "spring" in s: return "https://spring.io/guides/gs/rest-service/"
        if "react" in s: return "https://react.dev/learn"
        if "python" in s: return "https://docs.python.org/3/tutorial/"
        if "docker" in s: return "https://docs.docker.com/get-started/"
        if "system design" in s: return "https://github.com/donnemartin/system-design-primer"
        return "https://developer.mozilla.org"

    def _get_estimated_hours(self, skill: str) -> float:
        s = skill.lower()
        if "rag" in s or "spring" in s or "deep learning" in s or "system design" in s: return 18.0
        if "vector" in s or "java" in s or "react" in s or "sql" in s or "scikit" in s: return 14.0
        return 10.0

    def _get_project_title(self, skill: str) -> str:
        s = skill.lower()
        if "rag" in s: return "Enterprise Document QA RAG System with ChromaDB & Citations"
        if "vector" in s or "embedding" in s: return "Semantic Knowledge Base with Hybrid Vector Retrieval"
        if "prompt" in s: return "Multi-Agent Workflow Engine with Structured Tool Calling"
        if "java" in s: return "Multithreaded Concurrent Task Pipeline in Java 21"
        if "sql" in s: return "High-Performance Normalized Database Architecture"
        if "spring" in s: return "Production-Ready E-Commerce REST API with PostgreSQL"
        if "react" in s: return "Interactive SaaS Analytics Platform with React 19"
        if "python" in s or "learn" in s: return "Predictive Machine Learning Pipeline with Scikit-Learn"
        if "docker" in s: return "Multi-Container Microservices Deployment with Docker Compose"
        if "system design" in s: return "Scalable Distributed URL Shortener Service"
        return f"Production Capstone Application for {skill}"
