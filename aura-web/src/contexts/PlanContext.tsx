import React, { createContext, useContext, useState } from 'react';
import { apiService } from '../lib/api';
import { databases, DATABASE_ID, PLANS_COLLECTION_ID } from '../lib/appwrite';
import { Query } from 'appwrite';
import { useAuth } from './AuthContext';
import type { LearningPlanResponse, LearningRequest, PlanState, LearningModule } from '../types';

interface PlanContextType extends PlanState {
  generatePlan: (request: LearningRequest) => Promise<LearningPlanResponse | null>;
  savePlan: (plan: LearningPlanResponse) => Promise<boolean>;
  loadSavedPlans: () => Promise<void>;
  setCurrentPlan: (plan: LearningPlanResponse | null) => void;
}

const PlanContext = createContext<PlanContextType | undefined>(undefined);

export const usePlan = () => {
  const context = useContext(PlanContext);
  if (context === undefined) {
    throw new Error('usePlan must be used within a PlanProvider');
  }
  return context;
};

interface PlanProviderProps {
  children: React.ReactNode;
}

export const PlanProvider: React.FC<PlanProviderProps> = ({ children }) => {
  const { user } = useAuth();
  const [currentPlan, setCurrentPlanState] = useState<LearningPlanResponse | null>(null);
  const [savedPlans, setSavedPlans] = useState<LearningPlanResponse[]>([]);
  const [isSaving, setIsSaving] = useState(false);
  const [isSaved, setIsSaved] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Generate learning plan (matching Android PlanRepository.generateLearningPlan)
  const generatePlan = async (request: LearningRequest): Promise<LearningPlanResponse | null> => {
    try {
      setIsLoading(true);
      const plan = await apiService.generatePlan(request);
      setCurrentPlanState(plan);
      setIsSaved(false); // Reset saved state for new plan
      return plan;
    } catch (error) {
      console.error('Failed to generate plan:', error);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  // Save plan (matching Android PlanRepository.savePlan)
  const savePlan = async (plan: LearningPlanResponse): Promise<boolean> => {
    if (!user) {
      console.error('No user logged in');
      return false;
    }

    try {
      setIsSaving(true);

      // Prepare modules data for saving (matching Android format)
      const modulesData = plan.modules.map(module => ({
        week: module.week,
        topic: module.topic,
        resources: module.resources.map(res => ({
          title: res.title,
          url: res.url,
          type: res.type
        }))
      }));

      const saveRequest = {
        userId: user.$id,
        planTitle: plan.plan_title,
        modules: modulesData
      };

      const response = await apiService.savePlan(saveRequest);
      
      if (response.success) {
        setIsSaved(true);
        // Add to saved plans list if not already there
        setSavedPlans(prev => {
          const exists = prev.some(p => p.plan_title === plan.plan_title);
          return exists ? prev : [...prev, plan];
        });
        return true;
      } else {
        console.error('Save failed:', response.error);
        return false;
      }
    } catch (error) {
      console.error('Failed to save plan:', error);
      return false;
    } finally {
      setIsSaving(false);
    }
  };

  // Load saved plans (matching Android PlanRepository.getSavedPlans)
  const loadSavedPlans = async () => {
    if (!user) return;

    try {
      setIsLoading(true);
      
      const response = await databases.listDocuments(
        DATABASE_ID,
        PLANS_COLLECTION_ID,
        [Query.equal('userId', user.$id)]
      );

      const plans: LearningPlanResponse[] = response.documents.map(doc => {
        const modulesJson = doc.modules as string;
        const modules: LearningModule[] = JSON.parse(modulesJson);

        return {
          plan_title: doc.planTitle as string,
          modules: modules
        };
      });

      setSavedPlans(plans);
    } catch (error) {
      console.error('Failed to load saved plans:', error);
      setSavedPlans([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Set current plan (matching Android SharedViewModel.setPlan)
  const setCurrentPlan = (plan: LearningPlanResponse | null) => {
    setCurrentPlanState(plan);
    if (plan) {
      // Check if this plan is already saved
      const isAlreadySaved = savedPlans.some(p => p.plan_title === plan.plan_title);
      setIsSaved(isAlreadySaved);
    }
  };

  const value: PlanContextType = {
    currentPlan,
    savedPlans,
    isSaving,
    isSaved,
    isLoading,
    generatePlan,
    savePlan,
    loadSavedPlans,
    setCurrentPlan,
  };

  return <PlanContext.Provider value={value}>{children}</PlanContext.Provider>;
}; 