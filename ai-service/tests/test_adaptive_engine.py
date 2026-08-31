import pytest
from app.core.adaptive_engine import AdaptiveEngine
from app.models.schemas import RoadmapPhaseResponse, RoadmapItemResponse

def test_adaptive_engine_high_score_fast_track():
    engine = AdaptiveEngine()
    
    phases = [
        RoadmapPhaseResponse(
            phase_number=1,
            title="Phase 1: Java Foundations",
            description="Core Java",
            status="AVAILABLE",
            estimated_hours=20.0,
            items=[RoadmapItemResponse(id=1, item_type="RESOURCE", title="Java Core", url="https://java.com", estimated_hours=20.0, order_index=1, status="AVAILABLE", recommendation_score=90.0, recommendation_reason="Core")]
        ),
        RoadmapPhaseResponse(
            phase_number=2,
            title="Phase 2: Spring Boot",
            description="Spring Boot",
            status="LOCKED",
            estimated_hours=30.0,
            items=[RoadmapItemResponse(id=2, item_type="RESOURCE", title="Spring Boot", url="https://spring.io", estimated_hours=30.0, order_index=1, status="LOCKED", recommendation_score=95.0, recommendation_reason="Spring")]
        )
    ]

    res = engine.adapt_roadmap(phases, "ASSESSMENT_RESULT", {"score_percentage": 95, "skill_name": "Java", "phase_number": 1})
    assert "High Mastery" in res.adaptation_summary
    assert res.adapted_phases[0].status == "COMPLETED"
    assert res.adapted_phases[1].status == "AVAILABLE"

def test_adaptive_engine_hours_recalculation():
    engine = AdaptiveEngine()
    phases = [
        RoadmapPhaseResponse(phase_number=1, title="Phase 1", description="", status="AVAILABLE", estimated_hours=50.0)
    ]

    res_10h = engine.adapt_roadmap(phases, "HOURS_CHANGED", {"weekly_hours": 10})
    res_5h = engine.adapt_roadmap(phases, "HOURS_CHANGED", {"weekly_hours": 5})

    assert res_5h.recalculated_weeks > res_10h.recalculated_weeks
    assert res_5h.recalculated_weeks == 10
    assert res_10h.recalculated_weeks == 5
