import pytest
from app.core.skill_graph import SkillGraph
from app.core.scorer import RecommendationScorer
from app.core.roadmap_generator import RoadmapGenerator
from app.models.schemas import LearnerProfileInput, UserSkillInput

def test_roadmap_generation_multiple_profiles():
    graph = SkillGraph()
    scorer = RecommendationScorer(graph)
    gen = RoadmapGenerator(graph, scorer)

    # Profile A: Backend Java Developer
    prof_java = LearnerProfileInput(
        target_role="Backend Java Developer",
        experience_level="INTERMEDIATE",
        weekly_hours=10,
        skills=[UserSkillInput(skill_name="Java", proficiency_level=80)]
    )
    roadmap_java = gen.generate_roadmap(prof_java)
    assert len(roadmap_java.phases) >= 4
    assert "Java" in roadmap_java.phases[0].title or "Language" in roadmap_java.phases[0].title
    assert roadmap_java.estimated_weeks > 0

    # Profile B: Fullstack Developer
    prof_fs = LearnerProfileInput(
        target_role="Full Stack Developer",
        experience_level="BEGINNER",
        weekly_hours=12,
        skills=[UserSkillInput(skill_name="JavaScript (ES6+)", proficiency_level=30)]
    )
    roadmap_fs = gen.generate_roadmap(prof_fs)
    assert len(roadmap_fs.phases) >= 3
    assert "JavaScript" in roadmap_fs.phases[0].title or "Track" in roadmap_fs.phases[0].title

    # Profile C: AI / ML Engineer
    prof_ai = LearnerProfileInput(
        target_role="AI / ML Engineer",
        experience_level="INTERMEDIATE",
        weekly_hours=15,
        skills=[UserSkillInput(skill_name="Python Programming", proficiency_level=70)]
    )
    roadmap_ai = gen.generate_roadmap(prof_ai)
    assert len(roadmap_ai.phases) >= 3
    assert "Python" in roadmap_ai.phases[0].title or "Track" in roadmap_ai.phases[0].title
