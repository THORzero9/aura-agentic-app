# 🚀 Deploy Backend via Google Cloud Console

## ✅ Step-by-Step Instructions

### Step 1: Create Deployment ZIP
Create a ZIP file containing these files:
- `main.py` (✅ with CORS fix)
- `requirements.txt`
- `Dockerfile`
- `aura.py` (if needed)

**DO NOT include**: `.env`, `venv/`, `.venv/`, or `aura-backend.zip`

### Step 2: Google Cloud Console Deployment

1. **Go to**: [Google Cloud Console](https://console.cloud.google.com)

2. **Navigate to**: Cloud Run (search for "Cloud Run" in the top search bar)

3. **Find your existing service**: 
   - Look for a service name like `aura-backend` or similar
   - It should show your current URL: `aura-agentic-app-646612637107.asia-south1.run.app`

4. **Click on your service name** to open service details

5. **Click**: "EDIT & DEPLOY NEW REVISION" (blue button at top)

6. **Container tab**:
   - **Source Type**: Select "Source Repository" or "Upload"
   - **Upload your ZIP file** with the backend code
   - **Container Port**: `8080` (should be pre-filled)

7. **Variables & Secrets tab**:
   - Make sure your environment variables are set:
     - `GOOGLE_API_KEY`
     - `TAVILY_API_KEY` 
     - `APPWRITE_PROJECT_ID`
     - `APPWRITE_API_KEY`
     - `APPWRITE_DATABASE_ID`

8. **Click**: "DEPLOY" (blue button at bottom)

9. **Wait**: 2-3 minutes for deployment to complete

### Step 3: Test the Fix

1. **Copy the service URL** (should be the same as before)
2. **Test**: Visit `your-url/` - should show: `{"status": "Aura Backend is running!"}`
3. **Test your web app**: Try "Create my plan" - CORS error should be gone!

## 🎯 What This Fixes

✅ **CORS Policy**: Your Netlify domain is now allowed  
✅ **API Access**: Web app can call `/api/generate-plan`  
✅ **Plan Generation**: "Create my plan" will work  
✅ **Plan Saving**: Save functionality will work  

## 🚨 Important Notes

- **Same URL**: Your API URL stays the same
- **Environment Variables**: Make sure they're preserved in Cloud Run
- **No downtime**: Rolling deployment keeps service running
- **Instant fix**: CORS will work immediately after deployment

## ❓ Need Help?

If deployment fails:
1. Check the "Logs" tab in Cloud Run
2. Verify environment variables are set
3. Ensure ZIP contains the right files 