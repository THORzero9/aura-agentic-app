# Aura Web App

A React web application that provides the same functionality as the Aura Android app - an AI-powered learning plan generator.

## Features

- 🔐 **Authentication**: Email/password login, signup with OTP verification, Google OAuth
- 🎯 **Learning Plan Generation**: AI-powered personalized learning plans based on topic, hours, and learning style
- 💾 **Plan Management**: Save and retrieve learning plans
- 📱 **Responsive Design**: Works on desktop, tablet, and mobile browsers
- 🎨 **Modern UI**: Clean interface matching the Android app design

## Tech Stack

- **Frontend**: React 18 + TypeScript + Vite
- **Styling**: Tailwind CSS
- **Authentication & Database**: Appwrite (same backend as Android app)
- **API**: FastAPI backend (shared with Android app)
- **Routing**: React Router DOM
- **State Management**: React Context API

## Quick Start

1. **Install dependencies**:
   ```bash
   npm install
   ```

2. **Start development server**:
   ```bash
   npm run dev
   ```

3. **Build for production**:
   ```bash
   npm run build
   ```

## Configuration

The app is configured to use the same backend services as the Android app:

- **Appwrite Project**: `aura-agentic-app`
- **Backend API**: `https://aura-agentic-app-646612637107.asia-south1.run.app`
- **Database**: Same Appwrite database and collections as Android

No additional configuration needed - it works out of the box!

## Deployment

### Option 1: Netlify (Recommended for Hackathon)

1. Build the project: `npm run build`
2. Drag and drop the `dist` folder to Netlify
3. Your app will be live at a Netlify URL

### Option 2: Vercel

1. Connect your GitHub repository to Vercel
2. Set build command: `npm run build`
3. Set output directory: `dist`

### Option 3: Firebase Hosting

1. `npm install -g firebase-tools`
2. `firebase login`
3. `firebase init hosting`
4. Select `dist` as public directory
5. `firebase deploy`

## Project Structure

```
src/
├── components/
│   ├── common/          # Reusable components
│   └── screens/         # Main screen components
├── contexts/            # React Context providers
├── lib/                 # API clients and utilities
├── types/               # TypeScript type definitions
└── App.tsx             # Main app component
```

## Key Features Implementation

### Authentication Flow
- Login → Home (if successful)
- Signup → OTP → Home (if verified)
- Protected routes redirect to login if not authenticated

### Learning Plan Generation
1. User inputs topic, hours/week, learning style
2. API call to FastAPI backend with AI agents
3. Display generated plan with save functionality
4. View saved plans from Appwrite database

### Mobile-First Design
- Responsive layout that works on all screen sizes
- Touch-friendly interface
- PWA-ready (can be installed on mobile)

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Development Notes

- All API endpoints match the Android app exactly
- TypeScript types mirror Android data models
- Same authentication flow and user management
- Shared backend infrastructure

## License

Same as the Android app - this is the web version of the Aura learning platform.
