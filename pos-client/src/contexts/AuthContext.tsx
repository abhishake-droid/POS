import { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { useRouter } from 'next/router';
import { AuthData } from '../types/auth.types';
import { authService } from '../services/auth.service';
import { authStorage } from '../utils/authStorage';

interface AuthContextType {
  user: AuthData | null;
  login: (email: string, password?: string) => Promise<void>;
  logout: () => Promise<void>;
  isSupervisor: boolean;
  isLoading: boolean;
  checkAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  const checkAuth = useCallback(async () => {
    const token = authStorage.getToken();
    if (!token) {
      setUser(null);
      setIsLoading(false);
      return;
    }

    // If user is already set and we checked recently, skip validation
    if (user && !authStorage.shouldCheckAuth()) {
      setIsLoading(false);
      return;
    }

    // Validate token with backend
    try {
      const authData = await authService.validateToken(token);
      setUser(authData);
      authStorage.setToken(authData.token);
      authStorage.setUserId(authData.email); // Using email as userId
      authStorage.setLastCheckTime();
    } catch (error) {
      authStorage.clearAll();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  useEffect(() => {
    checkAuth();
  }, []);

  const login = async (email: string, password?: string) => {
    try {
      const authData = await authService.login({ email, password });
      setUser(authData);
      authStorage.setToken(authData.token);
      authStorage.setUserId(authData.email);
      authStorage.setLastCheckTime();
      router.push('/');
    } catch (error: any) {
      throw error;
    }
  };

  const logout = async () => {
    if (user?.email) {
      try {
        await authService.logout(user.email);
      } catch (error) {
        // Log error but continue with logout
        console.error('Failed to log logout activity:', error);
      }
    }
    setUser(null);
    authStorage.clearAll();
    router.push('/login');
  };

  return (
      <AuthContext.Provider
          value={{
            user,
            login,
            logout,
            isSupervisor: user?.role === 'SUPERVISOR',
            isLoading,
            checkAuth,
          }}
      >
        {children}
      </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}