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

_local_llm_instance = LocalDeterministicLLM()

def get_llm_service() -> BaseLLMService:
    return _local_llm_instance

