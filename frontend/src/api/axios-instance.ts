import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ErrorResponse } from '@/types';
import { toast } from 'sonner';
import { getErrorMessage, getStatusMessage } from '@/lib/error-messages';

// ?? (nullish coalescing): undefined/null만 fallback, 빈 문자열("")은 유효한 값으로 처리
const baseURL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';
const timeout = Number(process.env.NEXT_PUBLIC_API_TIMEOUT) || 10000;

export const apiClient = axios.create({
  baseURL,
  timeout,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 토큰 갱신 중 여부를 추적
let isRefreshing = false;
let isLoggingOut = false; // 로그아웃 중복 방지
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: any = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve();
    }
  });

  failedQueue = [];
};

// Request Interceptor: Access Token 추가
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const accessToken = localStorage.getItem('accessToken');

    if (accessToken && config.headers) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: 토큰 갱신 처리
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError<ErrorResponse>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // 디버깅 로그
    console.log('🔍 Axios Error:', {
      status: error.response?.status,
      errorCode: error.response?.data?.errorCode,
      message: error.response?.data?.message,
      url: originalRequest?.url,
    });

    // AUTH_TOKEN_EXPIRED 감지 (401 또는 403)
    if (
      (error.response?.status === 401 || error.response?.status === 403) &&
      error.response?.data?.errorCode === 'AUTH_TOKEN_EXPIRED' &&
      originalRequest &&
      !originalRequest._retry
    ) {
      if (isRefreshing) {
        // 토큰 갱신 중이면 큐에 추가
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(() => {
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        // Refresh Token이 없으면 로그아웃
        handleLogout();
        return Promise.reject(error);
      }

      try {
        // Refresh Token으로 새 Access Token 요청
        const response = await axios.post(
          `${baseURL}/api/v1/auth/refresh`,
          {},
          {
            headers: {
              'X-Refresh-Token': refreshToken,
            },
          }
        );

        const { accessToken: newAccessToken, refreshToken: newRefreshToken } =
          response.data.data;

        // 새 토큰 저장
        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        // 큐에 있는 요청들 처리
        processQueue();

        // 원래 요청 재시도
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }

        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh 실패 시 로그아웃
        processQueue(refreshError);
        toast.error('로그인이 만료되었습니다', {
          description: '다시 로그인해주세요',
        });
        handleLogout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // 일반 401/403 에러 (AUTH_TOKEN_EXPIRED가 아닌 경우)
    if (
      (error.response?.status === 401 || error.response?.status === 403) &&
      !originalRequest?._retry
    ) {
      const errorCode = error.response?.data?.errorCode;

      // 로그인 실패 - Toast만 표시 (로그아웃 안 함)
      if (errorCode === 'AUTH_INVALID_CREDENTIALS') {
        const message = getErrorMessage(errorCode);
        toast.error(message.title, {
          description: message.description,
        });
        return Promise.reject(error);
      }

      // ACCOUNT_LOCKED 처리
      if (errorCode === 'AUTH_ACCOUNT_LOCKED') {
        const message = getErrorMessage(errorCode);
        toast.error(message.title, {
          description: message.description,
        });
        handleLogout();
        return Promise.reject(error);
      }

      // 기타 인증 에러 (세션 만료 등)
      console.log('🚨 인증 에러 발생, 로그아웃 처리');
      const message = getErrorMessage(errorCode) || getStatusMessage(error.response.status);
      toast.error(message.title, {
        description: message.description,
      });
      handleLogout();
      return Promise.reject(error);
    }

    // 네트워크 에러 (서버 응답 없음)
    if (!error.response) {
      console.log('🌐 네트워크 에러:', error.message);

      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        toast.error('요청 시간이 초과되었습니다', {
          description: '네트워크 연결을 확인해주세요',
        });
      } else if (error.message === 'Network Error') {
        toast.error('네트워크 연결에 실패했습니다', {
          description: '인터넷 연결을 확인해주세요',
        });
      } else {
        toast.error('요청에 실패했습니다', {
          description: error.message,
        });
      }

      return Promise.reject(error);
    }

    // 기타 HTTP 에러 (400, 404, 409, 500 등)
    const errorCode = error.response?.data?.errorCode;
    const status = error.response?.status;

    console.log(`🔴 HTTP ${status} 에러:`, errorCode || 'NO_CODE');

    // 에러 코드가 있으면 매핑된 메시지 사용, 없으면 상태 코드 기반 메시지
    const message = errorCode ? getErrorMessage(errorCode) : getStatusMessage(status);

    // 유효성 검증 에러 (400)에 대한 상세 처리
    if (status === 400 && error.response?.data?.errors) {
      const validationErrors = error.response.data.errors;
      const firstError = validationErrors[0];

      toast.error(message.title, {
        description: firstError?.message || message.description,
      });
    } else {
      // 일반 에러
      toast.error(message.title, {
        description: message.description,
      });
    }

    return Promise.reject(error);
  }
);

function handleLogout() {
  if (isLoggingOut) {
    console.log('⚠️ 이미 로그아웃 처리 중입니다');
    return;
  }

  isLoggingOut = true;
  console.log('🚪 로그아웃 처리 중...');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');

  // 클라이언트 사이드에서만 리다이렉트
  if (typeof window !== 'undefined') {
    // 이미 로그인 페이지에 있으면 리다이렉트하지 않음
    if (window.location.pathname === '/login' || window.location.pathname === '/signup') {
      console.log('✅ 이미 인증 페이지에 있습니다. 리다이렉트하지 않습니다.');
      isLoggingOut = false; // 플래그 리셋
      return;
    }

    // Toast를 볼 수 있도록 3초 후 리다이렉트
    console.log('⏰ 3초 후 로그인 페이지로 이동합니다...');
    setTimeout(() => {
      console.log('🚪 로그인 페이지로 이동');
      window.location.href = '/login';
    }, 3000);
  }
}

export default apiClient;
