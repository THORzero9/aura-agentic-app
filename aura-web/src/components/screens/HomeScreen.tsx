import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { usePlan } from '../../contexts/PlanContext';
import type { HomeState } from '../../types';

const HomeScreen: React.FC = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { generatePlan, isLoading, setCurrentPlan } = usePlan();

  const [homeState, setHomeState] = useState<HomeState>({
    topic: '',
    hours: 1,
    selectedStyle: '',
    isLoading: false
  });

  const [showLogoutDialog, setShowLogoutDialog] = useState(false);

  const learningStyles = [
    'Video tutorials',
    'Reading articles', 
    'Interactive exercises'
  ];

  const handleTopicChange = (value: string) => {
    setHomeState(prev => ({ ...prev, topic: value }));
  };

  const handleHoursChange = (value: number) => {
    setHomeState(prev => ({ ...prev, hours: value }));
  };

  const handleStyleChange = (style: string) => {
    setHomeState(prev => ({ ...prev, selectedStyle: style }));
  };

  const handleCreatePlan = async () => {
    if (!user || !homeState.topic.trim()) return;

    setHomeState(prev => ({ ...prev, isLoading: true }));

    const request = {
      topic: homeState.topic,
      hours_per_week: homeState.hours,
      preferred_format: homeState.selectedStyle,
      userId: user.$id
    };

    const plan = await generatePlan(request);
    
    setHomeState(prev => ({ ...prev, isLoading: false }));

    if (plan) {
      setCurrentPlan(plan);
      navigate('/plan');
    } else {
      // Handle error - could show a toast or error message
      alert('Failed to generate plan. Please try again.');
    }
  };

  const handleLogout = async () => {
    await logout();
    setShowLogoutDialog(false);
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-app-bg">
      {/* Logout Confirmation Dialog */}
      {showLogoutDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm">
            <h3 className="text-lg font-bold text-primary-text mb-2">Confirm Logout</h3>
            <p className="text-secondary-text mb-6">Are you sure you want to log out?</p>
            <div className="flex space-x-3">
              <button
                onClick={() => setShowLogoutDialog(false)}
                className="btn-secondary flex-1"
              >
                Cancel
              </button>
              <button
                onClick={handleLogout}
                className="btn-primary flex-1"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Loading Overlay */}
      {(homeState.isLoading || isLoading) && (
        <div className="fixed inset-0 bg-primary-text bg-opacity-90 flex items-center justify-center z-40">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-white border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-white text-lg font-medium">Creating your personalized plan...</p>
          </div>
        </div>
      )}

      {/* Main Content */}
      <div className="flex items-center justify-center min-h-screen p-4">
        <div className="w-full max-w-lg">
          <div className="card">
            {/* Header */}
            <div className="flex items-center justify-between mb-8">
              <h1 className="text-2xl font-bold text-primary-text">Aura</h1>
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => navigate('/saved-plans')}
                  className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                  title="Saved Plans"
                >
                  <svg className="w-6 h-6 text-primary-text" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                  </svg>
                </button>
                <button
                  onClick={() => setShowLogoutDialog(true)}
                  className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                  title="Logout"
                >
                  <svg className="w-6 h-6 text-primary-text" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                </button>
              </div>
            </div>

            {/* What do you want to learn? */}
            <div className="mb-6">
              <h2 className="text-xl font-bold text-primary-text mb-3">
                What do you want to learn?
              </h2>
              <input
                type="text"
                placeholder="e.g., Learn to code"
                value={homeState.topic}
                onChange={(e) => handleTopicChange(e.target.value)}
                className="input-field"
                disabled={homeState.isLoading || isLoading}
              />
            </div>

            {/* Hours per week */}
            <div className="mb-6">
              <h2 className="text-xl font-bold text-primary-text mb-3">
                How many hours per week?
              </h2>
              <div className="flex items-center justify-between mb-2">
                <span className="text-primary-text">Hours per week</span>
                <span className="text-primary-text font-bold">{homeState.hours}</span>
              </div>
              <input
                type="range"
                min="1"
                max="10"
                step="1"
                value={homeState.hours}
                onChange={(e) => handleHoursChange(parseInt(e.target.value))}
                className="w-full h-2 bg-slider-track rounded-lg appearance-none cursor-pointer slider"
                disabled={homeState.isLoading || isLoading}
              />
              <style>{`
                .slider::-webkit-slider-thumb {
                  appearance: none;
                  height: 20px;
                  width: 20px;
                  border-radius: 50%;
                  background: #B0C4DE;
                  cursor: pointer;
                }
                .slider::-moz-range-thumb {
                  height: 20px;
                  width: 20px;
                  border-radius: 50%;
                  background: #B0C4DE;
                  cursor: pointer;
                  border: none;
                }
              `}</style>
            </div>

            {/* Learning style */}
            <div className="mb-8">
              <h2 className="text-xl font-bold text-primary-text mb-4">
                What's your learning style?
              </h2>
              <div className="space-y-3">
                <div className="flex flex-wrap gap-2">
                  {learningStyles.slice(0, 2).map((style) => (
                    <button
                      key={style}
                      onClick={() => handleStyleChange(style)}
                      disabled={homeState.isLoading || isLoading}
                      className={`chip ${
                        homeState.selectedStyle === style ? 'chip-selected' : 'chip-unselected'
                      } disabled:opacity-50`}
                    >
                      {style}
                    </button>
                  ))}
                </div>
                <div>
                  <button
                    onClick={() => handleStyleChange(learningStyles[2])}
                    disabled={homeState.isLoading || isLoading}
                    className={`chip ${
                      homeState.selectedStyle === learningStyles[2] ? 'chip-selected' : 'chip-unselected'
                    } disabled:opacity-50`}
                  >
                    {learningStyles[2]}
                  </button>
                </div>
              </div>
            </div>

            {/* Create Plan Button */}
            <button
              onClick={handleCreatePlan}
              disabled={
                !homeState.topic.trim() || 
                homeState.isLoading || 
                isLoading
              }
              className="btn-primary w-full h-14 text-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Create my plan
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomeScreen; 