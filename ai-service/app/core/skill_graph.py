from typing import Dict, List, Set, Tuple, Optional
from collections import defaultdict, deque
from app.services.semantic_knowledge_service import SemanticKnowledgeService

class SkillNode:
    def __init__(self, name: str, category: str, difficulty: str = "INTERMEDIATE", required_for_roles: Optional[Dict[str, int]] = None):
        self.name = name
        self.category = category
        self.difficulty = difficulty
        self.required_for_roles = required_for_roles or {}
        self.prerequisites: List[Tuple[str, str]] = [] # list of (prerequisite_skill_name, strength)

class SkillGraph:
    def __init__(self):
        self.knowledge_service = SemanticKnowledgeService()
        self.nodes: Dict[str, SkillNode] = {}
        self.adjacency_list: Dict[str, List[str]] = defaultdict(list)
        self.reverse_adjacency: Dict[str, List[str]] = defaultdict(list)
        self._build_from_knowledge_service()

    def add_skill(self, name: str, category: str, difficulty: str = "INTERMEDIATE", required_for_roles: Optional[Dict[str, int]] = None):
        node = SkillNode(name, category, difficulty, required_for_roles)
        self.nodes[name.lower()] = node
        return node

    def add_prerequisite(self, skill_name: str, prerequisite_name: str, strength: str = "REQUIRED"):
        s_key = skill_name.lower()
        p_key = prerequisite_name.lower()
        if s_key not in self.nodes:
            self.add_skill(skill_name, "CORE_CS")
        if p_key not in self.nodes:
            self.add_skill(prerequisite_name, "CORE_CS")

        self.nodes[s_key].prerequisites.append((prerequisite_name, strength))
        self.adjacency_list[p_key].append(s_key)
        self.reverse_adjacency[s_key].append(p_key)

    def _build_from_knowledge_service(self):
        for name, data in self.knowledge_service.taxonomy.items():
            self.add_skill(name, data.get("category", "CORE_CS"), data.get("difficulty", "INTERMEDIATE"))
            for prereq in data.get("prerequisites", []):
                self.add_prerequisite(name, prereq, "REQUIRED")

    def get_role_skills(self, role: str) -> Dict[str, int]:
        """
        Dynamically resolves role requirements using the universal knowledge graph.
        Zero domain-specific hardcoding.
        """
        resolved = self.knowledge_service.resolve_goal_knowledge(role)
        all_skills = resolved.get("missing_skills", []) or resolved.get("core_skills", [])
        
        reqs = {}
        for s in all_skills:
            reqs[s] = 80
            if s.lower() in self.nodes:
                if self.nodes[s.lower()].difficulty == "ADVANCED":
                    reqs[s] = 85
                elif self.nodes[s.lower()].difficulty == "BEGINNER":
                    reqs[s] = 75

        return reqs if reqs else {role: 80}

    def get_prerequisites_for_skill(self, skill_name: str) -> List[Tuple[str, str]]:
        node = self.nodes.get(skill_name.lower())
        return node.prerequisites if node else []

    def get_transitive_prerequisites(self, skill_name: str) -> Set[str]:
        visited = set()
        queue = deque([skill_name.lower()])
        while queue:
            curr = queue.popleft()
            for prereq in self.reverse_adjacency.get(curr, []):
                if prereq not in visited:
                    visited.add(prereq)
                    queue.append(prereq)
        return visited

    def topological_sort(self, skill_subset: List[str]) -> List[str]:
        subset_keys = {s.lower() for s in skill_subset}
        in_degree = {k: 0 for k in subset_keys}
        local_adj = defaultdict(list)

        for s in subset_keys:
            for p in self.reverse_adjacency.get(s, []):
                if p in subset_keys:
                    local_adj[p].append(s)
                    in_degree[s] += 1

        queue = deque([k for k, deg in in_degree.items() if deg == 0])
        ordered_keys = []

        while queue:
            curr = queue.popleft()
            ordered_keys.append(curr)
            for neighbor in local_adj[curr]:
                in_degree[neighbor] -= 1
                if in_degree[neighbor] == 0:
                    queue.append(neighbor)

        for k in subset_keys:
            if k not in ordered_keys:
                ordered_keys.append(k)

        result = []
        for k in ordered_keys:
            node = self.nodes.get(k)
            result.append(node.name if node else k.title())
        return result
