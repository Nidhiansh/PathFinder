from typing import Dict, List, Set, Tuple, Optional
from collections import defaultdict, deque

class SkillNode:
    def __init__(self, name: str, category: str, difficulty: str = "INTERMEDIATE", required_for_roles: Optional[Dict[str, int]] = None):
        self.name = name
        self.category = category
        self.difficulty = difficulty
        self.required_for_roles = required_for_roles or {}
        self.prerequisites: List[Tuple[str, str]] = [] # list of (prerequisite_skill_name, strength)

class SkillGraph:
    def __init__(self):
        self.nodes: Dict[str, SkillNode] = {}
        self.adjacency_list: Dict[str, List[str]] = defaultdict(list) # skill -> dependents
        self.reverse_adjacency: Dict[str, List[str]] = defaultdict(list) # skill -> prerequisites
        self._build_default_graph()

    def add_skill(self, name: str, category: str, difficulty: str = "INTERMEDIATE", required_for_roles: Optional[Dict[str, int]] = None):
        node = SkillNode(name, category, difficulty, required_for_roles)
        self.nodes[name.lower()] = node
        return node

    def add_prerequisite(self, skill_name: str, prerequisite_name: str, strength: str = "REQUIRED"):
        s_key = skill_name.lower()
        p_key = prerequisite_name.lower()
        if s_key in self.nodes and p_key in self.nodes:
            self.nodes[s_key].prerequisites.append((prerequisite_name, strength))
            self.adjacency_list[p_key].append(s_key)
            self.reverse_adjacency[s_key].append(p_key)

    def _build_default_graph(self):
        # Backend Java Ecosystem
        self.add_skill("Java", "LANGUAGE", "INTERMEDIATE", {"backend java developer": 85, "software engineer": 70})
        self.add_skill("Object-Oriented Programming (OOP)", "CORE_CS", "BEGINNER", {"backend java developer": 80, "software engineer": 75})
        self.add_skill("Data Structures & Algorithms", "CORE_CS", "INTERMEDIATE", {"backend java developer": 75, "software engineer": 75, "ai / ml engineer": 75})
        self.add_skill("SQL & Relational Databases", "DATABASE", "INTERMEDIATE", {"backend java developer": 75, "full stack developer": 70, "ai / ml engineer": 65})
        self.add_skill("Spring Boot", "FRAMEWORK", "INTERMEDIATE", {"backend java developer": 80})
        self.add_skill("RESTful APIs", "ARCHITECTURE", "INTERMEDIATE", {"backend java developer": 85, "full stack developer": 85})
        self.add_skill("Spring Data JPA & Hibernate", "FRAMEWORK", "INTERMEDIATE", {"backend java developer": 75})
        self.add_skill("Spring Security & JWT", "FRAMEWORK", "ADVANCED", {"backend java developer": 70})
        self.add_skill("Docker & Containers", "DEVOPS", "INTERMEDIATE", {"backend java developer": 65, "full stack developer": 60, "ai / ml engineer": 65})
        self.add_skill("System Design & Microservices", "ARCHITECTURE", "ADVANCED", {"backend java developer": 70, "full stack developer": 65})

        # Frontend & Fullstack Ecosystem
        self.add_skill("JavaScript (ES6+)", "LANGUAGE", "INTERMEDIATE", {"full stack developer": 85, "frontend developer": 90})
        self.add_skill("HTML5 & CSS3", "FRONTEND", "BEGINNER", {"full stack developer": 80, "frontend developer": 90})
        self.add_skill("React.js", "FRAMEWORK", "INTERMEDIATE", {"full stack developer": 80, "frontend developer": 90})
        self.add_skill("Node.js & Express", "FRAMEWORK", "INTERMEDIATE", {"full stack developer": 75})
        self.add_skill("Git & Version Control", "CORE_CS", "BEGINNER", {"backend java developer": 75, "full stack developer": 80, "software engineer": 75})

        # AI / ML Ecosystem
        self.add_skill("Python Programming", "LANGUAGE", "BEGINNER", {"ai / ml engineer": 90, "data scientist": 90, "generative ai & rag engineer": 90})
        self.add_skill("Mathematics & Statistics for ML", "DATA_AI", "INTERMEDIATE", {"ai / ml engineer": 80, "data scientist": 85})
        self.add_skill("NumPy & Pandas", "DATA_AI", "INTERMEDIATE", {"ai / ml engineer": 85, "data scientist": 90, "generative ai & rag engineer": 70})
        self.add_skill("Scikit-Learn", "DATA_AI", "INTERMEDIATE", {"ai / ml engineer": 80, "data scientist": 85})
        self.add_skill("Deep Learning & PyTorch", "DATA_AI", "ADVANCED", {"ai / ml engineer": 75, "generative ai & rag engineer": 65})
        self.add_skill("Model Deployment & FastAPI", "DATA_AI", "INTERMEDIATE", {"ai / ml engineer": 70, "generative ai & rag engineer": 70})

        # Generative AI & RAG Ecosystem
        self.add_skill("Prompt Engineering & LLM APIs", "GENAI", "BEGINNER", {"generative ai & rag engineer": 90})
        self.add_skill("Foundations of AI & ML", "GENAI", "BEGINNER", {"generative ai & rag engineer": 85, "ai / ml engineer": 80})
        self.add_skill("Vector Databases & Embeddings", "GENAI", "INTERMEDIATE", {"generative ai & rag engineer": 90})
        self.add_skill("RAG Architecture & LangChain", "GENAI", "INTERMEDIATE", {"generative ai & rag engineer": 90})
        self.add_skill("LLM Fine-Tuning & Evaluation", "GENAI", "ADVANCED", {"generative ai & rag engineer": 75})

        # Build Directed Edges
        self.add_prerequisite("Java", "Object-Oriented Programming (OOP)", "REQUIRED")
        self.add_prerequisite("Spring Boot", "Java", "REQUIRED")
        self.add_prerequisite("RESTful APIs", "Spring Boot", "REQUIRED")
        self.add_prerequisite("Spring Data JPA & Hibernate", "SQL & Relational Databases", "REQUIRED")
        self.add_prerequisite("Spring Data JPA & Hibernate", "Spring Boot", "REQUIRED")
        self.add_prerequisite("Spring Security & JWT", "Spring Boot", "REQUIRED")
        self.add_prerequisite("System Design & Microservices", "RESTful APIs", "REQUIRED")
        self.add_prerequisite("System Design & Microservices", "Docker & Containers", "RECOMMENDED")

        self.add_prerequisite("React.js", "JavaScript (ES6+)", "REQUIRED")
        self.add_prerequisite("React.js", "HTML5 & CSS3", "REQUIRED")
        self.add_prerequisite("Node.js & Express", "JavaScript (ES6+)", "REQUIRED")

        self.add_prerequisite("NumPy & Pandas", "Python Programming", "REQUIRED")
        self.add_prerequisite("Scikit-Learn", "NumPy & Pandas", "REQUIRED")
        self.add_prerequisite("Scikit-Learn", "Mathematics & Statistics for ML", "REQUIRED")
        self.add_prerequisite("Deep Learning & PyTorch", "Scikit-Learn", "REQUIRED")
        self.add_prerequisite("Model Deployment & FastAPI", "Python Programming", "REQUIRED")

        # Generative AI & RAG DAG edges
        self.add_prerequisite("Prompt Engineering & LLM APIs", "Python Programming", "REQUIRED")
        self.add_prerequisite("Foundations of AI & ML", "Python Programming", "REQUIRED")
        self.add_prerequisite("Vector Databases & Embeddings", "Foundations of AI & ML", "REQUIRED")
        self.add_prerequisite("Vector Databases & Embeddings", "NumPy & Pandas", "RECOMMENDED")
        self.add_prerequisite("RAG Architecture & LangChain", "Vector Databases & Embeddings", "REQUIRED")
        self.add_prerequisite("RAG Architecture & LangChain", "Prompt Engineering & LLM APIs", "REQUIRED")
        self.add_prerequisite("LLM Fine-Tuning & Evaluation", "RAG Architecture & LangChain", "REQUIRED")
        self.add_prerequisite("LLM Fine-Tuning & Evaluation", "Deep Learning & PyTorch", "RECOMMENDED")

    def get_role_skills(self, role: str) -> Dict[str, int]:
        role_lower = role.lower()
        role_skills = {}
        for key, node in self.nodes.items():
            for r, req_level in node.required_for_roles.items():
                if r in role_lower or role_lower in r:
                    role_skills[node.name] = req_level
                    break
        
        # Fallback defaults if matching role was not explicit
        if not role_skills:
            if "rag" in role_lower or "generative" in role_lower or "llm" in role_lower:
                role_skills = {
                    "Python Programming": 90, "Prompt Engineering & LLM APIs": 85,
                    "Foundations of AI & ML": 85, "Vector Databases & Embeddings": 80,
                    "RAG Architecture & LangChain": 80, "NumPy & Pandas": 70,
                    "Deep Learning & PyTorch": 65, "Model Deployment & FastAPI": 70,
                    "LLM Fine-Tuning & Evaluation": 60
                }
            elif "java" in role_lower or "backend" in role_lower:
                role_skills = {
                    "Java": 85, "Object-Oriented Programming (OOP)": 80,
                    "Data Structures & Algorithms": 75, "SQL & Relational Databases": 75,
                    "Spring Boot": 80, "RESTful APIs": 85,
                    "Spring Data JPA & Hibernate": 75, "Spring Security & JWT": 70,
                    "Docker & Containers": 65, "System Design & Microservices": 70
                }
            elif "fullstack" in role_lower or "web" in role_lower:
                role_skills = {
                    "JavaScript (ES6+)": 85, "HTML5 & CSS3": 80, "React.js": 80,
                    "Node.js & Express": 75, "SQL & Relational Databases": 70,
                    "Git & Version Control": 80, "RESTful APIs": 85, "Docker & Containers": 60
                }
            elif "ai" in role_lower or "machine learning" in role_lower:
                role_skills = {
                    "Python Programming": 90, "Mathematics & Statistics for ML": 80,
                    "NumPy & Pandas": 85, "Scikit-Learn": 80,
                    "Deep Learning & PyTorch": 75, "Model Deployment & FastAPI": 70,
                    "SQL & Relational Databases": 65
                }
            else:
                role_skills = {
                    "Java": 70, "Python Programming": 70, "Data Structures & Algorithms": 75,
                    "SQL & Relational Databases": 65, "Git & Version Control": 75
                }
        return role_skills

    def get_prerequisites_for_skill(self, skill_name: str) -> List[Tuple[str, str]]:
        node = self.nodes.get(skill_name.lower())
        return node.prerequisites if node else []

    def get_transitive_prerequisites(self, skill_name: str) -> Set[str]:
        """Returns all transitive prerequisites for a skill."""
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
        """
        Performs topological sort on a subset of skills, respecting prerequisite DAG order.
        """
        subset_keys = {s.lower() for s in skill_subset}
        in_degree = {k: 0 for k in subset_keys}
        local_adj = defaultdict(list)

        for s in subset_keys:
            for p in self.reverse_adjacency.get(s, []):
                if p in subset_keys:
                    local_adj[p].append(s)
                    in_degree[s] += 1

        # Kahn's algorithm
        queue = deque([k for k, deg in in_degree.items() if deg == 0])
        ordered_keys = []

        while queue:
            curr = queue.popleft()
            ordered_keys.append(curr)
            for neighbor in local_adj[curr]:
                in_degree[neighbor] -= 1
                if in_degree[neighbor] == 0:
                    queue.append(neighbor)

        # Append any remaining disconnected nodes
        for k in subset_keys:
            if k not in ordered_keys:
                ordered_keys.append(k)

        # Map back to display names
        result = []
        for k in ordered_keys:
            node = self.nodes.get(k)
            result.append(node.name if node else k.title())
        return result
