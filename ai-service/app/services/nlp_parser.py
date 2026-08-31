from typing import List, Dict, Any
from app.models.schemas import GoalAnalysisResponse
from app.services.semantic_knowledge_service import SemanticKnowledgeService

class NLPGoalParser:
    def __init__(self):
        self.knowledge_service = SemanticKnowledgeService()

    def parse_goal(self, prompt: str) -> GoalAnalysisResponse:
        """
        Parses natural language prompt using the universal knowledge-grounded ontology.
        Returns validated role, core skills, prerequisite skills, and summary.
        """
        result = self.knowledge_service.resolve_goal_knowledge(prompt)

        return GoalAnalysisResponse(
            target_role=result["target_role"],
            career_goal=result["raw_goal"],
            experience_level=result["experience_level"],
            estimated_months=result["estimated_months"],
            extracted_skills=result["extracted_skills"],
            missing_skills=result["missing_skills"],
            learning_pace=result["learning_pace"],
            ai_summary=result["ai_summary"]
        )
