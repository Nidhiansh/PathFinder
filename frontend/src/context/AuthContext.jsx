import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { profileService } from '../services/profileService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(authService.getStoredUser());
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const initAuth = async () => {
      if (authService.isAuthenticated()) {
        try {
          const userData = await authService.getCurrentUser();
          const profileData = await profileService.getProfile();
          setUser({ ...authService.getStoredUser(), ...userData });
          setProfile(profileData);
        } catch (err) {
          console.error("Failed to restore session", err);
          authService.logout();
          setUser(null);
          setProfile(null);
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (username, password) => {
    setError(null);
    try {
      const data = await authService.login(username, password);
      setUser(data);
      try {
        const profileData = await profileService.getProfile();
        setProfile(profileData);
      } catch {
        // Fallback if profile not immediately ready
      }
      return data;
    } catch (err) {
      const msg = err.response?.data?.message || 'Invalid username or password';
      setError(msg);
      throw new Error(msg);
    }
  };

  const register = async (username, email, password, fullName) => {
    setError(null);
    try {
      const data = await authService.register(username, email, password, fullName);
      setUser(data);
      try {
        const profileData = await profileService.getProfile();
        setProfile(profileData);
      } catch {
        // Fallback
      }
      return data;
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed';
      setError(msg);
      throw new Error(msg);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setProfile(null);
  };

  const refreshProfile = async () => {
    try {
      const profileData = await profileService.getProfile();
      setProfile(profileData);
      return profileData;
    } catch (err) {
      console.error("Failed to refresh profile", err);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        profile,
        loading,
        error,
        login,
        register,
        logout,
        refreshProfile,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
