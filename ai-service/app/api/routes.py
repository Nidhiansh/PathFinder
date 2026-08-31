from fastapi import APIRouter, HTTPException, Depends
from typing import List, Dict, Any
from app.models.schemas import (
    GoalAnalysisRequest, GoalAnalysisResponse,
    SkillGapRequest, SkillGapResponse, SkillGapItem,
    RecommendationRequest, RecommendationResponse, ScoredItemResponse,
    RoadmapGenerationRequest, RoadmapGenerationResponse,
    ExplainRecommendationRequest, ExplainRecommendationResponse,
    AdaptRoadmapRequest, AdaptRoadmapResponse,
    ChatRequestModel, ChatResponseModel, ResourceInput,
    ProjectGenerationRequest, ProjectResponse
)
from app.core.skill_graph import SkillGraph
from app.core.scorer import RecommendationScorer
from app.core.roadmap_generator import RoadmapGenerator
from app.core.adaptive_engine import AdaptiveEngine
from app.services.nlp_parser import NLPGoalParser
from app.services.semantic_knowledge_service import get_semantic_service
from app.services.llm_service import get_llm_service, BaseLLMService

router = APIRouter(prefix="/ai", tags=["AI Engine"])

# Initialize Singletons
skill_graph = SkillGraph()
scorer = RecommendationScorer(skill_graph)
roadmap_gen = RoadmapGenerator(skill_graph, scorer)
adaptive_engine = AdaptiveEngine()
nlp_parser = NLPGoalParser()
semantic_service = get_semantic_service()

@router.post("/analyze-goal", response_model=GoalAnalysisResponse)
async def analyze_goal(request: GoalAnalysisRequest):
    """
    Extracts target career, target skills, existing skills, missing skills,
    experience level, and timeline from natural language goal prompts.
    """
    if not request.prompt or not request.prompt.strip():
        raise HTTPException(status_code=400, detail="Goal prompt cannot be empty")
    return semantic_service.analyze_intent(request.prompt)

@router.post("/analyze-skill-gap", response_model=SkillGapResponse)
async def analyze_skill_gap(request: SkillGapRequest):
    """
    Calculates detailed skill gap breakdown between learner's current verified
    proficiencies and the requirements for the target role.
    """
    role_skills = skill_graph.get_role_skills(request.target_role)
    current_map = {s.skill_name.lower(): s.proficiency_level for s in request.learner_skills}

    gaps: List[SkillGapItem] = []
    mastered = 0
    in_prog = 0
    missing = 0

    for s_name, req_level in role_skills.items():
        curr_level = current_map.get(s_name.lower(), 0)
        gap = max(0, req_level - curr_level)
        
        node = skill_graph.nodes.get(s_name.lower())
        category = node.category if node else "GENERAL"

        if curr_level >= req_level:
            status = "MASTERED"
            mastered += 1
        elif curr_level > 0:
            status = "IN_PROGRESS"
            in_prog += 1
        else:
            status = "MISSING"
            missing += 1

        # Check unsatisfied prerequisites
        unsatisfied = []
        if node:
            for p_name, strength in node.prerequisites:
                p_level = current_map.get(p_name.lower(), 0)
                if p_level < 50:
                    unsatisfied.append(f"{p_name} ({p_level}%)")

        gaps.append(SkillGapItem(
            skill_name=s_name,
            category=category,
            current_proficiency=curr_level,
            required_proficiency=req_level,
            gap=gap,
            status=status,
            unsatisfied_prerequisites=unsatisfied
        ))

    # Sort: High gap first
    gaps.sort(key=lambda g: g.gap, reverse=True)

    return SkillGapResponse(
        target_role=request.target_role,
        gaps=gaps,
        mastered_count=mastered,
        in_progress_count=in_prog,
        missing_count=missing
    )

