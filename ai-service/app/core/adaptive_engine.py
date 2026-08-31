import math
from typing import List, Dict, Any
from app.models.schemas import (
    RoadmapPhaseResponse, RoadmapItemResponse, AdaptRoadmapResponse
)

class AdaptiveEngine:
    def __init__(self):
        pass

    def adapt_roadmap(
            self,
            phases: List[RoadmapPhaseResponse],
            trigger_event: str,
            event_data: Dict[str, Any]
    ) -> AdaptRoadmapResponse:
        
        adapted_phases = [p.model_copy(deep=True) for p in phases]
        adaptation_summary = ""
        total_hours = sum(p.estimated_hours for p in adapted_phases)
        weekly_hours = event_data.get("weekly_hours", 10)

        if trigger_event == "ASSESSMENT_RESULT":
            score = event_data.get("score_percentage", 0)
            skill_name = event_data.get("skill_name", "Core Skill")
            phase_num = event_data.get("phase_number", 1)

            if score >= 90:
                # Fast-track rule
                adaptation_summary = f"High Mastery Detected ({score}% in {skill_name}). Automatically satisfied prerequisite requirements and unlocked downstream Phase {phase_num + 1}."
                # Unlock next phase
                for p in adapted_phases:
                    if p.phase_number == phase_num:
                        p.status = "COMPLETED"
                        for item in p.items:
                            item.status = "COMPLETED"
                    elif p.phase_number == phase_num + 1:
                        p.status = "AVAILABLE"
                        if p.items:
                            p.items[0].status = "AVAILABLE"
                            p.items[0].is_locked = False
            elif score >= 70:
                adaptation_summary = f"Assessment Passed ({score}% in {skill_name}). Progressing sequentially to Phase {phase_num + 1}."
                for p in adapted_phases:
                    if p.phase_number == phase_num:
                        p.status = "COMPLETED"
                    elif p.phase_number == phase_num + 1:
                        p.status = "AVAILABLE"
                        if p.items:
                            p.items[0].status = "AVAILABLE"
                            p.items[0].is_locked = False
            else:
                # Remediation rule
                adaptation_summary = f"Score below passing standard ({score}% in {skill_name}). Injected prerequisite remedial module into Phase {phase_num} before advancing."
                for p in adapted_phases:
                    if p.phase_number == phase_num:
                        remedial_item = RoadmapItemResponse(
                            item_type="RESOURCE",
                            title=f"Targeted Refresher: Deep Dive into {skill_name} Foundations",
                            url="https://dev.java/learn/",
                            estimated_hours=6.0,
                            order_index=len(p.items) + 1,
                            status="AVAILABLE",
                            recommendation_score=99.0,
                            recommendation_reason="Remedial review module added due to recent assessment diagnostic.",
                            is_locked=False
                        )
                        p.items.append(remedial_item)
                        p.estimated_hours += 6.0
                        total_hours += 6.0

        elif trigger_event == "HOURS_CHANGED":
            new_hours = max(1, event_data.get("weekly_hours", 10))
            weekly_hours = new_hours
            recalculated_weeks = math.ceil(total_hours / weekly_hours)
            adaptation_summary = f"Weekly study commitment adjusted to {new_hours} hours/week. Total roadmap timeline recalculated to {recalculated_weeks} weeks."

        elif trigger_event == "SKILL_OVERRIDE":
            overridden_skill = event_data.get("skill_name", "")
            adaptation_summary = f"Skill '{overridden_skill}' updated to advanced proficiency. Prerequisite modules marked complete and advanced capstone project assigned."
            for p in adapted_phases:
                for item in p.items:
                    if overridden_skill.lower() in item.title.lower():
                        item.status = "COMPLETED"

        else:
            adaptation_summary = "Roadmap synchronized with updated learner preferences."

        recalculated_weeks = math.ceil(total_hours / max(1, weekly_hours))

        return AdaptRoadmapResponse(
            adapted_phases=adapted_phases,
            adaptation_summary=adaptation_summary,
            recalculated_weeks=recalculated_weeks
        )
