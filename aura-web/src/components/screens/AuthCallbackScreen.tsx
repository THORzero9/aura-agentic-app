import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const AuthCallbackScreen: React.FC = () => {
  const navigate = useNavigate();
  const { checkCurrentUser } = useAuth();

  useEffect(() => {
    const handleOAuthCallback = async () => {
      try {
        // Wait a moment for Appwrite to process the OAuth session
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // Check if user is now authenticated
        await checkCurrentUser();
        
        // Navigate to home on success
        navigate('/home');
      } catch (error) {
        console.error('OAuth callback error:', error);
        // Navigate to login with error on failure
        navigate('/login');
      }
    };

    handleOAuthCallback();
  }, [navigate, checkCurrentUser]);

  return (
    <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#F0F4F8' }}>
      <div className="text-center">
        <div className="w-16 h-16 border-4 border-white border-t-transparent rounded-full animate-spin mx-auto mb-4" style={{ borderColor: '#B0C4DE', borderTopColor: 'transparent' }}></div>
        <h2 className="text-xl font-bold mb-2" style={{ color: '#333A44' }}>
          Completing Sign In...
        </h2>
        <p style={{ color: '#8A99A8' }}>
          Please wait while we sign you in with Google
        </p>
      </div>
    </div>
  );
};

export default AuthCallbackScreen; 