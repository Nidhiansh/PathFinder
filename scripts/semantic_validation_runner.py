import requests
import json
import sys

BASE_URL = "http://localhost:8080"
AI_URL = "http://localhost:8000"

def get_auth_token():
    username = "sem_val_user_99"
    password = "Password123!"
    reg_payload = {
        "username": username,
        "email": f"{username}@test.com",
        "password": password,
        "fullName": "Semantic Validation Tester"
    }
    resp = requests.post(f"{BASE_URL}/api/auth/register", json=reg_payload)
    if resp.status_code in (200, 201):
        return resp.json().get("token")
    login_resp = requests.post(f"{BASE_URL}/api/auth/login", json={"usernameOrEmail": username, "password": password})
    return login_resp.json().get("token")

def run_validation():
    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    
    results = {}

    # Test 1: Dynamic Programming runtime flow
    dp_prompt = "I want to learn Dynamic Programming."
    resp_dp = requests.post(f"{BASE_URL}/api/profile/extract-goal", json={"prompt": dp_prompt, "applyToProfile": True}, headers=headers)
    results["dp_extract"] = resp_dp.json()
    
    results["dp_gaps"] = requests.get(f"{BASE_URL}/api/skills/gaps", headers=headers).json()
    results["dp_recs"] = requests.get(f"{BASE_URL}/api/recommendations", headers=headers).json()
    results["dp_projects"] = requests.get(f"{BASE_URL}/api/projects", headers=headers).json()
    results["dp_roadmap"] = requests.get(f"{BASE_URL}/api/roadmap", headers=headers).json()

    # Test 2: Unseen Domain 1 - Watercolor painting
    wc_prompt = "I want to learn watercolor painting."
    resp_wc = requests.post(f"{BASE_URL}/api/profile/extract-goal", json={"prompt": wc_prompt, "applyToProfile": True}, headers=headers)
    results["wc_extract"] = resp_wc.json()
    results["wc_gaps"] = requests.get(f"{BASE_URL}/api/skills/gaps", headers=headers).json()
    results["wc_projects"] = requests.get(f"{BASE_URL}/api/projects", headers=headers).json()
    results["wc_roadmap"] = requests.get(f"{BASE_URL}/api/roadmap", headers=headers).json()
    results["wc_recs"] = requests.get(f"{BASE_URL}/api/recommendations", headers=headers).json()

    # Test 3: Unseen Domain 2 - Supply chain forecasting
    sc_prompt = "I want to learn supply chain forecasting."
    resp_sc = requests.post(f"{BASE_URL}/api/profile/extract-goal", json={"prompt": sc_prompt, "applyToProfile": True}, headers=headers)
    results["sc_extract"] = resp_sc.json()
    results["sc_gaps"] = requests.get(f"{BASE_URL}/api/skills/gaps", headers=headers).json()
    results["sc_projects"] = requests.get(f"{BASE_URL}/api/projects", headers=headers).json()
    results["sc_roadmap"] = requests.get(f"{BASE_URL}/api/roadmap", headers=headers).json()
    results["sc_recs"] = requests.get(f"{BASE_URL}/api/recommendations", headers=headers).json()

    # Test 4: Paraphrases of DP
    p_a = "I want to learn Dynamic Programming."
    p_b = "I want to understand dynamic programming algorithms from scratch."
    p_c = "Teach me DP for solving algorithmic problems."
    results["paraphrase_a"] = requests.post(f"{AI_URL}/ai/analyze-goal", json={"prompt": p_a}).json()
    results["paraphrase_b"] = requests.post(f"{AI_URL}/ai/analyze-goal", json={"prompt": p_b}).json()
    results["paraphrase_c"] = requests.post(f"{AI_URL}/ai/analyze-goal", json={"prompt": p_c}).json()

    # Test 5: Career vs Topic (Backend Java Developer)
    career_prompt = "I want to become a backend Java developer."
    resp_java = requests.post(f"{BASE_URL}/api/profile/extract-goal", json={"prompt": career_prompt, "applyToProfile": True}, headers=headers)
    results["java_extract"] = resp_java.json()
    results["java_gaps"] = requests.get(f"{BASE_URL}/api/skills/gaps", headers=headers).json()
    results["java_roadmap"] = requests.get(f"{BASE_URL}/api/roadmap", headers=headers).json()
    results["java_projects"] = requests.get(f"{BASE_URL}/api/projects", headers=headers).json()

    # Test 6: Truly Unseen Topic - Origami Design
    origami_prompt = "I want to learn origami design."
    results["origami_extract"] = requests.post(f"{AI_URL}/ai/analyze-goal", json={"prompt": origami_prompt}).json()

    with open("scripts/validation_results.json", "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)

    print("Validation run complete. Results saved to scripts/validation_results.json")

if __name__ == "__main__":
    run_validation()
