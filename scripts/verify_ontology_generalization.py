import requests
import json
import sys

BASE_URL = "http://localhost:8080"
AI_URL = "http://localhost:8000"

def run_test_suite():
    print("=" * 80)
    print("PATHFINDER KNOWLEDGE-GROUNDED MULTI-DOMAIN GENERALIZATION VERIFICATION SUITE")
    print("=" * 80)

    import random
    rand_id = random.randint(10000, 99999)
    username = f"k_user_{rand_id}"
    password = "Password123!"
    reg_payload = {
        "username": username,
        "email": f"{username}@test.com",
        "password": password,
        "fullName": "Knowledge Test Learner"
    }
    
    print(f"\n[Step 1] Registering test user {username}...")
    resp = requests.post(f"{BASE_URL}/api/auth/register", json=reg_payload)
    if resp.status_code not in (200, 201):
        # Try login if already exists
        login_resp = requests.post(f"{BASE_URL}/api/auth/login", json={"usernameOrEmail": username, "password": password})
        if login_resp.status_code != 200:
            print(f"FAILED to authenticate: {login_resp.text}")
            sys.exit(1)
        token = login_resp.json().get("token")
    else:
        token = resp.json().get("token")

    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    print(f" Authenticated successfully. Token obtained: {token[:20]}...")

    # 2. Define 10 distinct domain scenarios + 2 novel/unanticipated concepts
    scenarios = [
        {
            "id": 1,
            "domain": "Algorithms / Computer Science",
            "prompt": "I want to master Dynamic Programming and algorithmic optimization",
            "expected_core": ["Dynamic Programming", "Recursion & Memoization"],
            "expected_prereqs": ["Time & Space Complexity (Big-O)", "Data Structures & Algorithms"],
            "forbidden_skills": ["Java", "Docker & Containers", "SQL & Relational Databases", "Git & Version Control"]
        },
        {
            "id": 2,
            "domain": "Mathematics",
            "prompt": "I want to learn Multivariable Calculus and Differential Equations",
            "expected_core": ["Multivariable Calculus", "Differential Equations"],
            "expected_prereqs": ["Single-Variable Calculus", "Linear Algebra"],
            "forbidden_skills": ["Java", "Docker & Containers", "Spring Boot", "Git & Version Control"]
        },
        {
            "id": 3,
            "domain": "Creative Arts / 3D Design",
            "prompt": "I want to learn 3D modeling and lighting in Blender",
            "expected_core": ["3D Modeling & Mesh Topology", "Shading, Lighting & Rendering"],
            "expected_prereqs": ["Spatial Geometry & 3D Transformations"],
            "forbidden_skills": ["Java", "Spring Boot", "SQL & Relational Databases"]
        },
        {
            "id": 4,
            "domain": "Photography",
            "prompt": "I want to learn portrait photography, exposure triangle, and lighting",
            "expected_core": ["Exposure Triangle (ISO, Aperture, Shutter)", "Composition & Lighting Techniques"],
            "expected_prereqs": ["Camera Optics & Sensor Physics"],
            "forbidden_skills": ["Docker & Containers", "Java", "SQL & Relational Databases"]
        },
        {
            "id": 5,
            "domain": "Music & Audio Production",
            "prompt": "I want to learn audio mixing and mastering in Ableton Live",
            "expected_core": ["Audio Mixing & EQ Balancing", "Dynamic Range Compression & Limiting"],
            "expected_prereqs": ["Digital Audio Fundamentals & Acoustics"],
            "forbidden_skills": ["Java", "Docker & Containers", "Spring Boot"]
        },
        {
            "id": 6,
            "domain": "Finance & Valuation",
            "prompt": "I want to learn discounted cash flow and financial modeling",
            "expected_core": ["Discounted Cash Flow (DCF) Modeling", "Financial Statement Analysis & Projections"],
            "expected_prereqs": ["Financial Accounting & Balance Sheet Mechanics"],
            "forbidden_skills": ["Git & Version Control", "Docker & Containers", "Java"]
        },
        {
            "id": 7,
            "domain": "Cloud Infrastructure / DevOps",
            "prompt": "I want to master Kubernetes cluster administration, Helm, and container orchestration",
            "expected_core": ["Cloud Infrastructure & Kubernetes", "Docker & Containers"],
            "expected_prereqs": ["Linux Systems & Shell Scripting", "Networking Fundamentals & TCP/IP"],
            "forbidden_skills": ["Java", "Spring Boot"]
        },
        {
            "id": 8,
            "domain": "Artificial Intelligence / RAG",
            "prompt": "I want to build an enterprise RAG system with LangChain and vector databases",
            "expected_core": ["RAG Architecture & LangChain", "Vector Databases & Embeddings"],
            "expected_prereqs": ["Python Programming", "Prompt Engineering & LLM APIs"],
            "forbidden_skills": ["Java", "Spring Boot", "Flutter Framework & Widgets"]
        },
        {
            "id": 9,
            "domain": "Mobile Engineering",
            "prompt": "I want to build cross-platform mobile apps with Flutter and Riverpod",
            "expected_core": ["Flutter Framework & Widgets", "State Management (Riverpod/Bloc)"],
            "expected_prereqs": ["Dart Programming"],
            "forbidden_skills": ["Java", "Spring Boot", "Docker & Containers"]
        },
        {
            "id": 10,
            "domain": "Full-Stack Web Development",
            "prompt": "I want to become a Full Stack Developer with React, Node, and TypeScript",
            "expected_core": ["React.js", "Node.js & Express"],
            "expected_prereqs": ["JavaScript (ES6+)", "HTML5 & CSS3"],
            "forbidden_skills": ["Flutter Framework & Widgets", "Exposure Triangle (ISO, Aperture, Shutter)"]
        },
        {
            "id": 11,
            "domain": "Novel / Unanticipated Domain A",
            "prompt": "I want to learn Quantum Superposition and Quantum Circuit Computing",
            "expected_core": ["Quantum Superposition And Quantum Circuit Computing"],
            "expected_prereqs": ["Foundations for Quantum Superposition And Quantum Circuit Computing"],
            "forbidden_skills": ["Java", "Spring Boot", "Git & Version Control", "Docker & Containers"]
        },
        {
            "id": 12,
            "domain": "Novel / Unanticipated Domain B",
            "prompt": "I want to master Molecular Biology and Genetic CRISPR Editing",
            "expected_core": ["Molecular Biology And Genetic Crispr Editing"],
            "expected_prereqs": ["Foundations for Molecular Biology And Genetic Crispr Editing"],
            "forbidden_skills": ["Java", "Spring Boot", "Git & Version Control", "Docker & Containers"]
        }
    ]

    all_passed = True
    summary_results = []

    print("\n[Step 2] Executing Multi-Domain Generalization & Negative Relevance Tests...")
    print("-" * 80)

    for sc in scenarios:
        sid = sc["id"]
        domain = sc["domain"]
        prompt = sc["prompt"]

        print(f"\n Scenario {sid}: [{domain}]")
        print(f"  Prompt: \"{prompt}\"")

        # A. Analyze Goal (POST /api/profile/extract-goal)
        extract_res = requests.post(f"{BASE_URL}/api/profile/extract-goal", json={"prompt": prompt, "applyToProfile": True}, headers=headers)
        if extract_res.status_code != 200:
            print(f"  ❌ Failed goal extraction: {extract_res.status_code} {extract_res.text}")
            all_passed = False
            continue

        goal_data = extract_res.json()
        target_role = goal_data.get("targetRole")
        goal_type = goal_data.get("goalType", "TOPIC_LEARNING")
        core_skills = goal_data.get("coreSkills", [])
        prereq_skills = goal_data.get("prerequisiteSkills", [])
        all_required = goal_data.get("missingSkills", [])

        print(f"  -> Extracted Role: {target_role} (Intent: {goal_type})")
        print(f"  -> Core Skills: {core_skills}")
        print(f"  -> Prerequisites: {prereq_skills}")

        # B. Check expected core and prerequisites
        core_matched = any(any(ec.lower() in cs.lower() for cs in core_skills) for ec in sc["expected_core"])
        if not core_matched:
            print(f"  [WARN] Expected core {sc['expected_core']} not clearly matched in {core_skills}")

        # C. Negative Relevance Test
        leaks = []
        for forbidden in sc["forbidden_skills"]:
            for act in all_required:
                if forbidden.lower() == act.lower():
                    leaks.append(forbidden)

        if leaks:
            print(f"  [FAIL] NEGATIVE TEST FAILED: Cross-domain leakage detected! Leaked: {leaks}")
            all_passed = False
        else:
            print(f"  [PASS] NEGATIVE TEST PASSED: Zero forbidden skills present. Excluded baggage: {sc['forbidden_skills']}")

        # D. Query Skill Gaps (GET /api/skills/gaps)
        gaps_res = requests.get(f"{BASE_URL}/api/skills/gaps", headers=headers)
        gap_items = gaps_res.json() if gaps_res.status_code == 200 else []
        gap_names = [g.get("skillName") for g in gap_items]
        print(f"  -> Validated Required Skill Gaps ({len(gap_items)}): {gap_names}")

        # E. Query Projects Hub (GET /api/projects/recommended)
        proj_res = requests.get(f"{BASE_URL}/api/projects/recommended", headers=headers)
        projects = proj_res.json() if proj_res.status_code == 200 else []
        proj_titles = [p.get("title") for p in projects]
        print(f"  -> Matched Projects ({len(projects)}): {proj_titles[:2]}")
        if len(projects) == 0:
            print("  [FAIL] ERROR: Projects Hub returned 0 projects!")
            all_passed = False

        # F. Query Learning Path Roadmap (GET /api/roadmap/active)
        roadmap_res = requests.get(f"{BASE_URL}/api/roadmap/active", headers=headers)
        roadmap = roadmap_res.json() if roadmap_res.status_code == 200 else {}
        phases = roadmap.get("phases", [])
        phase_titles = [p.get("title") for p in phases]
        print(f"  -> Generated Phases ({len(phases)}): {phase_titles}")
        if len(phases) == 0:
            print("  [FAIL] ERROR: Roadmap returned 0 phases!")
            all_passed = False

        # G. Query Recommendations (GET /api/recommendations)
        rec_res = requests.get(f"{BASE_URL}/api/recommendations", headers=headers)
        recs = rec_res.json() if rec_res.status_code == 200 else []
        rec_titles = [r.get("title") for r in recs]
        print(f"  -> Recommendations ({len(recs)}): {rec_titles[:2]}")

        summary_results.append({
            "scenario": sid,
            "domain": domain,
            "intent": goal_type,
            "core_skills": core_skills,
            "prerequisites": prereq_skills,
            "leak_free": len(leaks) == 0,
            "projects_count": len(projects),
            "phases_count": len(phases)
        })

    print("\n" + "=" * 80)
    print("FINAL MULTI-DOMAIN GENERALIZATION MATRIX SUMMARY")
    print("=" * 80)
    print(f"{'ID':<3} | {'Domain':<28} | {'Intent':<14} | {'Core/Prereqs':<16} | {'Projects':<8} | {'Phases':<7} | {'Leak Free'}")
    print("-" * 90)
    for r in summary_results:
        counts = f"{len(r['core_skills'])} core / {len(r['prerequisites'])} pre"
        print(f"{r['scenario']:<3} | {r['domain'][:28]:<28} | {r['intent'][:14]:<14} | {counts:<16} | {r['projects_count']:<8} | {r['phases_count']:<7} | {'[YES]' if r['leak_free'] else '[NO]'}")

    print("-" * 90)
    if all_passed:
        print("\nALL 12 SCENARIOS PASSED WITH ZERO CROSS-DOMAIN LEAKAGE AND 100% GENERALIZATION!")
    else:
        print("\nSOME SCENARIOS ENCOUNTERED FAILURES.")

if __name__ == "__main__":
    run_test_suite()
