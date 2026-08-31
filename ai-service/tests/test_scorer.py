import pytest
from app.core.skill_graph import SkillGraph
from app.core.scorer import RecommendationScorer
from app.models.schemas import LearnerProfileInput, UserSkillInput, ResourceInput

@pytest.fixture
def setup_scorer():
    graph = SkillGraph()
    scorer = RecommendationScorer(graph)
    return graph, scorer

def test_recommendation_scoring_beginner_vs_skilled(setup_scorer):
    graph, scorer = setup_scorer
    
    # User 1: Java Beginner (Java: 20%, Spring Boot: 0%)
    prof_beginner = LearnerProfileInput(
        target_role="Backend Java Developer",
        experience_level="BEGINNER",
        preferred_style="PRACTICAL",
        skills=[UserSkillInput(skill_name="Java", proficiency_level=20)]
    )

    # User 2: Java Intermediate with satisfied prereqs (Java: 85%, Spring Boot: 20%)
    prof_ready_for_spring = LearnerProfileInput(
        target_role="Backend Java Developer",
        experience_level="INTERMEDIATE",
        preferred_style="PRACTICAL",
        skills=[
            UserSkillInput(skill_name="Java", proficiency_level=85),
            UserSkillInput(skill_name="Object-Oriented Programming (OOP)", proficiency_level=90),
            UserSkillInput(skill_name="Spring Boot", proficiency_level=20)
        ]
    )

    spring_resource = ResourceInput(
        id=3,
        title="Building RESTful Web Services with Spring Boot",
        description="Official Spring guide to creating production REST APIs with Spring MVC.",
        resource_type="COURSE",
        url="https://spring.io",
        difficulty="INTERMEDIATE",
        estimated_hours=14.0,
        quality_score=0.98,
        skills_taught=["Spring Boot", "RESTful APIs"]
    )

    role_skills = graph.get_role_skills("Backend Java Developer")
    
    current_beg = {s.skill_name.lower(): s.proficiency_level for s in prof_beginner.skills}
    current_ready = {s.skill_name.lower(): s.proficiency_level for s in prof_ready_for_spring.skills}

    res_beg = scorer.score_resource(spring_resource, prof_beginner, current_beg, role_skills, 0.8)
    res_ready = scorer.score_resource(spring_resource, prof_ready_for_spring, current_ready, role_skills, 0.8)

    # The user with satisfied Java prerequisites must score higher for Spring Boot than the beginner with missing prerequisites
    assert res_ready.score > res_beg.score
    assert res_ready.is_prerequisites_met is True
    assert res_beg.is_prerequisites_met is False
