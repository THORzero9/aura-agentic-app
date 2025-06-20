# main.py in aura-backend/

from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn

# Initialize the FastAPI app
app = FastAPI()

# --- Data Models for the API ---
# This defines what the Android app needs to SEND to us
class LearningRequest(BaseModel):
    topic: str
    hours_per_week: int
    preferred_format: str

# This defines a single resource link
class Resource(BaseModel):
    title: str
    url: str
    type: str # "video" or "article"

# This defines a weekly module
class LearningModule(BaseModel):
    week: int
    topic: str
    resources: list[Resource]

# This defines the final plan that we SEND BACK to the Android app
class LearningPlanResponse(BaseModel):
    plan_title: str
    modules: list[LearningModule]


# --- API Endpoint ---
@app.post("/api/generate-plan", response_model=LearningPlanResponse)
def generate_plan(request: LearningRequest):
    """
    This is our main endpoint. For V0, it ignores the request
    and returns a hardcoded, dummy learning plan.
    """
    print(f"Received request for topic: {request.topic}")

    # Create a fake plan to send back
    dummy_plan = LearningPlanResponse(
        plan_title=f"Your Custom Plan for {request.topic}",
        modules=[
            LearningModule(
                week=1,
                topic="Introduction to the Basics",
                resources=[
                    Resource(title="[Video] A 15-Minute Overview", url="https://youtube.com/example1", type="video"),
                    Resource(title="[Article] Core Concepts Explained", url="https://medium.com/example1", type="article")
                ]
            ),
            LearningModule(
                week=2,
                topic="Diving Deeper",
                resources=[
                    Resource(title="[Video] Advanced Tutorial", url="https://youtube.com/example2", type="video")
                ]
            )
        ]
    )

    return dummy_plan

# A simple root endpoint to check if the server is running
@app.get("/")
def read_root():
    return {"status": "Aura Backend is running!"}

# This allows running the script directly
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)