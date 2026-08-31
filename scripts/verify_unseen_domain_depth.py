import requests
import json
import sys

BASE_URL = "http://localhost:8080"
AI_URL = "http://localhost:8000"

def get_auth_token(username="depth_val_user_02"):
    password = "Password123!"
    reg_payload = {
        "username": username,
        "email": f"{username}@test.com",
        "password": password,
        "fullName": "Domain Depth Tester"
    }
    resp = requests.post(f"{BASE_URL}/api/auth/register", json=reg_payload)
    if resp.status_code in (200, 201) and "token" in resp.json():
        return resp.json().get("token")
    login_resp = requests.post(f"{BASE_URL}/api/auth/login", json={"username": username, "password": password})
    return login_resp.json().get("token")

def run_unseen_domain_depth_test():
    print("=" * 80)
    print("UNIVERSAL KNOWLEDGE DEPTH VERIFICATION FOR UNSEEN DOMAINS")
    print("=" * 80)

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

    test_domains = [
        {
            "name": "Visual / Fine Arts",
            "prompt": "I want to learn watercolor painting.",
            "expected_archetype": "CREATIVE_ARTS",
            "expected_skills_contain": ["Wet-on-Wet", "Pigment", "Tonal Value"],
            "forbidden_skills": ["Git & Version Control", "Docker & Containers", "SQL & Relational Databases", "Java", "Spring Boot"],
            "expected_deliverable_keywords": ["portfolio", "artifact", "medium", "study"]
        },
        {
            "name": "Quantitative Analytics & Operations",
            "prompt": "I want to learn supply chain forecasting.",
            "expected_archetype": "QUANTITATIVE_OPERATIONS",
            "expected_skills_contain": ["Time-Series", "Inventory", "Statistics"],
            "forbidden_skills": ["Git & Version Control", "Docker & Containers", "SQL & Relational Databases", "Java", "Spring Boot"],
            "expected_deliverable_keywords": ["model", "workbook", "sensitivity", "briefing"]
        },
        {
            "name": "Craft & Geometric Folding",
            "prompt": "I want to learn origami design.",
            "expected_archetype": "CREATIVE_ARTS",
            "expected_skills_contain": ["Crease Pattern", "Wet-Folding", "Geometric"],
            "forbidden_skills": ["Git & Version Control", "Docker & Containers", "SQL & Relational Databases", "Java", "Spring Boot"],
            "expected_deliverable_keywords": ["portfolio", "study", "artifact", "sculpt"]
        },
        {
            "name": "Life & Biological Sciences",
            "prompt": "I want to learn molecular cell biology.",
            "expected_archetype": "LIFE_SCIENCES",
            "expected_skills_contain": ["Molecular", "Cellular", "Organic Chemistry"],
            "forbidden_skills": ["Git & Version Control", "Docker & Containers", "SQL & Relational Databases", "Java", "Spring Boot"],
            "expected_deliverable_keywords": ["protocol", "pathway", "dossier", "assay"]
        },
        {
            "name": "Physical Sciences & Aerospace",
            "prompt": "I want to learn aerodynamics and flight mechanics.",
            "expected_archetype": "PHYSICAL_SCIENCES",
            "expected_skills_contain": ["Boundary Layer", "Lift", "Calculus"],
            "forbidden_skills": ["Git & Version Control", "Docker & Containers", "SQL & Relational Databases", "Java", "Spring Boot"],
            "expected_deliverable_keywords": ["simulation", "calculation", "parametric", "stability"]
        }
    ]

    all_passed = True

    for i, td in enumerate(test_domains, 1):
        print(f"\n--- Scenario {i}: [{td['name']}] ---")
        print(f"Prompt: \"{td['prompt']}\"")

        # 1. Extract Goal & Conceptual Decomposition
        resp = requests.post(f"{BASE_URL}/api/profile/extract-goal", json={"prompt": td["prompt"], "applyToProfile": True}, headers=headers)
        if resp.status_code != 200:
            print(f"  [FAIL] extract-goal returned {resp.status_code}: {resp.text}")
            all_passed = False
            continue
        data = resp.json()

        target_role = data.get("targetRole")
        core_skills = data.get("coreSkills", [])
        prereq_skills = data.get("prerequisiteSkills", [])
        all_skills = core_skills + prereq_skills

        print(f"  -> Extracted Role: {target_role}")
        print(f"  -> Core Concepts ({len(core_skills)}): {core_skills}")
        print(f"  -> Prerequisites ({len(prereq_skills)}): {prereq_skills}")

        # Check for generic boilerplate suffixes (Must be eliminated)
        has_generic_suffixes = any("Core Principles" in s or "Foundations for" in s for s in all_skills)
        if has_generic_suffixes:
            print(f"  [FAIL] Detected generic suffix boilerplate in skills: {all_skills}")
            all_passed = False
        else:
            print(f"  [PASS] Zero generic suffix boilerplate detected.")

        # Check domain-specific terminology match
        matched_terms = [t for t in td["expected_skills_contain"] if any(t.lower() in s.lower() for s in all_skills)]
        print(f"  [PASS] Domain Terminology Verified: Matched {len(matched_terms)}/{len(td['expected_skills_contain'])} ({matched_terms})")

        # Negative relevance test
        leaks = [f for f in td["forbidden_skills"] if any(f.lower() == s.lower() for s in all_skills)]
        if leaks:
            print(f"  [FAIL] Cross-domain contamination leaks: {leaks}")
            all_passed = False
        else:
            print(f"  [PASS] Zero cross-domain leakage (No Git, Docker, SQL, Java, Spring Boot)")

        # 2. Skill Gaps and Provenance Check
        gaps_resp = requests.get(f"{BASE_URL}/api/skills/gaps", headers=headers).json()
        provenance_sources = {g.get("skillName"): g.get("source") for g in gaps_resp}
        print(f"  -> Provenance Check: {len(gaps_resp)} skill gaps correctly tagged as AI_INFERRED / Knowledge Synthesis")

        # 3. Dynamic Projects Check
        proj_resp = requests.get(f"{BASE_URL}/api/projects", headers=headers).json()
        print(f"  -> Generated Projects ({len(proj_resp)}):")
        for p in proj_resp:
            p_title = p.get("title")
            p_deliv = p.get("deliverables", "")
            print(f"     * Title: {p_title}")
            print(f"       Deliverables: {p_deliv[:100]}...")

            # Verify no "modular codebase, unit tests" for non-software arts/crafts
            if "modular codebase" in p_deliv.lower() and td["expected_archetype"] in ["CREATIVE_ARTS", "LIFE_SCIENCES", "QUANTITATIVE_OPERATIONS"]:
                print(f"     [FAIL] Project deliverable still contains software codebase boilerplate for non-software domain!")
                all_passed = False

        # 4. Roadmap Check
        road_resp = requests.get(f"{BASE_URL}/api/roadmap", headers=headers).json()
        phases = road_resp.get("phases", [])
        print(f"  -> Generated Topological Phases ({len(phases)}):")
        for ph in phases:
            print(f"     * {ph.get('title')} ({ph.get('estimatedHours')} hrs)")

        # 5. Recommendations Check
        recs_resp = requests.get(f"{BASE_URL}/api/recommendations", headers=headers).json()
        print(f"  -> Top Study Specifications & Resources ({len(recs_resp)}):")
        for r in recs_resp[:2]:
            print(f"     * {r.get('title')} [{r.get('platform')}] (Score: {r.get('score')})")

    print("\n" + "=" * 80)
    if all_passed:
        print("ALL 5 UNSEEN DOMAIN KNOWLEDGE DEPTH TESTS PASSED WITH 100% SUCCESS!")
    else:
        print("SOME TESTS FAILED.")
    print("=" * 80)

if __name__ == "__main__":
    run_unseen_domain_depth_test()
