"""
main.py - The core FastAPI backend for the Aura application.

This module defines the API endpoints for generating and saving AI-curated learning plans.
It uses a lazy initialization pattern for external clients (Google Gemini, Tavily, Appwrite)
to ensure robustness and compatibility with serverless environments like Google Cloud Run.
"""

import os
import json
import concurrent.futures
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import google.generativeai as genai
from tavily import TavilyClient
from appwrite.client import Client
from appwrite.services.databases import Databases
from appwrite.id import ID

# --- App Initialization ---
app = FastAPI(title="Aura AI Agent Backend")

# --- CORS Configuration ---
# Allows the web and mobile frontends to securely communicate with this backend.
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://localhost:5173",
        "https://aura-agentic-app.netlify.app",
        "https://*.netlify.app",
        "https://*.vercel.app"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Lazily Initialized Global Clients ---
# These are initialized as None to prevent crashes on startup if env vars are missing.
# They will be populated by the `initialize_external_clients_if_needed` function on the first API call.
model = None
tavily_client = None
appwrite_databases = None
appwrite_database_id = None
PLANS_COLLECTION_ID = "685da5d700336bdeab10"


def initialize_external_clients_if_needed():
    """
    Initializes and configures all external service clients on-demand.

    This function is called by endpoints that require external services. It checks if clients
    have already been initialized and, if not, configures them using environment variables.
    This "lazy loading" pattern is crucial for serverless environments where startup
    time and resilience to configuration errors are critical.

    Raises:
        ValueError: If any required environment variable is missing.
    """
    global model, tavily_client, appwrite_databases, appwrite_database_id
    if model and tavily_client and appwrite_databases:
        return  # Clients are already initialized

    # Check for all required environment variables at once.
    required_vars = {
        "GOOGLE_API_KEY": os.getenv("GOOGLE_API_KEY"),
        "TAVILY_API_KEY": os.getenv("TAVILY_API_KEY"),
        "APPWRITE_PROJECT_ID": os.getenv("APPWRITE_PROJECT_ID"),
        "APPWRITE_API_KEY": os.getenv("APPWRITE_API_KEY"),
        "APPWRITE_DATABASE_ID": os.getenv("APPWRITE_DATABASE_ID"),
    }
    missing_vars = [key for key, value in required_vars.items() if not value]
    if missing_vars:
        raise ValueError(f"Missing required environment variables: {', '.join(missing_vars)}")

    # Configure and initialize clients now that we know keys exist.
    print("Initializing external clients...")
    genai.configure(api_key=required_vars["GOOGLE_API_KEY"])
    model = genai.GenerativeModel('gemini-2.5-flash')
    tavily_client = TavilyClient(api_key=required_vars["TAVILY_API_KEY"])

    appwrite_client = Client()
    (appwrite_client
      .set_endpoint("https://cloud.appwrite.io/v1")
      .set_project(required_vars["APPWRITE_PROJECT_ID"])
      .set_key(required_vars["APPWRITE_API_KEY"])
    )
    appwrite_databases = Databases(appwrite_client)
    appwrite_database_id = required_vars["APPWRITE_DATABASE_ID"]
    print("External clients initialized successfully.")


# --- Pydantic Data Models ---
# These models define the data structures for API requests and responses,
# providing automatic data validation.

class LearningRequest(BaseModel):
    topic: str
    hours_per_week: int
    preferred_format: str
    userId: str

class Resource(BaseModel):
    title: str
    url: str
    type: str

class LearningModule(BaseModel):
    week: int
    topic: str
    resources: list[Resource]

class LearningPlanResponse(BaseModel):
    plan_title: str
    modules: list[LearningModule]

class SavePlanRequest(BaseModel):
    userId: str
    planTitle: str
    modules: list[dict]


