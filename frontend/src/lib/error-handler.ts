import { AxiosError } from 'axios';
import { ErrorResponse } from '@/types';
import { toast } from 'sonner';

const errorMessages: Record<string, string> = {
  // Auth
  AUTH_INVALID_CREDENTIALS: '이메일 또는 비밀번호가 올바르지 않습니다',
  AUTH_TOKEN_EXPIRED: '토큰이 만료되었습니다',
  AUTH_TOKEN_INVALID: '유효하지 않은 토큰입니다',
  AUTH_ACCOUNT_LOCKED: '계정이 잠겨있습니다',
  AUTH_EMAIL_NOT_VERIFIED: '이메일 인증이 필요합니다',

  // User
  USER_NOT_FOUND: '사용자를 찾을 수 없습니다',
  USER_EMAIL_DUPLICATE: '이미 사용 중인 이메일입니다',
  USER_INVALID_PASSWORD: '비밀번호가 정책을 만족하지 않습니다',

  // Todo
  TODO_NOT_FOUND: '요청한 할 일을 찾을 수 없습니다',
  TODO_FORBIDDEN: '이 할 일에 접근할 권한이 없습니다',
  TODO_ALREADY_DELETED: '이미 삭제된 할 일입니다',

  // Category
  CATEGORY_NOT_FOUND: '카테고리를 찾을 수 없습니다',
  CATEGORY_NAME_DUPLICATE: '이미 존재하는 카테고리 이름입니다',

  // Common
  COMMON_INVALID_PARAMETER: '유효하지 않은 요청 파라미터입니다',
  COMMON_INTERNAL_ERROR: '서버 내부 오류가 발생했습니다',
  COMMON_RATE_LIMIT_EXCEEDED: '요청 한도를 초과했습니다',
};

export function handleApiError(error: unknown): string {
  if (error instanceof AxiosError) {
    const errorResponse = error.response?.data as ErrorResponse | undefined;

    if (errorResponse?.errorCode) {
      // 유효성 검증 에러 (여러 필드)
      if (errorResponse.errors && errorResponse.errors.length > 0) {
        const fieldErrors = errorResponse.errors
          .map((e) => `${e.field}: ${e.message}`)
          .join('\n');
        toast.error('입력값을 확인해주세요', {
          description: fieldErrors,
        });
        return fieldErrors;
      }

      // 에러 코드 기반 메시지
      const message = errorMessages[errorResponse.errorCode] || errorResponse.message;
      return message;
    }

    // HTTP 상태 코드 기반 메시지
    if (error.response?.status === 401) {
      return '인증이 필요합니다';
    }

    if (error.response?.status === 403) {
      return '접근 권한이 없습니다';
    }

    if (error.response?.status === 404) {
      return '요청한 리소스를 찾을 수 없습니다';
    }

    if (error.response?.status === 429) {
      toast.error('요청 한도 초과', {
        description: '잠시 후 다시 시도해주세요',
      });
      return '요청 한도를 초과했습니다';
    }

    if (error.response?.status && error.response.status >= 500) {
      return '서버 오류가 발생했습니다';
    }
  }

  // 네트워크 에러
  if (error instanceof Error && error.message === 'Network Error') {
    return '네트워크 연결을 확인해주세요';
  }

  return '알 수 없는 오류가 발생했습니다';
}
