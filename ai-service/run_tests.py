import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.core.skill_graph import SkillGraph
from app.core.scorer import RecommendationScorer
from app.core.roadmap_generator import RoadmapGenerator
from app.core.adaptive_engine import AdaptiveEngine
from app.services.nlp_parser import NLPGoalParser
from app.models.schemas import LearnerProfileInput, UserSkillInput, ResourceInput, RoadmapPhaseResponse, RoadmapItemResponse

def run_all_tests():
    print("=== STARTING AI SERVICE TEST SUITE ===")
    
    # 1. Test Skill Graph
    graph = SkillGraph()
    assert "java" in graph.nodes, "Java missing from graph"
    assert "spring boot" in graph.nodes, "Spring Boot missing from graph"
    prereqs = [p[0].lower() for p in graph.get_prerequisites_for_skill("Spring Boot")]
    assert "java" in prereqs, "Java must be prerequisite of Spring Boot"
    
    skills = ["Spring Boot", "Java", "Object-Oriented Programming (OOP)", "RESTful APIs", "System Design & Microservices"]
    ordered = graph.topological_sort(skills)
    oop_idx = next(i for i, s in enumerate(ordered) if "oop" in s.lower() or "object" in s.lower())
    java_idx = next(i for i, s in enumerate(ordered) if s.lower() == "java")
    spring_idx = next(i for i, s in enumerate(ordered) if "spring boot" in s.lower())
    assert oop_idx < java_idx < spring_idx, "Topological sort order violated"
    print("[PASS] SkillGraph & Topological DAG Tests Passed")

    # 2. Test Recommendation Scorer
    scorer = RecommendationScorer(graph)
    prof_beg = LearnerProfileInput(
        target_role="Backend Java Developer", experience_level="BEGINNER", preferred_style="PRACTICAL",
        skills=[UserSkillInput(skill_name="Java", proficiency_level=20)]
    )
    prof_ready = LearnerProfileInput(
        target_role="Backend Java Developer", experience_level="INTERMEDIATE", preferred_style="PRACTICAL",
        skills=[
            UserSkillInput(skill_name="Java", proficiency_level=85),
            UserSkillInput(skill_name="Object-Oriented Programming (OOP)", proficiency_level=90),
            UserSkillInput(skill_name="Spring Boot", proficiency_level=20)
        ]
    )
    spring_res = ResourceInput(
        id=3, title="Building RESTful Web Services with Spring Boot", description="Official Spring guide",
        resource_type="COURSE", url="https://spring.io", difficulty="INTERMEDIATE", estimated_hours=14.0,
        quality_score=0.98, skills_taught=["Spring Boot", "RESTful APIs"]
    )
    role_skills = graph.get_role_skills("Backend Java Developer")
    res_beg = scorer.score_resource(spring_res, prof_beg, {"java": 20}, role_skills, 0.8)
    res_ready = scorer.score_resource(spring_res, prof_ready, {"java": 85, "object-oriented programming (oop)": 90, "spring boot": 20}, role_skills, 0.8)
    assert res_ready.score > res_beg.score, "Ready user should score higher than missing-prereq beginner"
    assert res_ready.is_prerequisites_met is True
    assert res_beg.is_prerequisites_met is False
    print(f"[PASS] Scorer Multi-Factor Tests Passed (Ready: {res_ready.score}%, Beg: {res_beg.score}%)")

    # 3. Test Roadmap Generator for Multiple Profiles
    gen = RoadmapGenerator(graph, scorer)
    roadmap_java = gen.generate_roadmap(prof_ready)
    assert len(roadmap_java.phases) >= 4
    assert roadmap_java.estimated_weeks > 0

    prof_fs = LearnerProfileInput(
        target_role="Full Stack Developer", experience_level="BEGINNER", weekly_hours=12,
        skills=[UserSkillInput(skill_name="JavaScript (ES6+)", proficiency_level=40)]
    )
    roadmap_fs = gen.generate_roadmap(prof_fs)
    assert len(roadmap_fs.phases) >= 3

    prof_ai = LearnerProfileInput(
        target_role="AI / ML Engineer", experience_level="INTERMEDIATE", weekly_hours=15,
        skills=[UserSkillInput(skill_name="Python Programming", proficiency_level=75)]
    )
    roadmap_ai = gen.generate_roadmap(prof_ai)
    assert len(roadmap_ai.phases) >= 3
    print(f"[PASS] Dynamic Roadmap Generator Tests Passed (Java Phases: {len(roadmap_java.phases)}, FS Phases: {len(roadmap_fs.phases)}, AI Phases: {len(roadmap_ai.phases)})")

    # 4. Test Adaptive Engine
    adaptive = AdaptiveEngine()
    test_phases = [
        RoadmapPhaseResponse(phase_number=1, title="Phase 1", description="", status="AVAILABLE", estimated_hours=20.0, items=[]),
        RoadmapPhaseResponse(phase_number=2, title="Phase 2", description="", status="LOCKED", estimated_hours=30.0, items=[])
    ]
    adapt_high = adaptive.adapt_roadmap(test_phases, "ASSESSMENT_RESULT", {"score_percentage": 95, "skill_name": "Java", "phase_number": 1})
    assert adapt_high.adapted_phases[0].status == "COMPLETED"
    assert adapt_high.adapted_phases[1].status == "AVAILABLE"

    adapt_hours = adaptive.adapt_roadmap(test_phases, "HOURS_CHANGED", {"weekly_hours": 5})
    assert adapt_hours.recalculated_weeks == 10
    print("[PASS] Adaptive Engine Tests Passed (Fast-track and Hours Recalculation verified)")

    # 5. Test NLP Goal Parser
    nlp = NLPGoalParser()
    p1 = nlp.parse_goal("I want to become a backend Java developer and get an internship in 6 months.")
    assert p1.target_role == "Backend Java Developer"
    assert "Spring Boot" in p1.missing_skills
    assert p1.estimated_months == 6

    p2 = nlp.parse_goal("I want to learn AI and machine learning for data science in 8 months.")
    assert p2.target_role == "AI / ML Engineer"
    assert "Scikit-Learn" in p2.missing_skills
    print("[PASS] NLP Goal Parser Tests Passed")

    print("=== ALL AI SERVICE TESTS PASSED SUCCESSFULLY! ===")

if __name__ == "__main__":
    run_all_tests()
