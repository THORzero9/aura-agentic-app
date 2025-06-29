import os
import concurrent.futures
from pydantic import BaseModel
import google.generativeai as genai
from tavily import TavilyClient
import json
from appwrite.client import Client as AppwriteSDKClient # Rename to avoid conflict
from appwrite.services.databases import Databases
from appwrite.id import ID

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

class SavePlanRequest(BaseModel):
    userId: str
    planTitle: str
    modules: list[dict] # Use dict to match the Android client's Map<String, Any> conversion

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
        appwrite_project_id = context.env.get("APPWRITE_PROJECT_ID")
        appwrite_api_key = context.env.get("APPWRITE_API_KEY")
        appwrite_database_id = context.env.get("APPWRITE_DATABASE_ID")
        PLANS_COLLECTION_ID = "685da5d700336bdeab10" # This should match your collection ID

        if not all([gemini_api_key, tavily_api_key, appwrite_project_id, appwrite_api_key, appwrite_database_id]):
            raise ValueError("All required API keys and Appwrite configuration are missing from function environment variables.")

        genai.configure(api_key=gemini_api_key)
        model = genai.GenerativeModel('gemini-2.5-flash')
        tavily_client = TavilyClient(api_key=tavily_api_key)

        appwrite_client = AppwriteSDKClient() # Use the renamed client
        (appwrite_client
          .set_endpoint("https://cloud.appwrite.io/v1")
          .set_project(appwrite_project_id)
          .set_key(appwrite_api_key)
        )
        appwrite_databases = Databases(appwrite_client)


        # Parse the request payload from the function trigger
        payload = json.loads(context.req.body)
        context.log(f"Received request payload: {payload}")

        # --- Differentiate Request Types ---
        if "topic" in payload and "hours_per_week" in payload and "preferred_format" in payload:
            # Assume it's a LearningRequest
            request = LearningRequest(**payload)
            context.log(f"Received GENERATE request for topic: {request.topic}")
            
            # --- The Agent Logic (Unchanged for generation) ---
            deconstruction_prompt = f"Break down '{request.topic}' into 4 logical, weekly sub-topics for a beginner. Respond with ONLY the list of 4 sub-topics, separated by newlines, without any prefixes."
            deconstruction_response = model.generate_content(deconstruction_prompt)
            sub_topics = deconstruction_response.text.strip().split('\n')
            context.log(f"LLM generated sub-topics: {sub_topics}")

            tasks = [(request.topic, i + 1, sub_topic, tavily_client, model) for i, sub_topic in enumerate(sub_topics)]
            
            with concurrent.futures.ThreadPoolExecutor() as executor:
                generated_modules = list(executor.map(process_single_sub_topic, tasks))
            
            generated_modules.sort(key=lambda x: x.week)
            
            response_data = LearningPlanResponse(
                plan_title=f"Your Curated Plan for {request.topic}",
                modules=generated_modules
            )

            context.log("Plan generated successfully.")
            return context.res.json(response_data.model_dump())

        elif "planTitle" in payload and "modules" in payload and "userId" in payload:
            # Assume it's a SavePlanRequest
            request = SavePlanRequest(**payload)
            context.log(f"Received SAVE request for user: {request.userId}")
            
            # --- Save Plan Logic (Adapted from main.py) ---
            document_data = {
                "planTitle": request.planTitle,
                "modules": json.dumps(request.modules), # modules are already a list of dicts
                "userId": request.userId
            }
            
            appwrite_databases.create_document(
                database_id=appwrite_database_id,
                collection_id=PLANS_COLLECTION_ID,
                document_id=ID.unique(),
                data=document_data
            )
            context.log("Successfully saved plan to Appwrite Database.")
            return context.res.json({"success": True})

        else:
            context.error("Invalid request payload. Could not determine request type.")
            return context.res.json({
                "success": False,
                "error": "Invalid request payload. Missing required fields for a known action."
            }, status_code=400)

    except Exception as e:
        context.error(f"A top-level error occurred: {e}")
        return context.res.json({
            "success": False,
            "error": str(e)
        }, status_code=500)