import os
import concurrent.futures
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
import google.generativeai as genai
#from dotenv import load_dotenv
from tavily import TavilyClient
import json
import time
from appwrite.client import Client
from appwrite.services.databases import Databases
from appwrite.id import ID

# --- App Initialization & API Configuration ---
app = FastAPI()

# --- CORS Configuration ---
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

#load_dotenv()

# Configure Gemini
gemini_api_key = os.getenv("GOOGLE_API_KEY")
if not gemini_api_key:
    raise ValueError("GOOGLE_API_KEY not found in environment variables")
genai.configure(api_key=gemini_api_key)
model = genai.GenerativeModel('gemini-2.5-flash')

# Configure Tavily
tavily_api_key = os.getenv("TAVILY_API_KEY")
if not tavily_api_key:
    raise ValueError("TAVILY_API_KEY not found in .env file")
tavily_client = TavilyClient(api_key=tavily_api_key)

# --- NEW: Configure Appwrite Client ---
appwrite_client = Client()
appwrite_project_id = os.getenv("APPWRITE_PROJECT_ID")
appwrite_api_key = os.getenv("APPWRITE_API_KEY")
appwrite_database_id = os.getenv("APPWRITE_DATABASE_ID")

if not all([appwrite_project_id, appwrite_api_key, appwrite_database_id]):
    raise ValueError("Appwrite configuration missing from environment variables")

(appwrite_client
  .set_endpoint("https://cloud.appwrite.io/v1")
  .set_project(appwrite_project_id)
  .set_key(appwrite_api_key)
)
appwrite_databases = Databases(appwrite_client)
PLANS_COLLECTION_ID = "685da5d700336bdeab10" # The ID we gave our collection


# --- Data Models ---
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

# --- Helper function for our advanced agentic workflow ---
def process_single_sub_topic(args):
    """
    This function represents the complete workflow for one sub-topic.
    It searches for resources and then uses an LLM to curate the best ones.
    """
    main_topic, week_num, sub_topic = args
    print(f"STARTING: Week {week_num} - {sub_topic}")
    
    try:
        # Step A: Initial Search
        search_query = f"Beginner tutorial for '{sub_topic}' in the context of '{main_topic}'"
        print(f"  -> Initial Search Query: \"{search_query}\"")
        search_results = tavily_client.search(query=search_query, max_results=5).get('results', [])

        if not search_results:
            print(f"Warning: Initial Tavily search returned no results for '{sub_topic}'.")
            return LearningModule(week=week_num, topic=sub_topic, resources=[])
        
        context_for_llm = "\n".join([f"- {res['title']}: {res['url']}" for res in search_results])

        # Step B: AI Sanity Check
        sanity_check_prompt = f"Are these search results relevant for a beginner learning '{sub_topic}' in the context of '{main_topic}'? Answer ONLY with YES or NO.\n\nResults:\n{context_for_llm}"
        sanity_response = model.generate_content(sanity_check_prompt).text.strip()
        print(f"  -> Sanity Check for '{sub_topic}': {sanity_response}")

        # Step C: Self-Correction Loop
        if "NO" in sanity_response.upper():
            print(f"  -> Self-Correction: Re-phrasing search for '{sub_topic}'...")
            rephrase_prompt = f"Create a better, more specific search query for a beginner learning '{sub_topic}' in the context of '{main_topic}'. Your response must be ONLY the new search query."
            new_search_query = model.generate_content(rephrase_prompt).text.strip()
            print(f"  -> New Search Query: \"{new_search_query}\"")
            search_results = tavily_client.search(query=new_search_query, max_results=5).get('results', [])
            context_for_llm = "\n".join([f"- {res['title']}: {res['url']}" for res in search_results])


        # Step D: Final Curation
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
        
        # Fallback to raw results if curation fails or returns empty
        if not resources and search_results:
            print(f"Warning: Curation failed. Using top raw results for '{sub_topic}' as fallback.")
            resources = [
                Resource(
                    title=res.get('title', 'No title'),
                    url=res.get('url', ''),
                    type="video" if "youtube.com" in res.get('url', '') else "article"
                ) for res in search_results[:2]
            ]
            
        print(f"COMPLETED: Week {week_num} - Found {len(resources)} resources.")
        return LearningModule(week=week_num, topic=sub_topic, resources=resources)

    except Exception as e:
        print(f"An unexpected error occurred for '{sub_topic}': {e}")
        return LearningModule(week=week_num, topic=sub_topic, resources=[])

# --- API Endpoint ---
@app.post("/api/generate-plan", response_model=LearningPlanResponse)
def generate_plan(request: LearningRequest):
    print(f"Received intelligent request for topic: {request.topic}")
    try:
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
        return LearningPlanResponse(plan_title="Error Generating Plan", modules=[]
        )
@app.post("/api/save-plan")
def save_plan(request: SavePlanRequest):
    
    print(f"Received SAVE request for user: {request.userId}")
    try:
        # THE FIX: We use `request.modules` which comes directly from the app.
        # The `response_data` variable does not exist here.
        document_data = {
            "planTitle": request.planTitle,
            "modules": json.dumps(request.modules),
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
    except Exception as db_error:
        print(f"DATABASE ERROR: Failed to save plan. {db_error}")
        return {"success": False, "error": str(db_error)}

@app.get("/")
def read_root():
    return {"status": "Aura Backend is running!"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)