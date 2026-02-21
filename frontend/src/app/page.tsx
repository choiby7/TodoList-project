'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';

export default function Home() {
  const router = useRouter();
  const { accessToken } = useAuthStore();
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    if (isMounted) {
      if (accessToken) {
        router.push('/dashboard');
      } else {
        router.push('/login');
      }
    }
  }, [isMounted, accessToken, router]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold">TodoList App</h1>
        <p className="mt-4 text-muted-foreground">
          {isMounted ? '리다이렉트 중...' : '로딩 중...'}
        </p>
      </div>
    </div>
  );
}
