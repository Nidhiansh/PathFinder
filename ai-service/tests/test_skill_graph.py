import pytest
from app.core.skill_graph import SkillGraph

def test_skill_graph_initialization():
    graph = SkillGraph()
    assert "java" in graph.nodes
    assert "spring boot" in graph.nodes
    assert "react.js" in graph.nodes
    assert "python programming" in graph.nodes

def test_prerequisite_lookup():
    graph = SkillGraph()
    spring_prereqs = [p[0].lower() for p in graph.get_prerequisites_for_skill("Spring Boot")]
    assert "java" in spring_prereqs

    react_prereqs = [p[0].lower() for p in graph.get_prerequisites_for_skill("React.js")]
    assert "javascript (es6+)" in react_prereqs

def test_topological_sort():
    graph = SkillGraph()
    skills = ["Spring Boot", "Java", "Object-Oriented Programming (OOP)", "RESTful APIs", "System Design & Microservices"]
    ordered = graph.topological_sort(skills)
    
    # OOP must come before Java, Java before Spring Boot, Spring Boot before RESTful APIs, RESTful APIs before System Design
    oop_idx = next(i for i, s in enumerate(ordered) if "oop" in s.lower() or "object" in s.lower())
    java_idx = next(i for i, s in enumerate(ordered) if s.lower() == "java")
    spring_idx = next(i for i, s in enumerate(ordered) if "spring boot" in s.lower())
    rest_idx = next(i for i, s in enumerate(ordered) if "rest" in s.lower())
    sys_idx = next(i for i, s in enumerate(ordered) if "system design" in s.lower())

    assert oop_idx < java_idx, "OOP must precede Java"
    assert java_idx < spring_idx, "Java must precede Spring Boot"
    assert spring_idx < rest_idx, "Spring Boot must precede RESTful APIs"
    assert rest_idx < sys_idx, "RESTful APIs must precede System Design"
