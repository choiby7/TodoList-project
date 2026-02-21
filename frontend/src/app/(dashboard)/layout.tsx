'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import { Header } from '@/components/layout/Header';
import { Sidebar } from '@/components/layout/Sidebar';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const { accessToken } = useAuthStore();
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    if (isMounted && !accessToken) {
      router.push('/login');
    }
  }, [isMounted, accessToken, router]);

  // Hydration 에러 방지: 클라이언트에서만 렌더링
  if (!isMounted) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-gray-500">로딩 중...</p>
      </div>
    );
  }

  if (!accessToken) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-gray-500">로그인이 필요합니다...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <Sidebar />
      <main className="md:pl-64 pt-16">
        {children}
      </main>
    </div>
  );
}
