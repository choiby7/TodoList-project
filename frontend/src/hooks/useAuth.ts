import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { authApi } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';
import { authKeys } from '@/lib/query-keys';
import { handleApiError } from '@/lib/error-handler';
import { LoginRequest, SignupRequest } from '@/types';

export function useAuth() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { setAuth, clearAuth } = useAuthStore();

  // 로그인
  const loginMutation = useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: async (response) => {
      const { accessToken, refreshToken } = response.data;

      // 사용자 정보 가져오기
      try {
        // 토큰을 먼저 저장해야 getCurrentUser가 작동함
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);

        const userResponse = await authApi.getCurrentUser();
        setAuth(accessToken, refreshToken, userResponse.data);

        toast.success('로그인 성공!');
        router.push('/dashboard');
      } catch (error) {
        toast.error('사용자 정보를 가져오는데 실패했습니다');
        clearAuth();
      }
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('로그인 실패', { description: message });
    },
  });

  // 회원가입
  const signupMutation = useMutation({
    mutationFn: (data: SignupRequest) => authApi.signup(data),
    onSuccess: () => {
      toast.success('회원가입 성공!', {
        description: '로그인 페이지로 이동합니다',
      });
      router.push('/login');
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('회원가입 실패', { description: message });
    },
  });

  // 로그아웃
  const logoutMutation = useMutation({
    mutationFn: () => authApi.logout(),
    onSuccess: () => {
      clearAuth();
      queryClient.clear();
      toast.success('로그아웃 되었습니다');
      router.push('/login');
    },
    onError: (error) => {
      // 로그아웃은 서버 에러가 나도 클라이언트에서 처리
      clearAuth();
      queryClient.clear();
      router.push('/login');
    },
  });

  return {
    login: loginMutation.mutate,
    signup: signupMutation.mutate,
    logout: logoutMutation.mutate,
    isLoggingIn: loginMutation.isPending,
    isSigningUp: signupMutation.isPending,
    isLoggingOut: logoutMutation.isPending,
  };
}

export function useCurrentUser() {
  const { accessToken } = useAuthStore();

  return useQuery({
    queryKey: authKeys.currentUser(),
    queryFn: async () => {
      const response = await authApi.getCurrentUser();
      return response.data;
    },
    enabled: !!accessToken,
    staleTime: 5 * 60 * 1000, // 5분
  });
}
