import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { usePlan } from '../../contexts/PlanContext';
import type { LearningModule, Resource } from '../../types';

const PlanScreen: React.FC = () => {
  const navigate = useNavigate();
  const { currentPlan, savePlan, isSaving, isSaved } = usePlan();
  const [showSaveSuccess, setShowSaveSuccess] = useState(false);

  useEffect(() => {
    if (!currentPlan) {
      navigate('/home');
    }
  }, [currentPlan, navigate]);

  const handleSavePlan = async () => {
    if (!currentPlan) return;
    
    const success = await savePlan(currentPlan);
    if (success) {
      setShowSaveSuccess(true);
      setTimeout(() => setShowSaveSuccess(false), 3000);
    }
  };

  const handleResourceClick = (url: string) => {
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  if (!currentPlan) {
    return null;
  }

  return (
    <div className="min-h-screen bg-app-bg">
      {/* Success Toast */}
      {showSaveSuccess && (
        <div className="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg z-50">
          <div className="flex items-center space-x-2">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            <span>Plan saved successfully!</span>
          </div>
        </div>
      )}

      {/* Header */}
      <div className="bg-card-bg shadow-sm border-b border-gray-100">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate('/home')}
                className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <svg className="w-6 h-6 text-primary-text" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <h1 className="text-xl font-bold text-primary-text">{currentPlan.plan_title}</h1>
            </div>
            
            <button
              onClick={handleSavePlan}
              disabled={isSaving || isSaved}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg transition-all ${
                isSaved 
                  ? 'bg-green-100 text-green-700 cursor-default'
                  : 'bg-accent-blue text-white hover:bg-opacity-90 disabled:opacity-50'
              }`}
            >
              {isSaving ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  <span>Saving...</span>
                </>
              ) : isSaved ? (
                <>
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
                  </svg>
                  <span>Saved</span>
                </>
              ) : (
                <>
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                  </svg>
                  <span>Save Plan</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Plan Content */}
      <div className="max-w-4xl mx-auto px-4 py-6">
        <div className="space-y-6">
          {currentPlan.modules.map((module) => (
            <ModuleCard key={module.week} module={module} onResourceClick={handleResourceClick} />
          ))}
        </div>
      </div>
    </div>
  );
};

interface ModuleCardProps {
  module: LearningModule;
  onResourceClick: (url: string) => void;
}

const ModuleCard: React.FC<ModuleCardProps> = ({ module, onResourceClick }) => {
  return (
    <div className="card">
      <h2 className="text-xl font-bold text-primary-text mb-4">
        Week {module.week}: {module.topic}
      </h2>
      
      <div className="space-y-3">
        {module.resources.map((resource, index) => (
          <ResourceItem 
            key={index} 
            resource={resource} 
            onClick={() => onResourceClick(resource.url)}
          />
        ))}
      </div>
    </div>
  );
};

interface ResourceItemProps {
  resource: Resource;
  onClick: () => void;
}

const ResourceItem: React.FC<ResourceItemProps> = ({ resource, onClick }) => {
  const getResourceIcon = (type: string) => {
    switch (type.toLowerCase()) {
      case 'video':
        return (
          <svg className="w-6 h-6 text-accent-blue" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h8m-4-7V3m0 6v4m0 4v3" />
            <circle cx="12" cy="12" r="3" fill="currentColor" />
          </svg>
        );
      default:
        return (
          <svg className="w-6 h-6 text-accent-blue" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        );
    }
  };

  return (
    <div 
      onClick={onClick}
      className="flex items-center space-x-3 p-3 hover:bg-gray-50 rounded-lg cursor-pointer transition-colors group"
    >
      {getResourceIcon(resource.type)}
      <div className="flex-1">
        <h3 className="text-primary-text font-medium group-hover:text-accent-blue transition-colors">
          {resource.title}
        </h3>
        <p className="text-sm text-secondary-text capitalize">
          {resource.type}
        </p>
      </div>
      <svg className="w-5 h-5 text-secondary-text group-hover:text-accent-blue transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
      </svg>
    </div>
  );
};

export default PlanScreen; 