# --- Agentic Workflow Helper Function ---
def process_single_sub_topic(args):
    """
    Executes the complete multi-step AI workflow for a single sub-topic.

    This function is designed to be run in parallel for each sub-topic to speed
    up the overall plan generation process.

    Args:
        args (tuple): A tuple containing (main_topic, week_num, sub_topic).

    Returns:
        LearningModule: A Pydantic model containing the curated resources for the week.
    """
    main_topic, week_num, sub_topic = args
    print(f"STARTING: Week {week_num} - {sub_topic}")

    try:
        # Step A: Initial Search - Use the Tavily API to find relevant resources.
        search_query = f"Beginner tutorial for '{sub_topic}' in the context of '{main_topic}'"
        search_results = tavily_client.search(query=search_query, max_results=5).get('results', [])
        if not search_results:
            print(f"Warning: Initial search returned no results for '{sub_topic}'.")
            return LearningModule(week=week_num, topic=sub_topic, resources=[])
        context_for_llm = "\n".join([f"- {res['title']}: {res['url']}" for res in search_results])

        # Step B: AI Sanity Check - Ask the LLM to validate the search results.
        sanity_check_prompt = f"Are these search results relevant for a beginner learning '{sub_topic}' in the context of '{main_topic}'? Answer ONLY with YES or NO.\n\nResults:\n{context_for_llm}"
        sanity_response = model.generate_content(sanity_check_prompt).text.strip()
        print(f" -> Sanity Check for '{sub_topic}': {sanity_response}")

        # Step C: Self-Correction Loop - If results are irrelevant, ask the LLM to generate a better query and re-search.
        if "NO" in sanity_response.upper():
            print(f" -> Self-Correction: Re-phrasing search for '{sub_topic}'...")
            rephrase_prompt = f"Create a better, more specific search query for a beginner learning '{sub_topic}' in the context of '{main_topic}'. Your response must be ONLY the new search query."
            new_search_query = model.generate_content(rephrase_prompt).text.strip()
            print(f" -> New Search Query: \"{new_search_query}\"")
            search_results = tavily_client.search(query=new_search_query, max_results=5).get('results', [])
            context_for_llm = "\n".join([f"- {res['title']}: {res['url']}" for res in search_results])

        # Step D: Final Curation - Ask the LLM to select the best 2 resources and format them as JSON.
        curation_prompt = f"""
        You are a helpful learning assistant. From the following list of search results about '{sub_topic}', select the 2 most relevant and highest quality resources for a beginner learning about '{main_topic}'.
        Your response MUST be in a valid JSON format, containing a list named "resources". If you cannot find two good resources, return an empty list.
        
        Search Results:\n{context_for_llm}
        
        Example JSON output: {{"resources": [{{"title": "Example Title", "url": "https://example.com", "type": "article"}}]}}
        """
        curation_response = model.generate_content(curation_prompt)
        cleaned_json_string = curation_response.text.strip().replace("```json", "").replace("```", "")
        
        resources = []
        if cleaned_json_string:
            try:
                curated_data = json.loads(cleaned_json_string)
                resources = [Resource(**res) for res in curated_data.get('resources', [])]
            except json.JSONDecodeError:
                print(f"Warning: Failed to parse JSON from curation response for '{sub_topic}'.")
        
        print(f"COMPLETED: Week {week_num} - Found {len(resources)} resources.")
        return LearningModule(week=week_num, topic=sub_topic, resources=resources)

    except Exception as e:
        print(f"An unexpected error occurred for '{sub_topic}': {e}")
        return LearningModule(week=week_num, topic=sub_topic, resources=[])


# --- API Endpoints ---
@app.post("/api/generate-plan", response_model=LearningPlanResponse)
def generate_plan(request: LearningRequest):
    """
    Handles the primary request to generate a new learning plan.
    """
    print(f"Received intelligent request for topic: {request.topic}")
    try:
        initialize_external_clients_if_needed()

        deconstruction_prompt = f"Break down '{request.topic}' into 4 logical, weekly sub-topics for a beginner. Respond with ONLY the list of 4 sub-topics, separated by newlines, without any prefixes."
        deconstruction_response = model.generate_content(deconstruction_prompt)
        sub_topics = deconstruction_response.text.strip().split('\n')
        print(f"LLM generated sub-topics: {sub_topics}")

        tasks = [(request.topic, i + 1, sub_topic) for i, sub_topic in enumerate(sub_topics)]
        
        with concurrent.futures.ThreadPoolExecutor() as executor:
            generated_modules = list(executor.map(process_single_sub_topic, tasks))
        
        generated_modules.sort(key=lambda x: x.week)

        return LearningPlanResponse(
            plan_title=f"Your Curated Plan for {request.topic}",
            modules=generated_modules
        )
    except Exception as e:
        print(f"A top-level error occurred during generation: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/save-plan")
def save_plan(request: SavePlanRequest):
    """
    Saves a user's generated learning plan to the Appwrite database.
    """
    print(f"Received SAVE request for user: {request.userId}")
    try:
        initialize_external_clients_if_needed()

        document_data = {
            "planTitle": request.planTitle,
            "modules": json.dumps(request.modules), # Store complex objects as a JSON string
            "userId": request.userId
        }
        
        appwrite_databases.create_document(
            database_id=appwrite_database_id,
            collection_id=PLANS_COLLECTION_ID,
            document_id=ID.unique(),
            data=document_data
        )
        print("Successfully saved plan to Appwrite Database.")
        return {"success": True}
    except Exception as e:
        print(f"DATABASE ERROR: Failed to save plan. {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/")
def read_root():
    return {"status": "Aura Backend is running!"}


# This block is for local development only and is not used by the Docker container.
if __name__ == "__main__":

    uvicorn.run(app, host="0.0.0.0", port=8000)
