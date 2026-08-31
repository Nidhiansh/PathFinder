-- ==========================================================
-- PostgreSQL Database Schema for Learning Path Recommender
-- ==========================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS learner_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(100),
    target_role VARCHAR(100),
    career_goal TEXT,
    experience_level VARCHAR(30) DEFAULT 'BEGINNER',
    weekly_hours INT DEFAULT 10,
    preferred_style VARCHAR(30) DEFAULT 'PRACTICAL',
    preferred_resource_types VARCHAR(255) DEFAULT 'COURSE,PROJECT,DOCUMENTATION',
    interests TEXT,
    streak_days INT DEFAULT 1,
    total_hours_spent DOUBLE PRECISION DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    difficulty_level VARCHAR(20) DEFAULT 'INTERMEDIATE'
);

CREATE TABLE IF NOT EXISTS user_skills (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES learner_profiles(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    proficiency_level INT NOT NULL DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    source VARCHAR(30) DEFAULT 'USER_PROVIDED',
    last_assessed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS skill_prerequisites (
    id BIGSERIAL PRIMARY KEY,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    prerequisite_skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    strength VARCHAR(20) DEFAULT 'REQUIRED'
);

CREATE TABLE IF NOT EXISTS learning_resources (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    resource_type VARCHAR(30) NOT NULL,
    url VARCHAR(500) NOT NULL,
    platform VARCHAR(100),
    difficulty VARCHAR(20) NOT NULL,
    estimated_hours DOUBLE PRECISION NOT NULL,
    rating DOUBLE PRECISION DEFAULT 4.5,
    quality_score DOUBLE PRECISION DEFAULT 0.9
);

CREATE TABLE IF NOT EXISTS resource_skills (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES learning_resources(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    coverage_weight DOUBLE PRECISION DEFAULT 1.0
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20) NOT NULL,
    estimated_hours DOUBLE PRECISION,
    deliverables TEXT,
    rubric TEXT,
    primary_skill_id BIGINT REFERENCES skills(id),
    github_template_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS assessments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    skill_id BIGINT NOT NULL REFERENCES skills(id),
    difficulty VARCHAR(20) NOT NULL,
    passing_score INT DEFAULT 70,
    time_limit_minutes INT DEFAULT 15
);

CREATE TABLE IF NOT EXISTS assessment_questions (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    options_json TEXT NOT NULL,
    correct_answer_index INT NOT NULL,
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS assessment_submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    score_percentage INT NOT NULL,
    passed BOOLEAN NOT NULL,
    adaptive_action_taken TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS learning_paths (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    target_role VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    total_estimated_hours DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS learning_phases (
    id BIGSERIAL PRIMARY KEY,
    learning_path_id BIGINT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE,
    phase_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'LOCKED',
    estimated_hours DOUBLE PRECISION DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS learning_path_items (
    id BIGSERIAL PRIMARY KEY,
    learning_phase_id BIGINT NOT NULL REFERENCES learning_phases(id) ON DELETE CASCADE,
    item_type VARCHAR(20) NOT NULL,
    resource_id BIGINT REFERENCES learning_resources(id),
    project_id BIGINT REFERENCES projects(id),
    assessment_id BIGINT REFERENCES assessments(id),
    title VARCHAR(255) NOT NULL,
    url VARCHAR(500),
    estimated_hours DOUBLE PRECISION DEFAULT 0.0,
    order_index INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'LOCKED',
    recommendation_score DOUBLE PRECISION,
    recommendation_reason TEXT,
    completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resource_id BIGINT REFERENCES learning_resources(id),
    project_id BIGINT REFERENCES projects(id),
    score DOUBLE PRECISION NOT NULL,
    match_factors_json TEXT,
    explanation TEXT NOT NULL,
    is_accepted BOOLEAN DEFAULT FALSE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recommendation_feedback (
    id BIGSERIAL PRIMARY KEY,
    recommendation_id BIGINT NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INT NOT NULL,
    feedback_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
