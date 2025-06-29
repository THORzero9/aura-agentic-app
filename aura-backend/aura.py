import os
import concurrent.futures
from pydantic import BaseModel
import google.generativeai as genai
from tavily import TavilyClient
import json

# Note: No FastAPI or Uvicorn imports are needed.

# --- Data Models (We still use these for data validation) ---
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


# --- Helper function for our agentic workflow ---
def process_single_sub_topic(args):
    main_topic, week_num, sub_topic, tavily_client, model = args
    print(f"STARTING: Week {week_num} - {sub_topic}")
    try:
        search_query = f"Beginner tutorial for '{sub_topic}' in the context of '{main_topic}'"
        search_results = tavily_client.search(query=search_query, max_results=5).get('results', [])
        if not search_results:
            return LearningModule(week=week_num, topic=sub_topic, resources=[])
        
        context_for_llm = "\n".join([f"- {res['title']}: {res['url']}" for res in search_results])
        curation_prompt = f"""
        You are a helpful learning assistant. From the list of search results about '{sub_topic}', select the 2 best resources for a beginner learning about '{main_topic}'.
        Your response MUST be in a valid JSON format, containing a list named "resources". If you find no good resources, return an empty list.
        Example: {{"resources": [{{"title": "Example Title", "url": "https://example.com", "type": "article"}}]}}
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

        if not resources and search_results:
             fallback_resources = [
                Resource(title=res.get('title', 'No title'), url=res.get('url', ''), type="video" if "youtube.com" in res.get('url', '') else "article")
                for res in search_results[:2]
            ]
             return LearningModule(week=week_num, topic=sub_topic, resources=fallback_resources)

        print(f"COMPLETED: Week {week_num} - Found {len(resources)} resources.")
        return LearningModule(week=week_num, topic=sub_topic, resources=resources)
    except Exception as e:
        print(f"An unexpected error occurred for '{sub_topic}': {e}")
        return LearningModule(week=week_num, topic=sub_topic, resources=[])


# --- Main Appwrite Function Entry Point ---
def main(context):
    """
    This is the function that Appwrite will execute.
    The 'context' object contains the request, response, and environment variables.
    """
    try:
        # --- API Configuration (Inside the function, using context.env) ---
        gemini_api_key = context.env.get("GOOGLE_API_KEY")
        tavily_api_key = context.env.get("TAVILY_API_KEY")
        
        if not all([gemini_api_key, tavily_api_key]):
            raise ValueError("API keys are missing from function environment variables.")

        genai.configure(api_key=gemini_api_key)
        model = genai.GenerativeModel('gemini-2.5-flash')
        tavily_client = TavilyClient(api_key=tavily_api_key)

        # Parse the request payload from the function trigger
        payload = json.loads(context.req.body)
        request = LearningRequest(**payload)
        context.log(f"Received GENERATE request for topic: {request.topic}")
        
        # --- The Agent Logic (Unchanged) ---
        deconstruction_prompt = f"Break down '{request.topic}' into 4 logical, weekly sub-topics..."
        deconstruction_response = model.generate_content(deconstruction_prompt)
        sub_topics = deconstruction_response.text.strip().split('\n')
        
        tasks = [(request.topic, i + 1, sub_topic, tavily_client, model) for i, sub_topic in enumerate(sub_topics)]
        
        with concurrent.futures.ThreadPoolExecutor() as executor:
            generated_modules = list(executor.map(process_single_sub_topic, tasks))
        
        generated_modules.sort(key=lambda x: x.week)
        
        response_data = LearningPlanResponse(
            plan_title=f"Your Curated Plan for {request.topic}",
            modules=generated_modules
        )

        # Return a successful JSON response using the context object
        context.log("Plan generated successfully.")
        return context.res.json(response_data.model_dump())

    except Exception as e:
        context.error(f"A top-level error occurred: {e}")
        return context.res.json({
            "success": False,
            "error": str(e)
        }, status_code=500)