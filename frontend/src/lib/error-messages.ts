/**
 * 에러 코드 → 사용자 친화적 메시지 매핑
 * CLAUDE.md 에러 코드 체계 기준
 */

export const ERROR_MESSAGES: Record<string, { title: string; description?: string }> = {
  // 인증 (AUTH)
  AUTH_INVALID_CREDENTIALS: {
    title: '로그인 실패',
    description: '이메일 또는 비밀번호가 올바르지 않습니다',
  },
  AUTH_TOKEN_EXPIRED: {
    title: '로그인이 만료되었습니다',
    description: '다시 로그인해주세요',
  },
  AUTH_TOKEN_INVALID: {
    title: '인증에 실패했습니다',
    description: '유효하지 않은 토큰입니다',
  },
  AUTH_ACCOUNT_LOCKED: {
    title: '계정이 잠겼습니다',
    description: '15분 후 다시 시도해주세요',
  },
  AUTH_EMAIL_NOT_VERIFIED: {
    title: '이메일 인증이 필요합니다',
    description: '인증 메일을 확인해주세요',
  },
  AUTH_OAUTH2_EMAIL_NOT_PROVIDED: {
    title: 'Google 계정 이메일 정보 없음',
    description: 'Google 계정에 이메일 정보가 없습니다',
  },
  AUTH_OAUTH2_ACCOUNT_MERGE_CONFLICT: {
    title: '계정 병합 실패',
    description: '이미 다른 OAuth2 제공자와 연결된 계정입니다',
  },

  // 사용자 (USER)
  USER_NOT_FOUND: {
    title: '사용자를 찾을 수 없습니다',
  },
  USER_EMAIL_DUPLICATE: {
    title: '이미 사용 중인 이메일입니다',
    description: '다른 이메일을 사용해주세요',
  },
  USER_INVALID_PASSWORD: {
    title: '비밀번호가 정책을 만족하지 않습니다',
    description: '8자 이상, 영문/숫자/특수문자 포함',
  },

  // Todo (TODO)
  TODO_NOT_FOUND: {
    title: '할 일을 찾을 수 없습니다',
  },
  TODO_FORBIDDEN: {
    title: '접근 권한이 없습니다',
    description: '이 할 일에 접근할 수 없습니다',
  },
  TODO_ALREADY_DELETED: {
    title: '이미 삭제된 할 일입니다',
  },

  // 카테고리 (CATEGORY)
  CATEGORY_NOT_FOUND: {
    title: '카테고리를 찾을 수 없습니다',
  },
  CATEGORY_NAME_DUPLICATE: {
    title: '이미 존재하는 카테고리 이름입니다',
    description: '다른 이름을 사용해주세요',
  },

  // 공통 (COMMON)
  COMMON_INVALID_PARAMETER: {
    title: '유효하지 않은 요청입니다',
    description: '입력값을 확인해주세요',
  },
  COMMON_INTERNAL_ERROR: {
    title: '서버 오류가 발생했습니다',
    description: '잠시 후 다시 시도해주세요',
  },
  COMMON_RATE_LIMIT_EXCEEDED: {
    title: '요청 한도를 초과했습니다',
    description: '잠시 후 다시 시도해주세요',
  },
};

/**
 * 에러 코드로부터 사용자 메시지 가져오기
 */
export function getErrorMessage(errorCode: string | undefined): {
  title: string;
  description?: string;
} {
  if (!errorCode) {
    return {
      title: '오류가 발생했습니다',
      description: '잠시 후 다시 시도해주세요',
    };
  }

  return (
    ERROR_MESSAGES[errorCode] || {
      title: '오류가 발생했습니다',
      description: errorCode,
    }
  );
}

/**
 * HTTP 상태 코드 기반 기본 메시지
 */
export function getStatusMessage(status: number): {
  title: string;
  description?: string;
} {
  switch (status) {
    case 400:
      return {
        title: '잘못된 요청입니다',
        description: '입력값을 확인해주세요',
      };
    case 401:
      return {
        title: '인증이 필요합니다',
        description: '다시 로그인해주세요',
      };
    case 403:
      return {
        title: '접근 권한이 없습니다',
      };
    case 404:
      return {
        title: '요청한 리소스를 찾을 수 없습니다',
      };
    case 409:
      return {
        title: '이미 존재하는 데이터입니다',
      };
    case 429:
      return {
        title: '요청이 너무 많습니다',
        description: '잠시 후 다시 시도해주세요',
      };
    case 500:
      return {
        title: '서버 오류가 발생했습니다',
        description: '잠시 후 다시 시도해주세요',
      };
    default:
      return {
        title: '오류가 발생했습니다',
        description: `상태 코드: ${status}`,
      };
  }
}
