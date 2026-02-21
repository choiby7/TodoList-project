'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

const signupSchema = z
  .object({
    email: z.string().email('유효한 이메일을 입력해주세요'),
    username: z
      .string()
      .min(2, '이름은 최소 2자 이상이어야 합니다')
      .max(50, '이름은 최대 50자까지 가능합니다'),
    password: z
      .string()
      .min(8, '비밀번호는 최소 8자 이상이어야 합니다')
      .regex(
        /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/,
        '비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다'
      ),
    confirmPassword: z.string().min(1, '비밀번호 확인을 입력해주세요'),
    termsAgreed: z.boolean().refine((val) => val === true, {
      message: '서비스 이용약관에 동의해주세요',
    }),
    privacyAgreed: z.boolean().refine((val) => val === true, {
      message: '개인정보 처리방침에 동의해주세요',
    }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: '비밀번호가 일치하지 않습니다',
    path: ['confirmPassword'],
  });

type SignupFormData = z.infer<typeof signupSchema>;

export default function SignupPage() {
  const { signup, isSigningUp } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignupFormData>({
    resolver: zodResolver(signupSchema),
  });

  const onSubmit = (data: SignupFormData) => {
    const { confirmPassword, ...signupData } = data;
    signup(signupData);
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4 py-8">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold">회원가입</CardTitle>
          <CardDescription>
            새 계정을 만들어 TodoList를 시작하세요
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="email"
                type="email"
                placeholder="example@email.com"
                {...register('email')}
              />
              {errors.email && (
                <p className="text-sm text-red-600">{errors.email.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="username">이름</Label>
              <Input
                id="username"
                type="text"
                placeholder="홍길동"
                {...register('username')}
              />
              {errors.username && (
                <p className="text-sm text-red-600">{errors.username.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">비밀번호</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                {...register('password')}
              />
              {errors.password && (
                <p className="text-sm text-red-600">{errors.password.message}</p>
              )}
              <p className="text-xs text-muted-foreground">
                최소 8자, 대소문자, 숫자, 특수문자 포함
              </p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">비밀번호 확인</Label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="••••••••"
                {...register('confirmPassword')}
              />
              {errors.confirmPassword && (
                <p className="text-sm text-red-600">
                  {errors.confirmPassword.message}
                </p>
              )}
            </div>

            {/* 약관 동의 섹션 */}
            <div className="pt-4 space-y-3 border-t">
              <div className="flex items-start space-x-3">
                <input
                  id="termsAgreed"
                  type="checkbox"
                  className="mt-1 h-4 w-4 rounded border-gray-300"
                  {...register('termsAgreed')}
                />
                <label htmlFor="termsAgreed" className="text-sm leading-relaxed">
                  <span className="text-red-600 font-semibold">*</span>{' '}
                  서비스 이용약관에 동의합니다
                </label>
              </div>
              {errors.termsAgreed && (
                <p className="text-sm text-red-600 ml-7">
                  {errors.termsAgreed.message}
                </p>
              )}

              <div className="flex items-start space-x-3">
                <input
                  id="privacyAgreed"
                  type="checkbox"
                  className="mt-1 h-4 w-4 rounded border-gray-300"
                  {...register('privacyAgreed')}
                />
                <label htmlFor="privacyAgreed" className="text-sm leading-relaxed">
                  <span className="text-red-600 font-semibold">*</span>{' '}
                  개인정보 처리방침에 동의합니다
                </label>
              </div>
              {errors.privacyAgreed && (
                <p className="text-sm text-red-600 ml-7">
                  {errors.privacyAgreed.message}
                </p>
              )}
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button
              type="submit"
              className="w-full"
              disabled={isSigningUp}
            >
              {isSigningUp ? '가입 중...' : '회원가입'}
            </Button>
            <p className="text-sm text-center text-muted-foreground">
              이미 계정이 있으신가요?{' '}
              <Link
                href="/login"
                className="font-medium text-primary hover:underline"
              >
                로그인
              </Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
