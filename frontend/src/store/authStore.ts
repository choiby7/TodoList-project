import { create } from 'zustand';
import { UserResponse } from '@/types';

interface AuthState {
  user: UserResponse | null;
  accessToken: string | null;
  refreshToken: string | null;
  setAuth: (accessToken: string, refreshToken: string, user: UserResponse) => void;
  setUser: (user: UserResponse) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null,
  refreshToken: typeof window !== 'undefined' ? localStorage.getItem('refreshToken') : null,

  setAuth: (accessToken, refreshToken, user) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
    }
    set({ accessToken, refreshToken, user });
  },

  setUser: (user) => {
    set({ user });
  },

  clearAuth: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
    set({ accessToken: null, refreshToken: null, user: null });
  },
}));
