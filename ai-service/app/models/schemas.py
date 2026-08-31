from pydantic import BaseModel, Field
from typing import List, Dict, Optional, Any

class GoalAnalysisRequest(BaseModel):
    prompt: str

class GoalAnalysisResponse(BaseModel):
    target_role: str
    career_goal: str
    raw_goal: Optional[str] = None
    normalized_goal: Optional[str] = None
    goal_type: Optional[str] = "TOPIC_LEARNING"
    experience_level: str
    estimated_months: int
    extracted_skills: List[str] = []
    missing_skills: List[str] = []
    core_skills: List[str] = []
    prerequisite_skills: List[str] = []
    excluded_skills: List[str] = []
    confidence: Optional[float] = 0.95
    learning_pace: str
    ai_summary: str

class UserSkillInput(BaseModel):
    skill_name: str
    proficiency_level: int = Field(ge=0, le=100)
    is_verified: bool = False

class LearnerProfileInput(BaseModel):
    user_id: Optional[int] = None
    target_role: str = "Backend Java Developer"
    career_goal: str = ""
    experience_level: str = "BEGINNER" # BEGINNER, INTERMEDIATE, ADVANCED
    weekly_hours: int = 10
    preferred_style: str = "PRACTICAL" # PRACTICAL, VISUAL, READING, VIDEO
    preferred_resource_types: str = "COURSE,PROJECT,DOCUMENTATION"
    interests: Optional[str] = ""
    skills: List[UserSkillInput] = []

class SkillGapItem(BaseModel):
    skill_name: str
    category: str
    current_proficiency: int
    required_proficiency: int
    gap: int
    status: str # MASTERED, IN_PROGRESS, MISSING
    unsatisfied_prerequisites: List[str] = []

class SkillGapRequest(BaseModel):
    target_role: str
    learner_skills: List[UserSkillInput] = []

class SkillGapResponse(BaseModel):
    target_role: str
    gaps: List[SkillGapItem]
    mastered_count: int
    in_progress_count: int
    missing_count: int

class ResourceInput(BaseModel):
    id: int
    title: str
    description: str
    resource_type: str # COURSE, VIDEO, DOCUMENTATION, BOOK, TUTORIAL, PROJECT
    url: str
    platform: Optional[str] = None
    difficulty: str # BEGINNER, INTERMEDIATE, ADVANCED
    estimated_hours: float
    rating: float = 4.5
    quality_score: float = 0.9
    skills_taught: List[str] = []

class ProjectInput(BaseModel):
    id: int
    title: str
    description: str
    difficulty: str
    estimated_hours: float
    deliverables: str
    rubric: str
    primary_skill: str
    github_template_url: Optional[str] = None

class ScoredItemResponse(BaseModel):
    id: int
    title: str
    description: str
    type: str
    url: str
    platform: Optional[str] = None
    difficulty: str
    estimated_hours: float
    score: float
    explanation: str
    match_factors: Dict[str, float]
    skills_taught: List[str] = []
    prerequisites: List[str] = []
    is_prerequisites_met: bool = True

class RecommendationRequest(BaseModel):
    profile: LearnerProfileInput
    resources: Optional[List[ResourceInput]] = None
    projects: Optional[List[ProjectInput]] = None
    top_k: int = 10

class RecommendationResponse(BaseModel):
    recommendations: List[ScoredItemResponse]
    total_evaluated: int

class RoadmapItemResponse(BaseModel):
    id: Optional[int] = None
    item_type: str # RESOURCE, PROJECT, ASSESSMENT
    title: str
    url: str
    estimated_hours: float
    order_index: int
    status: str # AVAILABLE, LOCKED, IN_PROGRESS, COMPLETED
    recommendation_score: float
    recommendation_reason: str
    required_prerequisites: List[str] = []
    is_locked: bool = False

class RoadmapPhaseResponse(BaseModel):
    phase_number: int
    title: str
    description: str
    status: str # AVAILABLE, LOCKED, IN_PROGRESS, COMPLETED
    estimated_hours: float
    items: List[RoadmapItemResponse] = []

class RoadmapGenerationRequest(BaseModel):
    profile: LearnerProfileInput

class RoadmapGenerationResponse(BaseModel):
    title: str
    target_role: str
    total_estimated_hours: float
    estimated_weeks: int
    phases: List[RoadmapPhaseResponse]

class ExplainRecommendationRequest(BaseModel):
    resource_id: int
    resource_title: str
    resource_skills: List[str]
    profile: LearnerProfileInput

class ExplainRecommendationResponse(BaseModel):
    summary: str
    score: float
    factor_breakdown: Dict[str, float]
    prerequisite_status: str
    actionable_tip: str

class AdaptRoadmapRequest(BaseModel):
    current_phases: List[RoadmapPhaseResponse]
    trigger_event: str # ASSESSMENT_RESULT, HOURS_CHANGED, FEEDBACK, SKILL_UPDATED
    event_data: Dict[str, Any]

class AdaptRoadmapResponse(BaseModel):
    adapted_phases: List[RoadmapPhaseResponse]
    adaptation_summary: str
    recalculated_weeks: int

class ChatRequestModel(BaseModel):
    message: str
    context: Optional[Dict[str, Any]] = None

class ChatResponseModel(BaseModel):
    reply: str
    suggested_action: str = "GENERAL"
    action_type: Optional[str] = "NONE"
    action_payload: Optional[Dict[str, Any]] = None
    quick_replies: List[str] = []

class ProjectResponse(BaseModel):
    id: int
    title: str
    description: str
    difficulty: str # BEGINNER, INTERMEDIATE, ADVANCED
    estimated_hours: float
    deliverables: str
    rubric: str
    primary_skill: str
    skills: List[str] = []
    github_template_url: str = "https://github.com"
    is_ai_generated: bool = True
    roadmap_phase: Optional[str] = None
    score: float = 90.0
    explanation: Optional[str] = None

class ProjectGenerationRequest(BaseModel):
    target_role: str
    career_goal: Optional[str] = None
    experience_level: str = "INTERMEDIATE" # BEGINNER, INTERMEDIATE, ADVANCED
    skills: List[str] = []
    skill_gaps: List[str] = []
    roadmap_phases: List[str] = []
    custom_topic: Optional[str] = None

