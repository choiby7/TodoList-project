'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useState, Suspense } from 'react';
import { toast } from 'sonner';
import axios from 'axios';
import { Button } from '@/components/ui/button';

function TermsAgreementHandler() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const sessionId = searchParams.get('session');
  const [isAgreeing, setIsAgreeing] = useState(false);
  const [agreedTerms, setAgreedTerms] = useState(false);
  const [agreedPrivacy, setAgreedPrivacy] = useState(false);

  const handleAgree = async () => {
    if (!agreedTerms || !agreedPrivacy) {
      toast.error('모든 약관에 동의해주세요');
      return;
    }

    if (!sessionId) {
      toast.error('세션이 만료되었습니다');
      router.replace('/login');
      return;
    }

    setIsAgreeing(true);
    try {
      const response = await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/auth/oauth2/agree-terms?session=${sessionId}`
      );

      const { accessToken, refreshToken } = response.data.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      toast.success('가입이 완료되었습니다! 환영합니다 🎉');
      router.replace('/dashboard');
    } catch (error) {
      toast.error('약관 동의 처리에 실패했습니다');
      router.replace('/login');
    } finally {
      setIsAgreeing(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white p-8 rounded-lg shadow">
        <h1 className="text-2xl font-bold mb-6">서비스 약관 동의</h1>

        <div className="space-y-4 mb-6">
          <label className="flex items-start space-x-3">
            <input
              type="checkbox"
              checked={agreedTerms}
              onChange={(e) => setAgreedTerms(e.target.checked)}
              className="mt-1"
            />
            <span className="text-sm">
              <strong className="text-red-600">*</strong> 서비스 이용약관에 동의합니다
            </span>
          </label>

          <label className="flex items-start space-x-3">
            <input
              type="checkbox"
              checked={agreedPrivacy}
              onChange={(e) => setAgreedPrivacy(e.target.checked)}
              className="mt-1"
            />
            <span className="text-sm">
              <strong className="text-red-600">*</strong> 개인정보 처리방침에 동의합니다
            </span>
          </label>
        </div>

        <Button
          onClick={handleAgree}
          disabled={isAgreeing || !agreedTerms || !agreedPrivacy}
          className="w-full"
        >
          {isAgreeing ? '처리 중...' : '동의하고 가입하기'}
        </Button>
      </div>
    </div>
  );
}

export default function TermsAgreementPage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <TermsAgreementHandler />
    </Suspense>
  );
}
