import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { PlanProvider } from './contexts/PlanContext';
import LoginScreen from './components/screens/LoginScreen';
import SignUpScreen from './components/screens/SignUpScreen';
import OtpScreen from './components/screens/OtpScreen';
import HomeScreen from './components/screens/HomeScreen';
import PlanScreen from './components/screens/PlanScreen';
import SavedPlansScreen from './components/screens/SavedPlansScreen';
import AuthCallbackScreen from './components/screens/AuthCallbackScreen';
import LoadingSpinner from './components/common/LoadingSpinner';

// Protected route component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return user ? <>{children}</> : <Navigate to="/login" replace />;
};

// Public route component (redirect to home if already logged in)
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return user ? <Navigate to="/home" replace /> : <>{children}</>;
};

// App navigation component (equivalent to Android AppNavigation)
const AppNavigation: React.FC = () => {
  return (
    <Router>
      <Routes>
        {/* Public routes */}
        <Route 
          path="/login" 
          element={
            <PublicRoute>
              <LoginScreen />
            </PublicRoute>
          } 
        />
        <Route 
          path="/signup" 
          element={
            <PublicRoute>
              <SignUpScreen />
            </PublicRoute>
          } 
        />
        <Route 
          path="/otp" 
          element={
            <PublicRoute>
              <OtpScreen />
            </PublicRoute>
          } 
        />

        {/* OAuth callback routes */}
        <Route path="/auth/callback" element={<AuthCallbackScreen />} />
        <Route path="/auth/failure" element={<LoginScreen />} />

        {/* Protected routes */}
        <Route 
          path="/home" 
          element={
            <ProtectedRoute>
              <HomeScreen />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/plan" 
          element={
            <ProtectedRoute>
              <PlanScreen />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/saved-plans" 
          element={
            <ProtectedRoute>
              <SavedPlansScreen />
            </ProtectedRoute>
          } 
        />

        {/* Default redirect */}
        <Route path="/" element={<Navigate to="/home" replace />} />
        
        {/* Catch all route */}
        <Route path="*" element={<Navigate to="/home" replace />} />
      </Routes>
    </Router>
  );
};

// Main App component
const App: React.FC = () => {
  return (
    <AuthProvider>
      <PlanProvider>
        <div className="min-h-screen bg-app-bg">
          <AppNavigation />
        </div>
      </PlanProvider>
    </AuthProvider>
  );
};

export default App;
