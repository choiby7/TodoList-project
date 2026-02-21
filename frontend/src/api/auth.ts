import apiClient from './axios-instance';
import {
  ApiResponse,
  LoginRequest,
  SignupRequest,
  TokenResponse,
  UserResponse,
} from '@/types';

export const authApi = {
  // 회원가입
  signup: async (data: SignupRequest): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.post('/api/v1/auth/signup', data);
    return response.data;
  },

  // 로그인
  login: async (data: LoginRequest): Promise<ApiResponse<TokenResponse>> => {
    const response = await apiClient.post('/api/v1/auth/login', data);
    return response.data;
  },

  // 로그아웃
  logout: async (): Promise<ApiResponse<void>> => {
    const refreshToken = localStorage.getItem('refreshToken');
    const response = await apiClient.post(
      '/api/v1/auth/logout',
      {},
      {
        headers: {
          'X-Refresh-Token': refreshToken || '',
        },
      }
    );
    return response.data;
  },

  // 토큰 갱신
  refresh: async (refreshToken: string): Promise<ApiResponse<TokenResponse>> => {
    const response = await apiClient.post(
      '/api/v1/auth/refresh',
      {},
      {
        headers: {
          'X-Refresh-Token': refreshToken,
        },
      }
    );
    return response.data;
  },

  // 현재 사용자 정보 조회
  getCurrentUser: async (): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.get('/api/v1/auth/me');
    return response.data;
  },

  // 프로필 수정
  updateProfile: async (data: {
    username?: string;
    profileImageUrl?: string;
  }): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.put('/api/v1/auth/profile', data);
    return response.data;
  },

  // 비밀번호 변경
  changePassword: async (data: {
    currentPassword: string;
    newPassword: string;
  }): Promise<ApiResponse<void>> => {
    const response = await apiClient.put('/api/v1/auth/password', data);
    return response.data;
  },

  // 계정 삭제
  deleteAccount: async (): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete('/api/v1/auth/account');
    return response.data;
  },
};
