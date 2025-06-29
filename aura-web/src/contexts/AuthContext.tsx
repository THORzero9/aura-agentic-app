import React, { createContext, useContext, useEffect, useState } from 'react';
import { account } from '../lib/appwrite';
import { ID, OAuthProvider } from 'appwrite';
import type { User, AuthState } from '../types';

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<boolean>;
  signup: (name: string, email: string, password: string) => Promise<boolean>;
  verifyOtp: (userId: string, otp: string) => Promise<boolean>;
  loginWithGoogle: () => Promise<boolean>;
  logout: () => Promise<void>;
  checkCurrentUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Check current user on app load
  const checkCurrentUser = async () => {
    try {
      setIsLoading(true);
      const currentUser = await account.get();
      setUser(currentUser as User);
      setError(null);
    } catch (err) {
      setUser(null);
      // Don't set error for initial check - just means not logged in
    } finally {
      setIsLoading(false);
    }
  };

  // Login with email/password (matching Android loginWithPassword)
  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      setError(null);
      
      await account.createEmailPasswordSession(email, password);
      const user = await account.get();
      setUser(user as User);
      return true;
    } catch (err: any) {
      setError('Login failed. Please check your credentials.');
      setUser(null);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // Signup (matching Android createUser)
  const signup = async (name: string, email: string, password: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      setError(null);

      if (password.length < 8) {
        setError('Password must be at least 8 characters long.');
        return false;
      }

      if (name.trim().length === 0) {
        setError('Name cannot be empty.');
        return false;
      }

      // Create user account
      await account.create(ID.unique(), email, password, name);
      
      // Request OTP for verification (matching Android requestOtp)
      await account.createEmailToken(ID.unique(), email);
      
      return true;
    } catch (err: any) {
      setError('Sign up failed. User may already exist.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // Verify OTP and login (matching Android verifyOtpAndLogin)
  const verifyOtp = async (userId: string, otp: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      setError(null);

      await account.createSession(userId, otp);
      const user = await account.get();
      setUser(user as User);
      return true;
    } catch (err: any) {
      setError('Invalid OTP. Please try again.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // Google OAuth login (matching Android signInWithGoogle)
  const loginWithGoogle = async (): Promise<boolean> => {
    try {
      setIsLoading(true);
      setError(null);

      await account.createOAuth2Session(
        OAuthProvider.Google,
        `${window.location.origin}/auth/callback`,
        `${window.location.origin}/auth/failure`
      );
      
      return true;
    } catch (err: any) {
      setError('Google Sign-In failed.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // Logout (matching Android logout)
  const logout = async () => {
    try {
      await account.deleteSession('current');
      setUser(null);
      setError(null);
    } catch (err: any) {
      console.error('Logout error:', err);
      // Force logout locally even if server call fails
      setUser(null);
    }
  };

  // Check user on mount
  useEffect(() => {
    checkCurrentUser();
  }, []);

  const value: AuthContextType = {
    user,
    isLoading,
    error,
    login,
    signup,
    verifyOtp,
    loginWithGoogle,
    logout,
    checkCurrentUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}; 