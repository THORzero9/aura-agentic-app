# 🚀 Quick Deployment Guide for Hackathon

## Option 1: Netlify (Fastest - 2 minutes)

1. **Build the project**:
   ```bash
   npm run build
   ```

2. **Go to [netlify.com](https://www.netlify.com)**

3. **Drag and drop** the `dist/` folder to the deployment area

4. **Done!** Your app will be live at a `.netlify.app` URL

## Option 2: Vercel (GitHub Integration)

1. **Push to GitHub** (if not already done)

2. **Go to [vercel.com](https://vercel.com)**

3. **Import your GitHub repository**

4. **Deploy settings**:
   - Build Command: `npm run build`
   - Output Directory: `dist`

5. **Deploy** - Vercel will give you a live URL

## Option 3: Firebase Hosting

```bash
npm install -g firebase-tools
firebase login
firebase init hosting
# Select 'dist' as public directory
# Configure as single-page app: Yes
firebase deploy
```

## Testing Before Deployment

```bash
npm run dev
# Test at http://localhost:5173
```

## What Judges Will See

✅ **Professional login screen** with Google OAuth
✅ **AI-powered learning plan generation**
✅ **Plan saving and management**
✅ **Responsive design** (works on mobile/desktop)
✅ **No installation required** - just visit the URL!

## Backend Status

✅ **Already configured** - uses same backend as Android app
✅ **Database ready** - same Appwrite database
✅ **API working** - same FastAPI backend

**The web app is production-ready!** 🎉 