'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState, useRef, Suspense } from 'react';
import { toast } from 'sonner';
import axios from 'axios';

/**
 * OAuth2 콜백 핸들러 컴포넌트
 */
function OAuth2CallbackHandler() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isProcessing, setIsProcessing] = useState(true);
  const hasProcessedRef = useRef(false);

  useEffect(() => {
    // 중복 실행 방지 (useRef 사용 - React Strict Mode 대응)
    if (hasProcessedRef.current) {
      console.log('[OAuth2 Callback] 중복 실행 차단');
      return;
    }
    hasProcessedRef.current = true;

    const processOAuth2Callback = async () => {
      // 에러 처리
      const error = searchParams.get('error');
      const errorMessage = searchParams.get('message');
      if (error) {
        toast.error(decodeURIComponent(errorMessage || 'OAuth2 로그인에 실패했습니다'));
        setTimeout(() => {
          router.replace('/login');
        }, 2000);
        return;
      }

      // 세션 ID 추출
      const sessionId = searchParams.get('session');
      if (!sessionId) {
        toast.error('세션 정보가 없습니다');
        setTimeout(() => {
          router.replace('/login');
        }, 2000);
        return;
      }

      try {
        // 세션 ID로 OAuth2 교환 요청
        const response = await axios.get(
          `${process.env.NEXT_PUBLIC_API_URL}/api/v1/auth/oauth2/exchange?session=${sessionId}`,
          {
            headers: {
              'Content-Type': 'application/json',
            },
          }
        );

        const data = response.data.data;

        // 1. 약관 동의 필요
        if (data.needsTermsAgreement) {
          router.replace(`/auth/oauth2/terms?session=${data.pendingSessionId}`);
          return;
        }

        // 2. 계정 병합 동의 필요
        if (data.needsAccountMerge) {
          router.replace(
            `/auth/oauth2/merge?session=${data.pendingSessionId}&email=${encodeURIComponent(data.existingEmail)}`
          );
          return;
        }

        // 3. 정상 로그인 - 토큰 저장
        if (data.accessToken && data.refreshToken) {
          localStorage.setItem('accessToken', data.accessToken);
          localStorage.setItem('refreshToken', data.refreshToken);
          toast.success('Google 로그인 성공!');
          setTimeout(() => {
            router.replace('/dashboard');
          }, 500);
          return;
        }

        throw new Error('Invalid response');
      } catch (error: any) {
        console.error('[OAuth2 Callback] 토큰 교환 실패:', error);

        const errorMessage =
          error.response?.data?.message || '토큰 교환에 실패했습니다';
        toast.error(errorMessage);

        setTimeout(() => {
          router.replace('/login');
        }, 2000);
      } finally {
        setIsProcessing(false);
      }
    };

    processOAuth2Callback();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // 빈 배열로 변경 - 마운트 시 한 번만 실행

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        {isProcessing ? (
          <>
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p className="text-gray-600 text-lg">로그인 처리 중...</p>
          </>
        ) : (
          <p className="text-gray-600 text-lg">리다이렉트 중...</p>
        )}
      </div>
    </div>
  );
}

/**
 * OAuth2 콜백 페이지
 * 1. URL에서 세션 ID 추출
 * 2. 세션 ID로 JWT 토큰 교환
 * 3. localStorage에 토큰 저장
 * 4. /dashboard로 리다이렉트
 */
export default function OAuth2CallbackPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p className="text-gray-600 text-lg">로그인 처리 중...</p>
          </div>
        </div>
      }
    >
      <OAuth2CallbackHandler />
    </Suspense>
  );
}
