# TodoList Frontend

Next.js 기반 TodoList 애플리케이션 프론트엔드

## 기술 스택

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui
- **Server State**: TanStack Query (React Query)
- **Client State**: Zustand
- **Form**: React Hook Form + Zod
- **HTTP Client**: Axios

## 주요 기능

- ✅ 회원가입 및 로그인 (JWT 인증)
- ✅ Todo CRUD (생성, 조회, 수정, 삭제)
- ✅ Optimistic Update (즉시 UI 반영)
- ✅ 완료 상태 토글
- ✅ 소프트 삭제 및 휴지통 관리
- ✅ 카테고리 관리
- ✅ 필터링 및 검색 (300ms 디바운스)
- ✅ 우선순위 및 마감일 설정
- ✅ 자동 토큰 갱신

## 시작하기

### 1. 환경 변수 설정

`.env.local` 파일에서 백엔드 API URL을 설정:

\`\`\`env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_API_TIMEOUT=10000
\`\`\`

### 2. 의존성 설치

\`\`\`bash
npm install
\`\`\`

### 3. 개발 서버 실행

\`\`\`bash
npm run dev
\`\`\`

브라우저에서 http://localhost:3000 접속

### 4. 프로덕션 빌드

\`\`\`bash
npm run build
npm run start
\`\`\`

## 디렉토리 구조

\`\`\`
src/
├── app/                    # Next.js App Router
│   ├── (auth)/            # 인증 페이지 (로그인, 회원가입)
│   ├── (dashboard)/       # 대시보드 (Todo 목록, 휴지통)
│   ├── layout.tsx         # Root Layout
│   └── providers.tsx      # TanStack Query Provider
├── components/            # React 컴포넌트
│   ├── layout/           # Header, Sidebar
│   ├── todo/             # TodoItem, TodoList, TodoForm, TodoFilters
│   ├── category/         # CategoryList, CategoryForm
│   └── ui/               # shadcn/ui 컴포넌트
├── hooks/                # Custom Hooks
│   ├── useAuth.ts        # 인증 관련 hooks
│   ├── useTodos.ts       # Todo CRUD hooks (Optimistic Update)
│   └── useCategories.ts  # Category hooks
├── store/                # Zustand 상태 관리
│   ├── authStore.ts      # 인증 상태
│   └── uiStore.ts        # UI 상태 (사이드바)
├── api/                  # API 서비스 레이어
│   ├── axios-instance.ts # Axios 설정 (토큰 갱신 interceptor)
│   ├── auth.ts           # 인증 API
│   ├── todos.ts          # Todo API
│   └── categories.ts     # Category API
├── types/                # TypeScript 타입 정의
│   └── index.ts
└── lib/                  # 유틸리티 함수
    ├── utils.ts          # 날짜 포맷, 스타일 함수
    ├── error-handler.ts  # 에러 처리
    ├── query-client.ts   # TanStack Query 설정
    └── query-keys.ts     # Query Key 구조
\`\`\`

## Optimistic Update 패턴

Todo 완료 토글, 삭제 등의 작업에서 Optimistic Update 패턴을 사용하여 즉각적인 UI 반응을 제공:

\`\`\`typescript
// 1. onMutate: 즉시 UI 업데이트
// 2. onError: 실패 시 이전 상태로 롤백
// 3. onSettled: 서버 데이터로 재검증
\`\`\`

## JWT 토큰 관리

- **Access Token**: localStorage 저장, Authorization 헤더로 전송
- **Refresh Token**: localStorage 저장, X-Refresh-Token 헤더로 전송
- **자동 갱신**: Axios interceptor에서 만료 시 자동 갱신
- **실패 시**: 로그아웃 및 로그인 페이지로 리다이렉트

## 백엔드 연동

백엔드 서버가 http://localhost:8080 에서 실행 중이어야 합니다.

백엔드 실행:
\`\`\`bash
cd ../todolist-backend
./gradlew bootRun
\`\`\`

## 📖 문서

- [구현 요약](./docs/IMPLEMENTATION_SUMMARY.md)

## 주의사항

- 회원가입 후 로그인 필요
- 백엔드 서버가 실행 중이어야 모든 기능 사용 가능
- 토큰은 브라우저 localStorage에 저장됨

## 라이선스

MIT
