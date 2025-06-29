import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const SignUpScreen: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const { signup, error, isLoading } = useAuth();
  const navigate = useNavigate();

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (password !== confirmPassword) {
      // This will be handled by the signup function, but we can add local validation
      return;
    }

    const success = await signup(name, email, password);
    if (success) {
      // Navigate to OTP screen after successful signup
      navigate('/otp', { state: { email } });
    }
  };

  return (
    <div className="min-h-screen bg-app-bg flex items-center justify-center p-6">
      <div className="w-full max-w-md">
        <div className="card">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-primary-text mb-2">
              Create Account
            </h1>
            <p className="text-secondary-text">
              Join Aura and start your learning journey
            </p>
          </div>

          <form onSubmit={handleSignUp} className="space-y-4">
            <div>
              <input
                type="text"
                placeholder="Full Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="input-field"
                required
                disabled={isLoading}
              />
            </div>

            <div>
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="input-field"
                required
                disabled={isLoading}
              />
            </div>

            <div>
              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="input-field"
                required
                disabled={isLoading}
                minLength={8}
              />
            </div>

            <div>
              <input
                type="password"
                placeholder="Confirm Password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="input-field"
                required
                disabled={isLoading}
              />
            </div>

            {/* Local validation for password match */}
            {password && confirmPassword && password !== confirmPassword && (
              <div className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-lg">
                Passwords do not match
              </div>
            )}

            {error && (
              <div className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-lg">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={
                isLoading || 
                !name || 
                !email || 
                !password || 
                !confirmPassword || 
                password !== confirmPassword
              }
              className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Creating Account...' : 'Sign Up'}
            </button>
          </form>

          <div className="text-center mt-6">
            <Link 
              to="/login" 
              className="text-accent-blue hover:underline font-medium"
            >
              Already have an account? Login
            </Link>
          </div>

          <div className="mt-6 text-xs text-secondary-text text-center">
            By signing up, you agree to our Terms of Service and Privacy Policy
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignUpScreen; 