@router.post("/recommend", response_model=RecommendationResponse)
async def recommend_resources(request: RecommendationRequest):
    """
    Evaluates, scores, and ranks candidate learning resources & projects
    using multi-factor weighted scoring and semantic vector similarity.
    """
    profile = request.profile
    target_role = profile.target_role or "Backend Java Developer"
    role_skills = skill_graph.get_role_skills(target_role)
    current_map = {s.skill_name.lower(): s.proficiency_level for s in profile.skills}

    # If resources were not passed in request body, use default rich corpus
    resources = request.resources or _get_default_candidate_resources()
    
    # Compute TF-IDF semantic similarities
    goal_text = f"{profile.target_role} {profile.career_goal} {profile.interests or ''}"
    resource_texts = [f"{r.title} {r.description} {' '.join(r.skills_taught)}" for r in resources]
    similarities = scorer.compute_semantic_goal_similarity(goal_text, resource_texts)

    scored_items: List[ScoredItemResponse] = []
    for r, sim in zip(resources, similarities):
        scored = scorer.score_resource(r, profile, current_map, role_skills, sim)
        scored_items.append(scored)

    # Sort descending by score
    scored_items.sort(key=lambda x: x.score, reverse=True)
    top_items = scored_items[:request.top_k]

    return RecommendationResponse(
        recommendations=top_items,
        total_evaluated=len(resources)
    )

@router.post("/generate-roadmap", response_model=RoadmapGenerationResponse)
async def generate_roadmap(request: RoadmapGenerationRequest):
    """
    Generates a structured, topological-sorted learning roadmap
    with progressive phase unlocking and milestone projects.
    """
    return roadmap_gen.generate_roadmap(request.profile)

@router.post("/explain-recommendation", response_model=ExplainRecommendationResponse)
async def explain_recommendation(
    request: ExplainRecommendationRequest,
    llm: BaseLLMService = Depends(get_llm_service)
):
    """
    Generates explainable AI justifications for why a specific resource was recommended.
    """
    current_map = {s.skill_name.lower(): s.proficiency_level for s in request.profile.skills}
    role_skills = skill_graph.get_role_skills(request.profile.target_role)
    
    # Check prerequisites
    missing_prereqs = []
    for s in request.resource_skills:
        for p_name, strength in skill_graph.get_prerequisites_for_skill(s):
            p_level = current_map.get(p_name.lower(), 0)
            if p_level < 50:
                missing_prereqs.append(f"{p_name} ({p_level}%)")

    prereq_status = "All prerequisites met." if not missing_prereqs else f"Unsatisfied prerequisites: {', '.join(missing_prereqs)}"
    
    summary = llm.explain_recommendation(
        request.resource_title,
        request.profile.model_dump(),
        {"totalScore": 92.0}
    )

    return ExplainRecommendationResponse(
        summary=summary,
        score=92.0,
        factor_breakdown={
            "skillGapFulfillment": 0.88,
            "careerGoalRelevance": 0.94,
            "prerequisiteReadiness": 1.0 if not missing_prereqs else 0.4,
            "learningStyleMatch": 0.90
        },
        prerequisite_status=prereq_status,
        actionable_tip="Complete the accompanying milestone hands-on project to cement these concepts in your portfolio."
    )

@router.post("/adapt-roadmap", response_model=AdaptRoadmapResponse)
async def adapt_roadmap(request: AdaptRoadmapRequest):
    """
    Dynamically recalculates roadmap phases, timelines, or remediation modules
    based on assessment outcomes, available hours changes, or skill overrides.
    """
    return adaptive_engine.adapt_roadmap(
        request.current_phases,
        request.trigger_event,
        request.event_data
    )

@router.post("/chat", response_model=ChatResponseModel)
async def chat_assistant(
    request: ChatRequestModel,
    llm: BaseLLMService = Depends(get_llm_service)
):
    """
    Conversational AI Learning Assistant with contextual user awareness.
    """
    res = llm.generate_chat_reply(request.message, request.context)
    return ChatResponseModel(
        reply=res["reply"],
        suggested_action=res.get("suggested_action", "GENERAL"),
        action_type=res.get("action_type", "NONE"),
        action_payload=res.get("action_payload", {}),
        quick_replies=res.get("quick_replies", [])
    )

