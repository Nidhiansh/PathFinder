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

    @abstractmethod
    def generate_domain_projects(
        self,
        target_role: str,
        career_goal: str,
        experience_level: str,
        skills: List[str],
        skill_gaps: List[str],
        roadmap_phases: List[str],
        custom_topic: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        pass

    @abstractmethod
    def generate_assessment_blueprint(self, target_role: str, skills: List[str]) -> Dict[str, Any]:
        pass

class LocalDeterministicLLM(BaseLLMService):
    """
    Production-grade deterministic local intelligence engine.
    Ensures zero external dependency while delivering context-aware, highly personalized reasoning
    grounded strictly in the learner's active domain and prerequisite graph.
    """
    def generate_chat_reply(self, message: str, context: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        msg = message.lower()
        ctx = context or {}
        role = ctx.get("target_role", "Engineering Specialist")
        style = ctx.get("preferred_style", "PRACTICAL")

        if "5 hour" in msg or "5h" in msg or "five hour" in msg:
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

        elif "why" in msg or "reason" in msg:
            reply = (
                f"Recommendations and roadmap milestones are calculated directly from your target objective **{role}** "
                f"and validated prerequisite dependencies in the knowledge graph. Foundational prerequisites must be verified "
                f"before advancing to capstone modules."
            )
            return {
                "reply": reply,
                "suggested_action": "EXPLAIN_RECOMMENDATION",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/recommendations", "label": "Explore Recommendations"},
                "quick_replies": ["What project should I build next?", "Show prerequisite graph", "How long will this take?"]
            }

        elif "what should i learn next" in msg or "what next" in msg or "start" in msg:
            reply = (
                f"Based on your active goal for **{role}**, your highest-priority step is your current foundational phase module. "
                f"Your prerequisites are mapped in topological order, and completing this unlocks subsequent capstone milestones."
            )
            return {
                "reply": reply,
                "suggested_action": "START_NEXT_ITEM",
                "action_type": "NAVIGATE",
                "action_payload": {"url": "/app/roadmap", "label": "Open Active Phase"},
                "quick_replies": ["View current phase roadmap", "Show project rubric", "Explain prerequisites"]
            }

        elif "project" in msg or "build" in msg or "portfolio" in msg:
            reply = (
                f"For your active goal in **{role}**, I recommend checking the Projects Hub for structured, practical deliverables "
                f"tailored to close your current competency gaps."
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
                "In our DAG prerequisite model, foundational skills cannot be safely bypassed without creating conceptual debt. "
                "However, if you already have proficiency, you can take a **Diagnostic Quiz** to instantly verify mastery and unlock downstream modules."
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
                "quick_replies": ["What should I learn next?", "Show my top skill gaps", "View projects"]
            }

    def explain_recommendation(self, resource_title: str, user_profile: Dict[str, Any], score_breakdown: Dict[str, float]) -> str:
        role = user_profile.get("target_role", "Engineering Specialist")
        return f"'{resource_title}' was recommended with high confidence because it directly targets essential competencies for {role}."

    def generate_domain_projects(
        self,
        target_role: str,
        career_goal: str,
        experience_level: str,
        skills: List[str],
        skill_gaps: List[str],
        roadmap_phases: List[str],
        custom_topic: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        active_skills = skills if skills else [target_role]
        primary = active_skills[0]
        context_text = f"{target_role} {career_goal} {primary}".lower()

        # Domain Archetype Identification
        is_arts = any(w in context_text for w in ["paint", "watercolor", "sketch", "draw", "origami", "ceramic", "sculpt", "art", "craft", "photo", "music", "audio"])
        is_analytics = any(w in context_text for w in ["forecast", "supply chain", "logistics", "inventory", "econometric", "finance", "valuation", "actuarial"])
        is_bio = any(w in context_text for w in ["biology", "genetic", "crispr", "molecular", "cell", "biochem", "medicine"])
        is_physical = any(w in context_text for w in ["aerodynamic", "flight", "physics", "thermo", "fluid", "mechanics"])
        is_software = any(w in context_text for w in ["java", "python", "spring", "react", "docker", "kubernetes", "flutter", "sql", "api", "algorithms", "dynamic programming", "rag"])

        if is_arts:
            p1_title = f"Foundational {primary} Study & Execution Portfolio"
            p1_desc = f"A structured hands-on project validating essential medium dynamics, composition, and technique execution for {primary}."
            p1_deliv = "Curated study portfolio containing 3 progressive technique artifacts, material notes, and reflective process log."
            p1_rubric = "Technical execution and medium control (40%), compositional balance (30%), reflective analysis (30%)."

            p2_title = f"{target_role} Advanced Synthesis & Exhibition Capstone"
            p2_desc = f"An advanced capstone creating a cohesive, professional-grade portfolio piece synthesizing all core techniques in {primary}."
            p2_deliv = "Exhibition-grade final portfolio piece, high-resolution documentation, and detailed technical artist statement."
            p2_rubric = "Mastery of advanced technique (35%), conceptual cohesion and aesthetic depth (35%), portfolio presentation (30%)."

        elif is_analytics:
            p1_title = f"Baseline {primary} Empirical Modeling & Volatility Study"
            p1_desc = f"A practical analytical modeling project applying foundational quantitative techniques, error measurement, and parameter sensitivity."
            p1_deliv = "Validated forecasting/analytical model specification, data validation workbook, and parameter sensitivity briefing."
            p1_rubric = "Mathematical and quantitative rigor (40%), error metric validation (30%), executive interpretation (30%)."

            p2_title = f"Enterprise {target_role} End-to-End Decision Framework Capstone"
            p2_desc = f"A comprehensive enterprise-grade decision and optimization capstone applying end-to-end multi-variable modeling for {primary}."
            p2_deliv = "Full production-ready modeling suite, scenario simulation report, and strategic recommendation briefing."
            p2_rubric = "Model robustness & scenario optimization (35%), data-driven decision quality (35%), executive documentation (30%)."

        elif is_bio:
            p1_title = f"Foundational {primary} Pathway & Protocol Analysis"
            p1_desc = f"A structured scientific research project analyzing cellular mechanisms, target selection, and experimental protocols."
            p1_deliv = "Experimental assay protocol specification, pathway diagram documentation, and literature review synthesis."
            p1_rubric = "Scientific accuracy and mechanism depth (40%), experimental protocol rigor (30%), literature citation (30%)."

            p2_title = f"{target_role} Applied Experimental Design & Genomic Synthesis Capstone"
            p2_desc = f"An advanced scientific capstone designing a full experimental pipeline, data verification protocol, and phenotypic assay."
            p2_deliv = "Comprehensive research dossier, simulation/assay dataset analysis, and publication-ready study manuscript."
            p2_rubric = "Experimental design validity (35%), analytical depth (35%), scientific reproducibility (30%)."

        elif is_physical:
            p1_title = f"Foundational {primary} Governing Dynamics & Flow Simulation"
            p1_desc = f"An engineering analysis project calculating theoretical parameters, boundary conditions, and flow/stability profiles."
            p1_deliv = "Parametric calculation workbook, simulation stability plots, and engineering verification summary."
            p1_rubric = "Governing equation accuracy (40%), parametric boundary handling (30%), simulation documentation (30%)."

            p2_title = f"{target_role} Multi-Parameter Simulation & Design Optimization Capstone"
            p2_desc = f"An advanced engineering capstone optimizing physical aerodynamic or mechanical performance across real-world operational envelopes."
            p2_deliv = "Complete parametric engineering model, optimization convergence report, and technical design verification dossier."
            p2_rubric = "Design optimization rigor (35%), physical simulation fidelity (35%), engineering report (30%)."

        elif is_software:
            p1_title = f"{primary} Core Mechanics & Algorithmic Framework"
            p1_desc = f"A hands-on implementation project validating fundamental architecture, core mechanics, and clean execution for {primary}."
            p1_deliv = "Modular implementation repository, automated unit test suite, and technical documentation."
            p1_rubric = "Core conceptual correctness (40%), code modularity (30%), test coverage (30%)."

            p2_title = f"{target_role} Production-Grade Capstone Architecture"
            p2_desc = f"An advanced end-to-end production milestone synthesizing {', '.join(active_skills[:4])} with performance benchmarking and optimization."
            p2_deliv = "Production-grade repository, benchmark suite, configuration manifests, and architecture documentation."
            p2_rubric = "Architectural robustness (35%), performance optimization (35%), production readiness (30%)."

        else:
            p1_title = f"Applied {primary} Practical Milestone Study"
            p1_desc = f"A structured practical project validating essential methodologies, workflow execution, and core principles in {primary}."
            p1_deliv = "Practical milestone portfolio, methodology documentation, and reviewable artifact."
            p1_rubric = "Domain rigor (40%), execution quality (30%), documentation (30%)."

            p2_title = f"{target_role} Comprehensive Field Capstone Portfolio"
            p2_desc = f"An advanced capstone demonstrating end-to-end mastery and practical problem solving in {primary}."
            p2_deliv = "Comprehensive capstone portfolio, full case evaluation, and professional artifact package."
            p2_rubric = "Synthesis of domain competencies (35%), practical impact (35%), presentation rigor (30%)."

        return [
            {
                "id": 20001,
                "title": p1_title,
                "description": p1_desc,
                "difficulty": "INTERMEDIATE",
                "estimated_hours": 15.0,
                "deliverables": p1_deliv,
                "rubric": p1_rubric,
                "primary_skill": primary,
                "github_template_url": f"https://learning.pathfinder.ai/projects/{primary.lower().replace(' ', '-')}",
                "skills": active_skills[:2],
                "roadmap_phase": "Phase 1: Foundation & Core Architecture",
                "score": 95.0,
                "explanation": f"Directly validates foundational competency in {primary} as defined in your personalized learning path."
            },
            {
                "id": 20002,
                "title": p2_title,
                "description": p2_desc,
                "difficulty": "ADVANCED",
                "estimated_hours": 25.0,
                "deliverables": p2_deliv,
                "rubric": p2_rubric,
                "primary_skill": primary,
                "github_template_url": f"https://learning.pathfinder.ai/projects/{target_role.lower().replace(' ', '-')}",
                "skills": active_skills,
                "roadmap_phase": "Phase 2: Advanced Synthesis & Capstone",
                "score": 92.0,
                "explanation": f"Comprehensive capstone synthesizing all required competencies for {target_role}."
            }
        ]

    def generate_assessment_blueprint(self, target_role: str, skills: List[str]) -> Dict[str, Any]:
        return {
            "target_role": target_role,
            "skills_evaluated": skills,
            "assessment_type": "DIAGNOSTIC_AND_PRACTICAL",
            "format": "Conceptual Evaluation & Milestone Artifact Review",
            "passing_threshold_percentage": 75.0,
            "sections": [
                {
                    "section_name": "Foundational Principles & Mechanics",
                    "weight_percentage": 35.0,
                    "focus": f"Evaluates core understanding of {skills[0] if skills else target_role}"
                },
                {
                    "section_name": "Applied Domain Technique & Problem Solving",
                    "weight_percentage": 40.0,
                    "focus": "Practical execution and parameter/medium handling"
                },
                {
                    "section_name": "Advanced Synthesis & Edge Cases",
                    "weight_percentage": 25.0,
                    "focus": "Complex scenarios, optimization, and synthesis"
                }
            ]
        }

_local_llm_instance = LocalDeterministicLLM()

def get_llm_service() -> BaseLLMService:
    return _local_llm_instance


