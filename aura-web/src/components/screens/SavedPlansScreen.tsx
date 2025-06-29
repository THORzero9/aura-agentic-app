import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { usePlan } from '../../contexts/PlanContext';
import LoadingSpinner from '../common/LoadingSpinner';
import type { LearningPlanResponse } from '../../types';

const SavedPlansScreen: React.FC = () => {
  const navigate = useNavigate();
  const { savedPlans, loadSavedPlans, setCurrentPlan, isLoading } = usePlan();

  useEffect(() => {
    loadSavedPlans();
  }, []);

  const handlePlanClick = (plan: LearningPlanResponse) => {
    setCurrentPlan(plan);
    navigate('/plan');
  };

  const handleBack = () => {
    navigate('/home');
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="min-h-screen bg-app-bg">
      {/* Header */}
      <div className="bg-card-bg shadow-sm border-b border-gray-100">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center space-x-4">
            <button
              onClick={handleBack}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <svg className="w-6 h-6 text-primary-text" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <h1 className="text-xl font-bold text-primary-text">My Saved Plans</h1>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-4xl mx-auto px-4 py-6">
        {savedPlans.length === 0 ? (
          <EmptyState />
        ) : (
          <div className="space-y-4">
            {savedPlans.map((plan, index) => (
              <SavedPlanItem 
                key={index} 
                plan={plan} 
                onClick={() => handlePlanClick(plan)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

const EmptyState: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="text-center py-12">
      <div className="w-24 h-24 bg-accent-blue bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-6">
        <svg className="w-12 h-12 text-accent-blue" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
        </svg>
      </div>
      
      <h2 className="text-xl font-bold text-primary-text mb-2">
        No saved plans yet
      </h2>
      <p className="text-secondary-text mb-8 max-w-md mx-auto">
        Create your first learning plan to see it here. Your saved plans will help you track your learning journey.
      </p>
      
      <button
        onClick={() => navigate('/home')}
        className="btn-primary"
      >
        Create Your First Plan
      </button>
    </div>
  );
};

interface SavedPlanItemProps {
  plan: LearningPlanResponse;
  onClick: () => void;
}

const SavedPlanItem: React.FC<SavedPlanItemProps> = ({ plan, onClick }) => {
  return (
    <div 
      onClick={onClick}
      className="card hover:shadow-md transition-shadow cursor-pointer group"
    >
      <div className="flex items-center justify-between">
        <div className="flex-1">
          <h3 className="text-lg font-bold text-primary-text group-hover:text-accent-blue transition-colors mb-2">
            {plan.plan_title}
          </h3>
          <div className="flex items-center space-x-4 text-sm text-secondary-text">
            <div className="flex items-center space-x-1">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{plan.modules.length} weeks</span>
            </div>
            <div className="flex items-center space-x-1">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
              </svg>
              <span>
                {plan.modules.reduce((total, module) => total + module.resources.length, 0)} resources
              </span>
            </div>
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          <div className="w-10 h-10 bg-accent-blue bg-opacity-20 rounded-full flex items-center justify-center">
            <svg className="w-5 h-5 text-accent-blue" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SavedPlansScreen; 