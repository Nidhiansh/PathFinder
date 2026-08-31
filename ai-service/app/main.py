from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import router as ai_router

app = FastAPI(
    title="AI-Powered Personalized Learning Path Recommender - AI Service",
    description="Intelligent AI/ML Engine for Skill Gap Analysis, DAG Prerequisite Solving, Multi-Factor Scoring, and Dynamic Roadmap Adaptation.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(ai_router)

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "learning-path-ai-service",
        "engine": "active"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
