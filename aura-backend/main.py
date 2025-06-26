import os
import concurrent.futures
from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
import google.generativeai as genai
from dotenv import load_dotenv
from tavily import TavilyClient
import json
import time

# --- App Initialization & API Configuration ---
app = FastAPI()
load_dotenv()

gemini_api_key = os.getenv("GOOGLE_API_KEY")
if not gemini_api_key:
    raise ValueError("GOOGLE_API_KEY not found in .env file")
genai.configure(api_key=gemini_api_key)
model = genai.GenerativeModel('gemini-2.5-flash')

tavily_api_key = os.getenv("TAVILY_API_KEY")
if not tavily_api_key:
    raise ValueError("TAVILY_API_KEY not found in .env file")
tavily_client = TavilyClient(api_key=tavily_api_key)


# --- Data Models (Unchanged) ---
class LearningRequest(BaseModel):
    topic: str
    hours_per_week: int
    preferred_format: str

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

# --- Helper function for our parallel agentic workflow ---
# THIS FUNCTION IS THE MAIN CHANGE
def process_single_sub_topic(args):
    """
    This function represents the complete workflow for one sub-topic.
    It now accepts the main topic for better context.
    """
    main_topic, week_num, sub_topic = args
    print(f"STARTING: Week {week_num} - {sub_topic}")
    
    for attempt in range(2):
        try:
            # Step A: Search with more context
            # NEW: We add the main_topic to the search query
            search_query = f"Beginner tutorial for '{sub_topic}' in the context of '{main_topic}'"
            print(f"  -> Tavily Search Query: \"{search_query}\"")
            search_results = tavily_client.search(query=search_query, max_results=5, include_raw_content=False)
            context_for_llm = "\n".join([f"- {res['title']}: {res['url']}" for res in search_results.get('results', [])])

            if not context_for_llm:
                print(f"Warning: Tavily returned no results for '{sub_topic}'.")
                return LearningModule(week=week_num, topic=sub_topic, resources=[])

            # Step B: Use Gemini to curate with more context
            # NEW: We tell the LLM to focus on the main_topic
            curation_prompt = f"""
            You are a helpful learning assistant. A student is learning about '{main_topic}'.
            Your task is to select the 2 best learning resources for a beginner from the list below on the specific sub-topic of '{sub_topic}'.
            Prioritize results that are directly related to '{main_topic}'.

            Search Results:
            {context_for_llm}

            Your response MUST be in a valid JSON format, containing a list named "resources" with exactly two items. If you cannot find two good resources, return an empty list.
            Example JSON output: {{"resources": [{{"title": "Example Title", "url": "https://example.com", "type": "article"}}]}}
            """
            
            curation_response = model.generate_content(curation_prompt)
            cleaned_json_string = curation_response.text.strip().replace("```json", "").replace("```", "")
            
            if cleaned_json_string:
                curated_data = json.loads(cleaned_json_string)
                resources = [Resource(**res) for res in curated_data.get('resources', [])]
                print(f"COMPLETED: Week {week_num} - {sub_topic}")
                return LearningModule(week=week_num, topic=sub_topic, resources=resources)
            else:
                raise ValueError("LLM returned an empty response.")

        except Exception as e:
            print(f"Attempt {attempt + 1} failed for '{sub_topic}': {e}")
            if attempt == 1:
                return LearningModule(week=week_num, topic=sub_topic, resources=[])
            time.sleep(1)

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

        # NEW: We now pass the main topic into our worker function
        tasks = [(request.topic, i + 1, sub_topic) for i, sub_topic in enumerate(sub_topics)]
        
        generated_modules = []
        with concurrent.futures.ThreadPoolExecutor() as executor:
            results = executor.map(process_single_sub_topic, tasks)
            generated_modules = list(results)
        
        generated_modules.sort(key=lambda x: x.week)

        return LearningPlanResponse(
            plan_title=f"Your Curated Plan for {request.topic}",
            modules=generated_modules
        )
    except Exception as e:
        print(f"A top-level error occurred: {e}")
        return LearningPlanResponse(
            plan_title=f"Error generating plan for {request.topic}",
            modules=[]
        )

# ... (rest of the file is the same) ...
@app.get("/")
def read_root():
    return {"status": "Aura Backend is running!"}
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)