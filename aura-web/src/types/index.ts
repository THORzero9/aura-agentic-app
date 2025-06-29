// Authentication types
export interface User {
  $id: string;
  name: string;
  email: string;
  emailVerification: boolean;
}

export interface AuthState {
  user: User | null;
  isLoading: boolean;
  error: string | null;
}

// Learning plan types - matching Android NetworkModels
export interface LearningRequest {
  topic: string;
  hours_per_week: number;
  preferred_format: string;
  userId: string;
}

export interface Resource {
  title: string;
  url: string;
  type: string;
}

export interface LearningModule {
  week: number;
  topic: string;
  resources: Resource[];
}

export interface LearningPlanResponse {
  plan_title: string;
  modules: LearningModule[];
}

export interface SavePlanRequest {
  userId: string;
  planTitle: string;
  modules: any[];
}

export interface SavePlanResponse {
  success: boolean;
  error?: string;
}

// UI State types
export interface HomeState {
  topic: string;
  hours: number;
  selectedStyle: string;
  isLoading: boolean;
}

export interface PlanState {
  currentPlan: LearningPlanResponse | null;
  savedPlans: LearningPlanResponse[];
  isSaving: boolean;
  isSaved: boolean;
  isLoading: boolean;
} 