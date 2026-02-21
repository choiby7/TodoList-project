'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useState, Suspense } from 'react';
import { toast } from 'sonner';
import axios from 'axios';
import { Button } from '@/components/ui/button';

function AccountMergeHandler() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const sessionId = searchParams.get('session');
  const existingEmail = searchParams.get('email');
  const [isMerging, setIsMerging] = useState(false);

  const handleMerge = async () => {
    if (!sessionId) {
      toast.error('세션이 만료되었습니다');
      router.replace('/login');
      return;
    }

    setIsMerging(true);
    try {
      const response = await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/auth/oauth2/agree-merge?session=${sessionId}`
      );

      const { accessToken, refreshToken } = response.data.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      toast.success('Google 계정이 연동되었습니다!');
      router.replace('/dashboard');
    } catch (error) {
      toast.error('계정 연동에 실패했습니다');
      router.replace('/login');
    } finally {
      setIsMerging(false);
    }
  };

  const handleCancel = () => {
    router.replace('/login');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white p-8 rounded-lg shadow">
        <h1 className="text-2xl font-bold mb-4">계정 연동 확인</h1>

        <div className="bg-blue-50 border border-blue-200 rounded p-4 mb-6">
          <p className="text-sm text-blue-900 mb-2">
            <strong>{existingEmail}</strong> 계정이 이미 존재합니다.
          </p>
          <p className="text-sm text-blue-700">
            Google 계정과 연동하시겠습니까?
          </p>
        </div>

        <div className="space-y-3">
          <Button onClick={handleMerge} disabled={isMerging} className="w-full">
            {isMerging ? '처리 중...' : '연동하기'}
          </Button>

          <Button
            onClick={handleCancel}
            disabled={isMerging}
            variant="outline"
            className="w-full"
          >
            취소
          </Button>
        </div>

        <p className="text-xs text-gray-500 mt-4">
          * 연동하면 Google 계정으로도 로그인할 수 있습니다.
        </p>
      </div>
    </div>
  );
}

export default function AccountMergePage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <AccountMergeHandler />
    </Suspense>
  );
}