@router.post("/generate-projects", response_model=List[ProjectResponse])
async def generate_projects(
    request: ProjectGenerationRequest,
    llm: BaseLLMService = Depends(get_llm_service)
):
    """
    Dynamically generates structured, phase-aligned portfolio projects
    tailored to the learner's current goal, skills, and skill gaps.
    """
    projects_data = llm.generate_domain_projects(
        target_role=request.target_role,
        career_goal=request.career_goal,
        experience_level=request.experience_level,
        skills=request.skills,
        skill_gaps=request.skill_gaps,
        roadmap_phases=request.roadmap_phases,
        custom_topic=request.custom_topic
    )
    return [ProjectResponse(**p) for p in projects_data]

def _get_default_candidate_resources() -> List[ResourceInput]:
    return [
        ResourceInput(
            id=1,
            title="Modern Java: Collections, Streams, and Concurrency",
            description="Masterclass covering Java 21, Stream pipelines, Optional patterns, and concurrent executor pools.",
            resource_type="COURSE",
            url="https://dev.java/learn/",
            platform="Oracle Java Tutorials",
            difficulty="INTERMEDIATE",
            estimated_hours=16.0,
            quality_score=0.96,
            skills_taught=["Java", "Object-Oriented Programming (OOP)"]
        ),
        ResourceInput(
            id=2,
            title="PostgreSQL High Performance & Schema Architecture",
            description="Guide to relational data modeling, query optimization, B-Tree indexes, and transactions.",
            resource_type="TUTORIAL",
            url="https://www.postgresql.org/docs/current/tutorial.html",
            platform="PostgreSQL Docs",
            difficulty="INTERMEDIATE",
            estimated_hours=12.0,
            quality_score=0.94,
            skills_taught=["SQL & Relational Databases"]
        ),
        ResourceInput(
            id=3,
            title="Building RESTful Web Services with Spring Boot",
            description="Official Spring guide to creating production REST APIs with Spring MVC and dependency injection.",
            resource_type="DOCUMENTATION",
            url="https://spring.io/guides/gs/rest-service/",
            platform="Spring.io",
            difficulty="INTERMEDIATE",
            estimated_hours=14.0,
            quality_score=0.98,
            skills_taught=["Spring Boot", "RESTful APIs"]
        ),
        ResourceInput(
            id=4,
            title="Mastering Spring Data JPA & Hibernate Performance",
            description="Avoid N+1 queries, understand detached entity states, and transactional management.",
            resource_type="COURSE",
            url="https://spring.io/guides/gs/accessing-data-jpa/",
            platform="Spring.io",
            difficulty="INTERMEDIATE",
            estimated_hours=15.0,
            quality_score=0.92,
            skills_taught=["Spring Data JPA & Hibernate"]
        ),
        ResourceInput(
            id=5,
            title="Spring Security 6: Stateless JWT Authentication Architecture",
            description="Implement JWT filter pipelines, BCrypt password hashing, and role authorization.",
            resource_type="COURSE",
            url="https://spring.io/guides/topicals/spring-security-architecture",
            platform="Spring.io",
            difficulty="ADVANCED",
            estimated_hours=18.0,
            quality_score=0.95,
            skills_taught=["Spring Security & JWT"]
        ),
        ResourceInput(
            id=6,
            title="Docker for Backend Developers: Multi-Stage Builds",
            description="Create lightweight JVM container images and orchestrate services with Docker Compose.",
            resource_type="VIDEO",
            url="https://docs.docker.com/get-started/",
            platform="Docker Docs",
            difficulty="INTERMEDIATE",
            estimated_hours=10.0,
            quality_score=0.91,
            skills_taught=["Docker & Containers"]
        ),
        ResourceInput(
            id=7,
            title="Distributed System Design: Scalability, Caching, and Message Queues",
            description="Architecting high-availability systems with Redis caching and asynchronous event queues.",
            resource_type="BOOK",
            url="https://github.com/donnemartin/system-design-primer",
            platform="System Design Primer",
            difficulty="ADVANCED",
            estimated_hours=24.0,
            quality_score=0.99,
            skills_taught=["System Design & Microservices"]
        )
    ]
