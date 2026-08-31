import numpy as np
from typing import Dict, List, Any, Optional, Tuple
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from app.models.schemas import LearnerProfileInput, ResourceInput, ProjectInput, ScoredItemResponse
from app.core.skill_graph import SkillGraph

class RecommendationScorer:
    def __init__(self, skill_graph: SkillGraph):
        self.skill_graph = skill_graph
        self.vectorizer = TfidfVectorizer(stop_words='english')
        
        # Configurable mathematical scoring weights:
        self.weights = {
            "skill_gap": 0.30,
            "goal_relevance": 0.25,
            "prerequisites": 0.15,
            "difficulty": 0.10,
            "learning_style": 0.10,
            "quality": 0.10
        }

    def compute_semantic_goal_similarity(self, goal_text: str, candidate_texts: List[str]) -> np.ndarray:
        """Computes TF-IDF cosine similarity between learner's goal and candidate resources."""
        if not goal_text or not candidate_texts:
            return np.zeros(len(candidate_texts))
        
        corpus = [goal_text] + candidate_texts
        tfidf_matrix = self.vectorizer.fit_transform(corpus)
        sims = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:])[0]
        return sims

    def score_resource(
            self,
            resource: ResourceInput,
            profile: LearnerProfileInput,
            current_skills_map: Dict[str, int],
            target_skills_map: Dict[str, int],
            semantic_sim: float
    ) -> ScoredItemResponse:
        
        # 1. Skill Gap Match (0.30)
        gap_scores = []
        for s in resource.skills_taught:
            s_lower = s.lower()
            current_p = current_skills_map.get(s_lower, 0)
            required_p = target_skills_map.get(s, 75)
            if current_p < required_p:
                gap_scores.append((required_p - current_p) / 100.0)
            else:
                gap_scores.append(0.1) # low boost if already mastered
        
        skill_gap_match = float(np.mean(gap_scores)) if gap_scores else 0.4
        skill_gap_match = min(1.0, max(0.0, skill_gap_match))

        # 2. Goal Relevance (0.25)
        # Combine role keyword match + TF-IDF semantic similarity
        role_lower = profile.target_role.lower()
        title_lower = resource.title.lower()
        desc_lower = resource.description.lower()
        
        keyword_boost = 0.5
        if (("rag" in role_lower or "generative" in role_lower or "llm" in role_lower) and any(k in title_lower or k in desc_lower for k in ["rag", "langchain", "vector", "prompt", "embedding", "llm", "ragas"])) or \
           (("java" in role_lower or "backend" in role_lower) and ("java" in title_lower or "spring" in title_lower or "sql" in title_lower)) or \
           (("fullstack" in role_lower or "web" in role_lower) and ("react" in title_lower or "node" in title_lower or "javascript" in title_lower)) or \
           (("devops" in role_lower or "cloud" in role_lower) and ("docker" in title_lower or "kubernetes" in title_lower or "cloud" in title_lower)) or \
           ("ai" in role_lower and ("python" in title_lower or "learning" in title_lower or "pytorch" in title_lower)):
            keyword_boost = 0.95

        goal_relevance = 0.6 * keyword_boost + 0.4 * float(semantic_sim)
        goal_relevance = min(1.0, max(0.2, goal_relevance))

        # 3. Prerequisite Readiness (0.15)
        prerequisites_met = True
        missing_prereqs = []
        taught_keys = {s.lower() for s in resource.skills_taught}
        
        for s in resource.skills_taught:
            prereqs = self.skill_graph.get_prerequisites_for_skill(s)
            for p_name, strength in prereqs:
                if p_name.lower() not in taught_keys:
                    p_level = current_skills_map.get(p_name.lower(), 0)
                    if p_level < 50:
                        prerequisites_met = False
                        missing_prereqs.append(f"{p_name} ({p_level}%)")

        prereq_score = 1.0 if prerequisites_met else 0.35

        # 4. Difficulty Compatibility (0.10)
        exp_level = profile.experience_level.upper()
        res_diff = resource.difficulty.upper()
        if exp_level == res_diff:
            diff_score = 1.0
        elif (exp_level == "BEGINNER" and res_diff == "INTERMEDIATE") or (exp_level == "INTERMEDIATE" and res_diff == "ADVANCED"):
            diff_score = 0.75
        elif exp_level == "BEGINNER" and res_diff == "ADVANCED":
            diff_score = 0.4
        else:
            diff_score = 0.85

        # 5. Learning Style Preference (0.10)
        style = profile.preferred_style.upper()
        r_type = resource.resource_type.upper()
        if style == "VIDEO" and r_type == "VIDEO":
            style_score = 1.0
        elif style == "READING" and r_type in ["BOOK", "DOCUMENTATION", "ARTICLE"]:
            style_score = 1.0
        elif style == "PRACTICAL" and r_type in ["COURSE", "TUTORIAL", "EXERCISE"]:
            style_score = 1.0
        else:
            style_score = 0.65

        # 6. Quality & Rating (0.10)
        quality_score = float(resource.quality_score or 0.9)

        # Prerequisite Readiness multiplier (penalizes advanced modules if foundations missing)
        readiness_multiplier = 1.0 if prerequisites_met else 0.70

        # Weighted Final Score (0 - 100)
        total = (
            self.weights["skill_gap"] * skill_gap_match +
            self.weights["goal_relevance"] * goal_relevance +
            self.weights["prerequisites"] * prereq_score +
            self.weights["difficulty"] * diff_score +
            self.weights["learning_style"] * style_score +
            self.weights["quality"] * quality_score
        ) * 100.0 * readiness_multiplier

        final_score = round(float(np.clip(total, 30.0, 99.0)), 1)

        # Match Factors Breakdown
        match_factors = {
            "skillGapMatch": round(skill_gap_match, 2),
            "goalMatch": round(goal_relevance, 2),
            "prerequisiteMatch": round(prereq_score, 2),
            "difficultyMatch": round(diff_score, 2),
            "styleMatch": round(style_score, 2),
            "qualityMatch": round(quality_score, 2)
        }

        # Explainable Reasoning
        explanation = self._generate_explanation(
            resource.title, resource.skills_taught, skill_gap_match,
            prerequisites_met, missing_prereqs, profile
        )

        return ScoredItemResponse(
            id=resource.id,
            title=resource.title,
            description=resource.description,
            type=resource.resource_type,
            url=resource.url,
            platform=resource.platform,
            difficulty=resource.difficulty,
            estimated_hours=resource.estimated_hours,
            score=final_score,
            explanation=explanation,
            match_factors=match_factors,
            skills_taught=resource.skills_taught,
            prerequisites=missing_prereqs,
            is_prerequisites_met=prerequisites_met
        )

    def _generate_explanation(
            self,
            title: str,
            skills_taught: List[str],
            skill_gap_match: float,
            prerequisites_met: bool,
            missing_prereqs: List[str],
            profile: LearnerProfileInput
    ) -> str:
        parts = []
        skills_str = ", ".join(skills_taught) if skills_taught else "core skills"
        
        if skill_gap_match > 0.6:
            parts.append(f"Directly bridges your priority gap in {skills_str} for {profile.target_role}.")
        else:
            parts.append(f"Reinforces foundational proficiency in {skills_str}.")

        if prerequisites_met:
            parts.append("Your verified prerequisites qualify you to absorb this module effectively without blockers.")
        else:
            parts.append(f"Prerequisite advisory: Strengthening {', '.join(missing_prereqs)} first will accelerate comprehension.")

        if profile.preferred_style:
            parts.append(f"Format tailored for your {profile.preferred_style.lower()} learning preference.")

        return " ".join(parts)
