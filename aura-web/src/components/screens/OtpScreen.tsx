import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const OtpScreen: React.FC = () => {
  const [otp, setOtp] = useState('');
  const { verifyOtp, error, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  // Get email from navigation state
  const email = location.state?.email || '';

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Note: In the web version, we'll need to handle the userId differently
    // For now, we'll use a placeholder approach similar to Android
    const success = await verifyOtp('web-user-temp', otp);
    if (success) {
      navigate('/home');
    }
  };

  const handleGoToLogin = () => {
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-app-bg flex items-center justify-center p-6">
      <div className="w-full max-w-md">
        <div className="card">
          <div className="text-center mb-8">
            <div className="w-16 h-16 bg-accent-blue bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-accent-blue" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-primary-text mb-2">
              Check Your Email
            </h1>
            <p className="text-secondary-text">
              We've sent a verification code to
            </p>
            <p className="text-primary-text font-medium">

              {email}
            </p>
          </div>
          

          <form onSubmit={handleVerifyOtp} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-primary-text mb-2">
                Enter verification code
              </label>
              <input
                type="text"
                placeholder="123456"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                className="input-field text-center text-lg tracking-widest"
                required
                disabled={isLoading}
                maxLength={6}
                pattern="[0-9]{6}"
              />
              <p className="text-xs text-secondary-text mt-2 text-center">
                Enter the 6-digit code from your email
              </p>
            </div>

            {error && (
              <div className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-lg">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading || otp.length !== 6}
              className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Verifying...' : 'Verify & Continue'}
            </button>
          </form>

          <div className="text-center mt-6 space-y-4">
            <p className="text-sm text-secondary-text">
              Didn't receive the code?
            </p>
            <button
              onClick={() => {
                // In a real app, you'd implement resend OTP functionality here
                console.log('Resend OTP clicked');
              }}
              className="text-accent-blue hover:underline font-medium text-sm"
              disabled={isLoading}
            >
              Resend Code
            </button>
          </div>

          <div className="border-t border-chip-border mt-8 pt-6">
            <button
              onClick={handleGoToLogin}
              className="btn-secondary w-full"
              disabled={isLoading}
            >
              Back to Login
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OtpScreen; 