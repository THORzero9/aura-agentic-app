# Aura - Your Personal AI Learning Guide

<a href="https://ibb.co/8nF638jk"><img src="https://i.ibb.co/8nF638jk/Aura-App-Logo.png" alt="Aura-App-Logo" border="0"></a> <br>
**Aura is an intelligent, full-stack mobile application designed to combat information overload and create personalized, high-quality learning paths for any topic.**

## The Problem

In today's world, learning a new skill is overwhelming. You are faced with a sea of random YouTube videos, blog posts, and expensive courses, with no clear path from beginner to expert. It's difficult to know where to start and what to learn next, leading to frustration and abandoned goals.

## The Solution

Aura acts as your personal AI mentor. Simply tell the app what you want to learn, and our advanced, multi-step AI agent gets to work. It deconstructs the topic, searches for the best free resources on the web, and curates them into a logical, week-by-week curriculum tailored just for you. With Aura, you get a clear, structured path to mastery.

---

## 🚀 Key Features

* **Intelligent Path Generation:** Our AI agent understands your learning goals and creates a structured, easy-to-follow plan.
* **AI-Powered Curation:** We don't just search; our agent analyzes and selects the best, most relevant resources from around the web, using a two-step "Search and Curate" workflow.
* **Secure User Authentication:** Full email/password and Google Sign-In support, powered by **Appwrite**. Includes a robust OTP email verification flow.
* **Save & View Plans:** Users can save their favorite generated plans to their personal account and view them anytime.
* **Polished Native Experience:** A beautiful, responsive, and user-friendly native Android application built with the latest Jetpack Compose standards.
* **(Bonus) Web Frontend:** A fully functional web app built with React, deployed on Netlify, for universal accessibility.

---

## 🛠️ Tech Stack & Architecture

Aura is a full-stack application built with a modern, decoupled architecture.

### **Frontend (Android)**

* **Language:** Kotlin
* **UI:** Jetpack Compose
* **Architecture:** MVVM (Model-View-ViewModel) with a Repository Pattern
* **Networking:** Retrofit
* **Dependencies:** Hilt (for Dependency Injection), Lottie (for animations), Jetpack Navigation

### **Frontend (Web)**

* **Framework:** React
* **Deployment:** Netlify

### **Backend (The Agent)**

* **Framework:** Python with FastAPI
* **Deployment:** Google Cloud Run for scalability and management.
* **AI Orchestration:**
    * **Google Generative AI (Gemini 2.5 Flash):** Used for all reasoning tasks, including deconstructing topics and curating resources.
    * **Tavily Search API (Sponsor Tech):** Used for all web searches to find high-quality, relevant learning materials.

### **Backend-as-a-Service (BaaS)**

* **Appwrite (Sponsor Tech):**
    * **Appwrite Auth:** Manages all user accounts, including Email/Password, Google Sign-In, and OTP verification.
    * **Appwrite Databases:** Provides the persistent cloud database for storing all user-saved learning plans.

---

## ⚙️ Setup and Installation

### **Prerequisites**

* Android Studio (latest version)
* Python 3.10+
* An active `.env` file in the `aura-backend` directory with valid API keys for:
    * `GOOGLE_API_KEY`
    * `TAVILY_API_KEY`
    * `APPWRITE_PROJECT_ID`
    * `APPWRITE_API_KEY`
    * `APPWRITE_DATABASE_ID`

### **Running the App**

1.  **Backend:**
    ```bash
    cd aura-backend
    python3 -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
    python3 main.py
    ```

2.  **Android App:**
    * Open the `aura-android` folder in Android Studio.
    * Update the `BASE_URL` in `ApiClient.kt` to point to your local machine's IP address.
    * Build and run the app on a physical device or emulator.

---

## 🎬 Demo Video

https://youtu.be/qxn4Sp8i0as?si=Xiy0qMDLqaH4YPQ6
