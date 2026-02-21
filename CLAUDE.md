# CLAUDE.md - TodoList 프로젝트 컨텍스트

> 이 파일은 Claude Code가 프로젝트의 모든 컨텍스트를 파악하기 위한 가이드입니다.
> 코드 작성 전 반드시 이 파일 전체를 읽고 모든 규칙을 따르세요.

---

## 📌 프로젝트 개요

- **프로젝트명**: TodoList 서비스
- **목적**: 개인 및 팀 협업이 가능한 프로덕션 레벨의 Todo 관리 서비스
- **아키텍처**: 3-Tier (Next.js + Spring Boot + PostgreSQL)
- **배포 환경**: AWS (Vercel + ECS Fargate + RDS)

---

## 🏗️ 기술 스택

### 백엔드
| 항목 | 기술 |
|------|------|
| Framework | Spring Boot 3.x |
| Language | Java 17+ |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL 15+ |
| Cache | Redis (ElastiCache) |
| Migration | Flyway |
| Auth | Spring Security 6.x + JWT (jjwt 0.11.5+) |
| API Docs | Swagger (springdoc-openapi) |
| Build | Gradle |
| Test | JUnit 5 + Mockito + Testcontainers |

### 프론트엔드
| 항목 | 기술 |
|------|------|
| Framework | Next.js 14+ (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS |
| UI Components | shadcn/ui |
| Server State | TanStack Query (React Query) |
| Client State | Zustand |
| Form | React Hook Form + Zod |
| HTTP | Axios |
| Test | Jest + React Testing Library + Playwright |

### 인프라
| 항목 | 기술 |
|------|------|
| Frontend 배포 | Vercel |
| Backend 배포 | AWS ECS Fargate |
| Database | AWS RDS PostgreSQL (Multi-AZ) |
| Cache | AWS ElastiCache Redis |
| CDN | AWS CloudFront |
| CI/CD | GitHub Actions |
| Container | Docker |

---

## 📁 디렉토리 구조

### 백엔드 (todolist-backend)
```
src/main/java/com/todolist/
├── TodolistApplication.java
├── config/
│   ├── SwaggerConfig.java
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   └── CorsConfig.java
├── controller/
│   ├── AuthController.java
│   ├── TodoController.java
│   ├── CategoryController.java
│   └── StatisticsController.java
├── service/
│   ├── AuthService.java
│   ├── TodoService.java
│   ├── CategoryService.java
│   ├── JwtService.java
│   └── StatisticsService.java
├── repository/
│   ├── UserRepository.java
│   ├── TodoRepository.java
│   ├── CategoryRepository.java
│   ├── RefreshTokenRepository.java
│   └── ActivityLogRepository.java
├── domain/
│   ├── User.java
│   ├── Todo.java
│   ├── Category.java
│   ├── Tag.java
│   ├── TodoTag.java
│   ├── RefreshToken.java
│   └── ActivityLog.java
├── dto/
│   ├── request/
│   └── response/
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ErrorCode.java
│   ├── ErrorResponse.java
│   └── [CustomExceptions].java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── util/

src/main/resources/
├── application.yml
├── application-local.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/
    ├── V1__initial_schema.sql
    ├── V2__create_triggers.sql
    ├── V3__create_views_and_functions.sql
    └── V4__create_app_user.sql
```

### 프론트엔드 (todolist-frontend)
```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/page.tsx
│   │   └── signup/page.tsx
│   ├── (dashboard)/
│   │   ├── layout.tsx
│   │   ├── dashboard/page.tsx
│   │   ├── dashboard/trash/page.tsx
│   │   └── category/[id]/page.tsx
│   ├── layout.tsx
│   └── page.tsx
├── components/
│   ├── layout/
│   │   ├── AppLayout.tsx
│   │   ├── Header.tsx
│   │   └── Sidebar.tsx
│   ├── todo/
│   │   ├── TodoList.tsx
│   │   ├── TodoItem.tsx
│   │   ├── TodoForm.tsx
│   │   └── TodoFilters.tsx
│   ├── category/
│   │   ├── CategoryList.tsx
│   │   └── CategoryForm.tsx
│   └── ui/ (shadcn/ui)
├── hooks/
├── store/
│   ├── authStore.ts
│   └── uiStore.ts
├── api/
│   ├── axios-instance.ts
│   ├── auth.ts
│   ├── todos.ts
│   └── categories.ts
├── types/
│   └── index.ts
└── lib/
    └── utils.ts
```

---

## ✏️ 코딩 컨벤션

### 절대 규칙: 모든 코드는 camelCase를 기반으로 작성

### 백엔드 (Java)

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스/인터페이스 | PascalCase | `TodoService`, `UserRepository` |
| 메서드/변수 | lowerCamelCase | `createTodo`, `userId`, `isCompleted` |
| 상수 | UPPER_SNAKE_CASE | `JWT_SECRET_KEY`, `MAX_TITLE_LENGTH` |
| 패키지 | 소문자 | `com.todolist.service` |
| DB 컬럼 매핑 | `@Column(name = "snake_case")` | `@Column(name = "user_id")` |

```java
// 올바른 예시
@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 편의 메서드: lowerCamelCase
    public void softDelete() {
        this.isDeleted = true;
    }
}

// 올바른 서비스 메서드
public TodoResponse createTodo(Long userId, TodoCreateRequest requestDto) {
    boolean isDuplicate = todoRepository.existsByTitle(requestDto.getTitle());
    LocalDateTime dueDate = requestDto.getDueDate();
}
```

### 프론트엔드 (TypeScript)

| 대상 | 규칙 | 예시 |
|------|------|------|
| 컴포넌트/타입/인터페이스 | PascalCase | `TodoItem`, `TodoData`, `ApiResponse` |
| 함수/변수/훅 | lowerCamelCase | `fetchTodoList`, `isCompleted`, `useTodos` |
| 상수 | UPPER_SNAKE_CASE | `API_BASE_URL`, `DEFAULT_PAGE_SIZE` |
| 파일명 (컴포넌트) | PascalCase | `TodoItem.tsx` |
| 파일명 (유틸/훅) | camelCase | `useTodos.ts`, `formatDate.ts` |
| CSS 클래스 | Tailwind 유틸리티 클래스 | `bg-red-100 text-red-700` |

```typescript
// 올바른 예시
interface TodoData {
  todoId: number;
  userId: number;
  isCompleted: boolean;
  createdAt: string;
  dueDate: string | null;
}

const fetchTodoList = async (userId: number): Promise<TodoData[]> => {
  const response = await apiClient.get(`/api/v1/todos`);
  return response.data.data;
};

export default function TodoItem({ todo, onToggle }: TodoItemProps) {
  const isOverdue = todo.dueDate && new Date(todo.dueDate) < new Date();
}
```

### Database

| 대상 | 규칙 |
|------|------|
| 테이블명 | snake_case (복수형) |
| 컬럼명 | snake_case |
| 인덱스명 | `idx_{테이블}_{컬럼}` |
| 제약조건명 | `chk_{테이블}_{설명}`, `uq_{테이블}_{컬럼}` |

### API

| 대상 | 규칙 | 예시 |
|------|------|------|
| URL Path | kebab-case | `/api/v1/todos`, `/api/v1/user-profile` |
| Query Parameter | camelCase | `?sortBy=createdAt&sortOrder=desc` |
| Request/Response Body | camelCase | `{"userId": 1, "isCompleted": true}` |
| API 버전 | `/api/v1/` | `/api/v1/todos` |

### Git Commit

```
feat(todo): add soft delete functionality
fix(auth): resolve jwt token expiration issue
refactor(service): optimize todo query performance
test(todo): add unit tests for TodoService
docs(api): update swagger documentation
chore(ci): add github actions workflow
```

---

## 🔒 보안 전략

### 절대 원칙 (위반 금지)

1. **비밀번호는 반드시 BCrypt 해싱** (강도 10-12), 평문 저장 절대 금지
2. **JWT Secret은 환경 변수에서만 읽기**, 코드에 하드코딩 절대 금지
3. **모든 API에서 소유권 검증**: 요청한 사용자가 해당 리소스의 소유자인지 확인
4. **서버 측 입력 검증 필수**: 클라이언트 검증만 믿지 말 것
5. **로그에 민감 정보 절대 기록 금지**: 비밀번호, 토큰, 개인정보

### JWT 토큰 관리
- **Access Token**: 1시간 유효, Authorization 헤더 전송
- **Refresh Token**: 7일 유효, HttpOnly Cookie, DB 저장
- **Token Rotation**: Refresh 시 새 토큰 발급, 이전 토큰 폐기
- **Blacklist**: 로그아웃 시 Access Token을 Redis에 저장 (만료 시까지)

### Brute Force 방어
- 로그인 실패 5회 시 15분 계정 잠금 (`locked_until` 컬럼)
- 로그인 성공 시 `failed_login_attempts` 자동 초기화 (트리거)
- IP 기반 Rate Limiting: Bucket4j + Redis

### Rate Limiting (계층별)
```
로그인 시도: 5회 / 15분 (IP 기준)
회원가입: 3회 / 시간 (IP 기준)
Todo CRUD: 100회 / 분 (사용자 기준)
파일 업로드: 10회 / 시간 (사용자 기준)
```

### 보안 헤더 (모든 응답에 적용)
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

### CORS 설정
```yaml
allowed-origins:
  - http://localhost:3000        # 로컬
  - https://todolist.vercel.app  # Vercel
  - https://yourdomain.com       # 운영 도메인
allowed-methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
allow-credentials: true
```

### SQL Injection 방어
- JPA / JPQL 파라미터 바인딩 필수
- Native Query 최소화, 불가피 시 PreparedStatement
- 사용자 입력을 직접 쿼리에 삽입 절대 금지

---

## 🗄️ 데이터베이스 스키마

### 핵심 원칙
- **컬럼명**: snake_case (PostgreSQL 관례)
- **JPA 매핑**: `@Column(name = "snake_case")`로 camelCase ↔ snake_case 변환

### 주요 테이블 요약

#### users
```sql
user_id, email (UNIQUE), password_hash (BCrypt),
username, profile_image_url, is_active,
email_verified (DEFAULT FALSE),    -- 이메일 인증
failed_login_attempts (DEFAULT 0), -- Brute Force 방어
locked_until,                      -- 계정 잠금
created_at, updated_at, last_login_at
```

#### todos
```sql
todo_id, user_id (FK), category_id (FK),
title (NOT NULL, max 200), description (max 5000),
priority (ENUM: LOW/MEDIUM/HIGH, DEFAULT MEDIUM),
status (ENUM: TODO/IN_PROGRESS/COMPLETED, DEFAULT TODO),
due_date, reminder_at, is_important,
is_deleted (DEFAULT FALSE),  -- 소프트 삭제 핵심 필드
display_order,
created_at, updated_at, completed_at,
deleted_at                   -- 휴지통 이동 시간
```

#### categories
```sql
category_id, user_id (FK), name,
color_code (DEFAULT '#3B82F6', HEX 형식),
icon, display_order,
created_at, updated_at
UNIQUE(user_id, name)
```

#### refresh_tokens
```sql
token_id, user_id (FK), token (UNIQUE),
expires_at, is_revoked (DEFAULT FALSE),
ip_address,     -- 보안 추적
user_agent,     -- 보안 추적
last_used_at,
created_at
```

#### activity_logs (월별 파티셔닝)
```sql
log_id, user_id (FK), todo_id (FK),
activity_type (ENUM: CREATE/UPDATE/DELETE/COMPLETE/REOPEN),
description, metadata (JSONB),
ip_address,   -- 보안 추적
user_agent,   -- 보안 추적
created_at    -- 파티셔닝 키
```

### 인덱스 전략 (필수 준수)
```sql
-- 소프트 삭제가 있는 경우 반드시 부분 인덱스 사용
CREATE INDEX idx_todos_user_id ON todos(user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_todos_user_status ON todos(user_id, status) WHERE is_deleted = FALSE;

-- 전문 검색
CREATE INDEX idx_todos_search ON todos
  USING gin(to_tsvector('english', title || ' ' || COALESCE(description, '')))
  WHERE is_deleted = FALSE;
```

### Flyway 마이그레이션 규칙
```
형식: V{번호}__{설명}.sql
예시: V1__initial_schema.sql
     V2__create_triggers.sql
     V5__add_oauth_columns.sql

규칙:
- 한 번 적용된 파일은 절대 수정 금지
- 새 변경 사항은 반드시 새 버전으로 생성
- 멱등성 보장: IF NOT EXISTS 사용
- 실행 후 스키마 검증 필수
```

---

## 🌐 API 설계 표준

### 버전 관리
- 모든 API: `/api/v1/` 프리픽스 사용

### 성공 응답 형식
```json
{
  "success": true,
  "data": { },
  "timestamp": "2026-02-17T10:30:00Z"
}
```

### 페이징 응답 형식
```json
{
  "success": true,
  "data": {
    "content": [],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2026-02-17T10:30:00Z"
}
```

### 에러 응답 형식 (단건)
```json
{
  "success": false,
  "errorCode": "TODO_NOT_FOUND",
  "message": "요청한 할 일을 찾을 수 없습니다",
  "timestamp": "2026-02-17T10:30:00Z",
  "path": "/api/v1/todos/999"
}
```

### 에러 응답 형식 (유효성 검증)
```json
{
  "success": false,
  "errorCode": "COMMON_INVALID_PARAMETER",
  "message": "유효하지 않은 요청 파라미터입니다",
  "errors": [
    { "field": "title", "message": "제목은 필수입니다", "rejectedValue": null },
    { "field": "priority", "message": "유효하지 않은 우선순위입니다", "rejectedValue": "URGENT" }
  ],
  "timestamp": "2026-02-17T10:30:00Z",
  "path": "/api/v1/todos"
}
```

### 에러 코드 체계
```java
// 인증 (AUTH)
AUTH_INVALID_CREDENTIALS  // 이메일 또는 비밀번호가 올바르지 않습니다
AUTH_TOKEN_EXPIRED        // 토큰이 만료되었습니다
AUTH_TOKEN_INVALID        // 유효하지 않은 토큰입니다
AUTH_ACCOUNT_LOCKED       // 계정이 잠겨있습니다
AUTH_EMAIL_NOT_VERIFIED   // 이메일 인증이 필요합니다

// 사용자 (USER)
USER_NOT_FOUND            // 사용자를 찾을 수 없습니다
USER_EMAIL_DUPLICATE      // 이미 사용 중인 이메일입니다
USER_INVALID_PASSWORD     // 비밀번호가 정책을 만족하지 않습니다

// Todo (TODO)
TODO_NOT_FOUND            // 요청한 할 일을 찾을 수 없습니다
TODO_FORBIDDEN            // 이 할 일에 접근할 권한이 없습니다
TODO_ALREADY_DELETED      // 이미 삭제된 할 일입니다

// 카테고리 (CATEGORY)
CATEGORY_NOT_FOUND        // 카테고리를 찾을 수 없습니다
CATEGORY_NAME_DUPLICATE   // 이미 존재하는 카테고리 이름입니다

// 공통 (COMMON)
COMMON_INVALID_PARAMETER  // 유효하지 않은 요청 파라미터입니다
COMMON_INTERNAL_ERROR     // 서버 내부 오류가 발생했습니다
COMMON_RATE_LIMIT_EXCEEDED // 요청 한도를 초과했습니다
```

### HTTP 상태 코드
```
200 OK               - 조회, 수정 성공
201 Created          - 생성 성공
204 No Content       - 삭제 성공 (응답 본문 없음)
400 Bad Request      - 유효성 검증 실패
401 Unauthorized     - 인증 실패 (토큰 없음/만료)
403 Forbidden        - 권한 없음 (다른 사용자 리소스 접근)
404 Not Found        - 리소스 없음
409 Conflict         - 충돌 (중복 등)
429 Too Many Requests - Rate Limit 초과
500 Internal Server Error - 서버 에러
```

### 엔드포인트 목록
```
[Auth]
POST   /api/v1/auth/signup
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
GET    /api/v1/auth/me
PUT    /api/v1/auth/profile
PUT    /api/v1/auth/password
DELETE /api/v1/auth/account

[Todo]
GET    /api/v1/todos                  (page, size, status, priority, category, keyword, sortBy, sortOrder, dueFrom, dueTo)
POST   /api/v1/todos
GET    /api/v1/todos/{id}
PUT    /api/v1/todos/{id}
DELETE /api/v1/todos/{id}             (소프트 삭제)
PATCH  /api/v1/todos/{id}/toggle
PATCH  /api/v1/todos/{id}/restore
DELETE /api/v1/todos/{id}/permanent
GET    /api/v1/todos/trash
DELETE /api/v1/todos/trash/empty

[Category]
GET    /api/v1/categories
POST   /api/v1/categories
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
PUT    /api/v1/categories/reorder

[Statistics]
GET    /api/v1/statistics/dashboard
GET    /api/v1/statistics/trends
```

---

## 🎨 프론트엔드 설계

### 디자인 시스템

**색상 팔레트**
```
Primary Action: blue-600
완료 상태: green-600
Medium 우선순위: yellow-600
High 우선순위 / 에러: red-600
배경: gray-50 (라이트) / gray-900 (다크)
```

**우선순위 배지 스타일**
```typescript
const priorityStyles = {
  HIGH:   'bg-red-100 text-red-700 border border-red-300',
  MEDIUM: 'bg-yellow-100 text-yellow-700 border border-yellow-300',
  LOW:    'bg-green-100 text-green-700 border border-green-300',
}
```

**마감일 표시 스타일**
```typescript
const getDueDateStyle = (dueDate: string) => {
  const today = new Date();
  const due = new Date(dueDate);
  if (due < today) return 'text-red-600 font-medium';        // 기한 초과
  if (isSameDay(due, today)) return 'text-red-500';          // 오늘
  if (isSameDay(due, addDays(today, 1))) return 'text-orange-500'; // 내일
  return 'text-gray-500';                                     // 미래
}
```

### 상태 관리 규칙

**서버 상태 (TanStack Query)**
- API 데이터는 모두 TanStack Query로 관리
- Query Keys: 계층적 구조 사용
- Optimistic Update: 생성/수정/삭제/완료 토글에 필수 적용
- staleTime: 30초

**클라이언트 상태 (Zustand)**
- UI 상태만 관리 (사이드바, 테마, 모달 상태)
- API 데이터를 Zustand에 중복 저장 금지

### Optimistic Update 패턴 (필수)
```typescript
// Todo 완료 토글 예시
const toggleMutation = useMutation({
  mutationFn: (todoId: number) => toggleTodo(todoId),
  onMutate: async (todoId) => {
    // 1. 진행 중인 쿼리 취소
    await queryClient.cancelQueries({ queryKey: todoKeys.lists() });
    // 2. 이전 상태 저장
    const previousTodos = queryClient.getQueryData(todoKeys.lists());
    // 3. 즉시 UI 업데이트
    queryClient.setQueryData(todoKeys.lists(), (old) =>
      old?.map(todo => todo.todoId === todoId
        ? { ...todo, isCompleted: !todo.isCompleted }
        : todo
      )
    );
    return { previousTodos };
  },
  onError: (err, todoId, context) => {
    // 4. 실패 시 롤백
    queryClient.setQueryData(todoKeys.lists(), context?.previousTodos);
    toast.error('변경에 실패했습니다');
  },
  onSettled: () => {
    // 5. 성공/실패 무관 쿼리 재검증
    queryClient.invalidateQueries({ queryKey: todoKeys.lists() });
  },
});
```

### 에러 처리 패턴 (Axios Interceptor)
```typescript
// 에러 코드 → 사용자 메시지 매핑 적용
// COMMON_RATE_LIMIT_EXCEEDED → Toast 경고
// AUTH_TOKEN_EXPIRED → 자동 토큰 갱신 후 재시도
// AUTH_ACCOUNT_LOCKED → /login 페이지로 이동
// 나머지 → 에러 코드 기반 메시지 Toast
```

---

## ⚡ 성능 최적화

### 백엔드

1. **N+1 쿼리 방지**: `@EntityGraph` 또는 Fetch Join 사용
2. **불필요한 SELECT 방지**: DTO Projection 사용 (전체 Entity 로드 지양)
3. **캐싱 전략**: Redis `@Cacheable` 활용
   - 카테고리 목록: TTL 10분
   - 통계 데이터: TTL 5분
4. **페이징**: 기본 20개, Offset 방식 (대용량 시 Cursor 고려)
5. **DB 커넥션 풀**: HikariCP `maximumPoolSize: 10`

### 프론트엔드

1. **Dynamic Import**: 페이지/모달 컴포넌트는 `next/dynamic` 사용
2. **이미지 최적화**: `next/image` 컴포넌트 필수 사용
3. **리렌더링 최소화**: `React.memo`, `useMemo`, `useCallback` 적절히 사용
4. **검색 최적화**: 300ms Debounce 적용
5. **Virtual Scroll**: 100개 이상 리스트에 적용 고려

---

## 🧪 테스트 전략

### 테스트 피라미드
```
E2E (10%):         핵심 사용자 플로우만 (로그인, Todo CRUD)
Integration (20%): API 엔드포인트, DB 통합
Unit (70%):        Service 비즈니스 로직
```

### 백엔드 테스트 규칙

```java
// 1. 테스트 메서드명: "메서드명_상황_예상결과" 패턴
@Test
@DisplayName("Todo 생성 - 성공")
void createTodo_WhenValidRequest_ShouldReturnTodoResponse() { }

@Test
@DisplayName("Todo 조회 - 다른 사용자 접근 시 403 예외")
void getTodo_WhenAccessedByOtherUser_ShouldThrowForbiddenException() { }

// 2. Given-When-Then 구조 필수
// 3. 각 테스트는 독립적으로 실행 가능해야 함
// 4. Mock 객체: Mockito @Mock, @InjectMocks 사용
// 5. 통합 테스트: Testcontainers PostgreSQL 사용
```

### 프론트엔드 테스트 규칙

```typescript
// 1. 컴포넌트 테스트: render + 사용자 상호작용 + 기대 결과
it('체크박스 클릭 시 onToggle이 호출된다', () => {
  const mockToggle = jest.fn();
  render(<TodoItem todo={mockTodo} onToggle={mockToggle} />);
  fireEvent.click(screen.getByRole('checkbox'));
  expect(mockToggle).toHaveBeenCalledWith(mockTodo.todoId);
});

// 2. API 호출 테스트: MSW로 Mock 처리
// 3. async 테스트: waitFor, findBy 사용
```

### 커버리지 목표
```
백엔드 Service Layer: 80% 이상
백엔드 전체: 70% 이상
프론트엔드 컴포넌트: 70% 이상
```

---

## 🔄 CI/CD

### 브랜치 전략 (GitHub Flow)
```
main      → 운영 (태그 기반 배포, 수동 승인)
develop   → 개발 (자동 배포)
feature/* → 기능 개발 (PR → develop)
hotfix/*  → 긴급 수정 (PR → main + develop)
```

### PR 규칙
- `feature/* → develop`: 2명 이상 리뷰 필수
- `develop → main`: 모든 테스트 통과 + 배포 승인 필요
- PR 템플릿 사용 필수

### 배포 전략
- **운영 (main)**: Blue-Green 배포
- **개발 (develop)**: Rolling 배포
- 배포 후 Health Check: `/actuator/health` 응답 확인

### GitHub Actions 워크플로우
```
PR 생성 → Lint + Type Check + Unit Test
develop push → 위 + Integration Test + Dev 배포
main push → 위 + E2E Test + 수동 승인 + Prod 배포
```

---

## 🌍 환경 변수

### 백엔드 환경 변수 (application.yml 구조)
```yaml
# 공통
spring.application.name: todolist-api

# 환경별 분리
# application-local.yml: 로컬 DB, Debug 로그
# application-dev.yml: 개발 서버
# application-prod.yml: 환경 변수에서 읽기 (${ })
```

### 백엔드 필수 환경 변수
```
DB_URL=jdbc:postgresql://host:5432/todolist_db
DB_USERNAME=todolist_app
DB_PASSWORD=<strong_password>
REDIS_HOST=<redis_host>
REDIS_PORT=6379
JWT_SECRET=<256bit_minimum_secret>
JWT_ACCESS_VALIDITY=3600000
JWT_REFRESH_VALIDITY=604800000
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### 프론트엔드 필수 환경 변수 (.env.example)
```
NEXT_PUBLIC_API_URL=https://api.todolist.com
NEXT_PUBLIC_API_TIMEOUT=10000
```

### 환경 변수 보안 규칙
- `.env`, `.env.local`, `.env.production` 파일: `.gitignore`에 반드시 포함
- `.env.example`: 실제 값 없이 키만 포함, Git 커밋 필수
- 운영 환경: AWS Secrets Manager 사용
- 코드에 하드코딩 절대 금지

---

## 🗃️ DB 마이그레이션

### Flyway 사용 원칙
1. 적용된 파일 절대 수정 금지
2. 새 변경사항은 새 버전 파일로 생성
3. 멱등성 보장 (IF NOT EXISTS)
4. 테스트 환경에서 먼저 검증
5. 운영 적용 전 Staging 검증 필수

### 마이그레이션 파일 위치
```
src/main/resources/db/migration/V{N}__{description}.sql
```

---

## 📖 API 문서화

### Swagger 어노테이션 필수 항목
```java
// Controller 레벨
@Tag(name = "Todo", description = "할 일 관리 API")

// 메서드 레벨
@Operation(summary = "Todo 목록 조회", description = "...")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공"),
    @ApiResponse(responseCode = "401", description = "인증 실패"),
})
@SecurityRequirement(name = "bearerAuth")

// DTO 레벨
@Schema(description = "Todo 생성 요청")
// 각 필드에 @Schema(description = "...", example = "...")
```

### Swagger UI 접근
- 로컬: `http://localhost:8080/swagger-ui/index.html`
- 개발: `https://dev-api.todolist.com/swagger-ui/index.html`
- **운영 환경: Swagger UI 비활성화** (보안)

---

## ✅ 코드 작성 전 체크리스트

코드를 작성하기 전에 다음을 반드시 확인하세요:

### 모든 코드
- [ ] camelCase 명명 규칙 준수 (DB는 snake_case 유지)
- [ ] 민감 정보 (비밀번호, 토큰) 로그 기록 없음
- [ ] 환경 변수는 하드코딩 없이 파일에서 읽음

### 백엔드 코드
- [ ] 모든 입력값 `@Valid` + DTO 검증 어노테이션
- [ ] 리소스 접근 시 userId 소유권 확인
- [ ] 에러 발생 시 ErrorCode Enum 사용
- [ ] API에 Swagger 어노테이션 추가
- [ ] N+1 쿼리 발생 여부 확인

### 프론트엔드 코드
- [ ] API 호출은 TanStack Query 사용
- [ ] 변경 작업에 Optimistic Update 적용
- [ ] 에러 상황 Toast 안내 구현
- [ ] 로딩 상태 표시 구현
- [ ] 반응형/다크모드 동작 확인

### 테스트
- [ ] 새 기능에 단위 테스트 작성
- [ ] Given-When-Then 구조 준수
- [ ] 비정상 케이스 (예외, 권한) 테스트 포함

---

## 🚨 절대 하지 말 것

1. **보안**
   - 비밀번호 평문 저장 또는 로그 기록
   - JWT Secret 코드 하드코딩
   - 소유권 검증 없이 리소스 반환
   - `dangerouslySetInnerHTML` 사용
   - 운영 DB에서 직접 테스트

2. **코딩 컨벤션**
   - snake_case 변수명 (DB 컬럼 제외)
   - Context API로 서버 상태 관리
   - `SELECT *` 쿼리 사용
   - 적용된 Flyway 파일 수정

3. **아키텍처**
   - Controller에 비즈니스 로직 작성
   - Service에 HTTP 관련 코드 작성
   - 순환 의존성 생성
   - API 버전 없이 엔드포인트 생성 (/api/ 직접 사용)

---

## 📞 개발 중 참고사항

### 주요 포트
```
프론트엔드 (로컬): 3000
백엔드 (로컬): 8080
PostgreSQL (로컬): 5432
Redis (로컬): 6379
```

### 로컬 실행 방법
```bash
# 1. 인프라 시작
docker-compose up -d

# 2. 백엔드 시작
cd todolist-backend
./gradlew bootRun --args='--spring.profiles.active=local'

# 3. 프론트엔드 시작
cd todolist-frontend
npm run dev
```

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### 테스트 실행
```bash
# 백엔드 단위 테스트
./gradlew test

# 백엔드 통합 테스트 (Docker 필요)
./gradlew integrationTest

# 프론트엔드 테스트
npm run test

# 프론트엔드 커버리지
npm run test:coverage
```

---

## 📚 프로젝트 문서

문서는 프로젝트별로 분리되어 있습니다.

### 전체 프로젝트 문서

#### 1. [README.md](./README.md) ⭐
- **목적**: 프로젝트 개요 및 빠른 시작 가이드
- **내용**: 기술 스택, 주요 기능, 실행 방법, 프로젝트 구조

#### 2. [PROJECT_STATUS.md](./docs/PROJECT_STATUS.md) ⭐ **필독**
- **목적**: CLAUDE.md 기준 현재 구현 상태 분석
- **내용**: 전체 진행률 (약 80%), 구현 완료 항목, 다음 스텝
- **업데이트**: 2026-02-18 18:30
- **활용**: 새로운 기능 구현 전 현재 상태 파악

#### 3. [STACK_VERIFICATION_REPORT.md](./docs/STACK_VERIFICATION_REPORT.md)
- **목적**: DB → Backend → Frontend 전체 스택 연결 검증
- **내용**: PostgreSQL, Backend API, Frontend 통합 테스트 결과
- **활용**: 스택 간 연동 이슈 디버깅

### Backend 문서

#### 1. [Backend/README.md](./Backend/README.md) ⭐
- **목적**: 백엔드 실행 가이드
- **내용**: 환경 설정, 실행 방법, API 엔드포인트, 기술 스택

#### 2. [FLYWAY_MIGRATION_GUIDE.md](./Backend/docs/FLYWAY_MIGRATION_GUIDE.md)
- **목적**: Flyway 마이그레이션 가이드
- **내용**: 마이그레이션 파일 구조, 적용 방법, 검증, 문제 해결
- **활용**: DB 스키마 변경 시 참조

#### 3. [TEST_GUIDE.md](./Backend/docs/TEST_GUIDE.md)
- **목적**: Backend 테스트 가이드
- **내용**: 테스트 케이스 목록, 실행 방법, 설정
- **활용**: 테스트 작성 및 실행 가이드

### Frontend 문서

#### 1. [frontend/README.md](./frontend/README.md) ⭐
- **목적**: 프론트엔드 실행 가이드
- **내용**: 환경 설정, 실행 방법, 디렉토리 구조, 주요 기능

#### 2. [IMPLEMENTATION_SUMMARY.md](./frontend/docs/IMPLEMENTATION_SUMMARY.md)
- **목적**: Frontend 구현 완료 항목 상세 목록
- **내용**: Phase별 작업, API 연동, Hooks, 컴포넌트 목록
- **활용**: Frontend 아키텍처 및 구현 패턴 참조

### 문서 활용 가이드

#### 새 기능 구현 전
1. **PROJECT_STATUS.md** 읽고 현재 진행 상태 확인
2. 해당 기능이 "미구현 항목"에 있는지 확인
3. 우선순위 및 의존성 파악

#### 에러 발생 시
1. **STACK_VERIFICATION_REPORT.md**에서 관련 레이어 확인
2. DB/Backend/Frontend 중 어느 레이어에서 문제인지 판단
3. 해당 레이어의 테스트 또는 로그 확인

#### 아키텍처 참조 시
1. **Backend**: Backend/README.md 및 Backend/docs/ 확인
2. **Frontend**: frontend/README.md 및 frontend/docs/ 확인
3. 기존 코드와 일관성 유지
4. CLAUDE.md 코딩 컨벤션 준수

### 문서 업데이트 규칙

- **PROJECT_STATUS.md**: 주요 기능 완료 시 또는 주 1회
- **Backend/docs/**: Backend 관련 변경 시 (마이그레이션, 테스트)
- **frontend/docs/**: Frontend 새 컴포넌트/Hook 추가 시
- **STACK_VERIFICATION_REPORT.md**: 스택 변경 또는 배포 전

---

## 🎯 현재 프로젝트 상태

> 최종 업데이트: 2026-02-19 14:00

### ✅ 최근 완료 작업 (2026-02-19)

#### Docker 로컬 배포 환경 구축 완료 ⭐⭐⭐ (14:00 완료)
**달성 내용**
- docker-compose.yml 작성 (nginx, backend, frontend, postgres, redis 5개 서비스)
- Backend/Frontend Dockerfile 작성 (Multi-stage build)
- nginx.conf 리버스 프록시 설정 (OAuth2 경로 추가)
- .env.example 환경 변수 템플릿
- DOCKER_SETUP.md 배포 가이드 작성

**해결한 주요 문제들**
1. **Flyway 미실행 문제**
   - DataSource URL currentSchema 파라미터 제거 (Flyway 실행 보장)
   - FlywayConfig.java 수동 Bean 등록 (Spring Boot 4.x 호환)
   - FlywayMigrationStrategy 패키지 변경 대응
   - V7 마이그레이션 추가 (약관 동의 컬럼)

2. **JWT 인증 보안 강화**
   - RefreshToken last_used_at 기본값 설정 (NOT NULL 제약 해결)
   - SessionCreationPolicy.STATELESS 변경 (익명 세션 우회 차단)
   - authenticationEntryPoint 추가 (Google 리다이렉트 방지, 401 JSON 반환)
   - 로그아웃 토큰 블랙리스트 구현 (Redis blacklist:*)

3. **nginx OAuth2 경로 프록시**
   - /login/oauth2/ → Backend (OAuth2 콜백)
   - /oauth2/ → Backend (OAuth2 시작)
   - Google OAuth2 정상 작동 확인

**기술적 성과**
- Docker 로컬 환경 완전 구축
- 보안 취약점 3건 해결
- Flyway 마이그레이션 안정화
- OAuth2 소셜 로그인 통합 완료

---

### ✅ 이전 완료 작업 (2026-02-18~19)

#### Flyway 환경변수 명시적 설정 추가 ⭐⭐⭐ (23:30 완료)
**문제 지속**
- application-prod.yml 수정 후에도 Flyway 실행 안 됨
- 컨테이너 환경변수 확인: Flyway 관련 환경변수 없음
- `SPRING_PROFILES_ACTIVE=prod` 설정은 있음

**근본 원인**
- Docker 환경에서 환경변수가 application-prod.yml보다 우선 적용됨
- docker-compose.yml에 DataSource 환경변수만 있고 Flyway 환경변수 없음
- Spring Boot가 Flyway 설정을 application-prod.yml에서 읽지 못함
- 또는 환경변수와 YAML 설정 간 충돌

**해결 방법**
- docker-compose.yml의 backend 서비스에 Flyway 환경변수 명시적 추가
- 모든 Flyway 설정을 환경변수로 오버라이드
- application-prod.yml과 동일한 값 사용

**추가된 환경변수**
```yaml
# docker-compose.yml - backend 서비스
environment:
  - SPRING_FLYWAY_ENABLED=true
  - SPRING_FLYWAY_LOCATIONS=classpath:db/migration
  - SPRING_FLYWAY_SCHEMAS=todolist_db
  - SPRING_FLYWAY_DEFAULT_SCHEMA=todolist_db
  - SPRING_FLYWAY_CREATE_SCHEMAS=true
  - SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
  - SPRING_FLYWAY_BASELINE_VERSION=0
```

**Spring Boot 환경변수 우선순위**
1. 명령행 인자 (--spring.flyway.enabled=true)
2. 환경변수 (SPRING_FLYWAY_ENABLED=true) ← docker-compose
3. application.yml / application-prod.yml
4. 기본값

**수정된 파일**
- `docker-compose.yml`
  - backend 서비스 environment에 Flyway 설정 7개 추가

**효과**
- Flyway 설정이 확실히 적용됨
- 환경변수 우선순위로 인한 충돌 해결
- 컨테이너에서 env 확인 시 Flyway 설정 보임

#### Flyway 실행 안 되는 문제 해결 ⭐⭐⭐ (23:15 완료)
**문제 진단**
- Flyway가 전혀 실행되지 않음 (로그 한 줄도 없음)
- `spring.flyway.enabled=true` 설정 있음에도 불구하고 동작하지 않음

**원인 분석**
1. ✅ TodolistApplication.java - exclude 옵션 없음 (정상)
2. ✅ application-prod.yml - 들여쓰기 올바름 (정상)
3. ✅ build.gradle - Flyway 의존성 정상
4. ✅ 커스텀 Flyway 설정 없음
5. ❌ **DataSource URL에 문제 발견**
   - `jdbc:postgresql://postgres:5432/todolist_db?currentSchema=todolist_db`
   - `currentSchema` 파라미터가 Flyway 실행 방해 가능성

**근본 원인**
- `currentSchema=todolist_db` 파라미터 사용 시
- Flyway가 스키마를 찾으려고 하지만 아직 생성되지 않음
- DataSource 연결은 성공하지만 Flyway가 건너뛰어짐
- `create-schemas: true` 설정이 있어도 currentSchema 우선 적용

**해결 방법**
- DataSource URL에서 `currentSchema` 파라미터 제거
- Flyway가 `default-schema: todolist_db` 설정으로 스키마 생성
- 마이그레이션 완료 후 Hibernate가 스키마 사용

**수정된 파일**
- `Backend/src/main/resources/application-prod.yml`
  - `url: jdbc:postgresql://postgres:5432/todolist_db?currentSchema=todolist_db`
  - → `url: jdbc:postgresql://postgres:5432/todolist_db`

**기술적 배경**
- PostgreSQL 연결 시 `currentSchema`는 **기존 스키마 지정용**
- Flyway는 스키마가 없으면 생성하려고 하지만, currentSchema가 있으면 혼란
- Flyway의 `default-schema` 설정으로 충분

**효과**
- Flyway가 정상 실행
- 스키마 자동 생성 (V1 마이그레이션)
- 테이블 생성 (V3 마이그레이션)
- Hibernate가 스키마 인식

#### Docker 빌드 검증 로직 추가 ⭐ (23:00 완료)
**문제 지속**
- Dockerfile 수정 후에도 JAR에 SQL 파일 미포함
- `find / -name 'V1__*.sql'` → 결과 없음

**진단 강화**
- JAR 빌드 후 즉시 내용 검증하는 로직 추가
- SQL 파일이 JAR에 포함되지 않으면 빌드 실패하도록 설정

**추가된 검증 로직**
```dockerfile
# JAR 내부 SQL 파일 확인 (빌드 검증)
RUN echo "=== Checking JAR contents ===" && \
    ls -lh build/libs/*.jar && \
    jar -tf build/libs/*.jar | grep "db/migration" || \
    (echo "ERROR: SQL files NOT found in JAR!" && exit 1)
```

**검증 내용**
- JAR 파일 크기 확인
- JAR 내부 `db/migration` 경로 검색
- SQL 파일 없으면 빌드 실패 (exit 1)

**프로젝트 구조 검증 완료**
- ✅ SQL 파일 위치: `Backend/src/main/resources/db/migration/`
- ✅ V1~V7 마이그레이션 파일 존재 (8개 파일)
- ✅ Dockerfile COPY 순서 올바름: `COPY . .` → `bootJar`

**다음 단계**
- 재빌드 시 검증 로그 확인
- 빌드 실패 시 에러 메시지로 원인 파악
- Gradle 설정 또는 `.dockerignore` 추가 점검

#### Docker 환경 Flyway 마이그레이션 파일 누락 해결 ⭐⭐ (22:45 완료)
**문제 진단**
- Docker 환경에서 Flyway 마이그레이션이 실행되지 않음
- 에러: `relation "todolist_db.users" does not exist`
- 컨테이너에서 SQL 파일 찾을 수 없음: `find / -name '*.sql'` → 없음
- Flyway 로그 전혀 없음

**근본 원인**
- Dockerfile의 `COPY src src` 방식
- 빌드 캐시 문제로 일부 파일이 JAR에 포함되지 않을 가능성

**해결 방법**
1. **Dockerfile 수정**: `COPY src src` → `COPY . .`
   - 전체 소스 컨텍스트를 복사하여 누락 방지
   - `.dockerignore`로 불필요한 파일 제외
   - `src/main/resources/db/migration` 확실히 포함

2. **Flyway 로그 활성화** (application-prod.yml)
   - `org.flywaydb: INFO`
   - `org.springframework.boot.autoconfigure: INFO`

**수정된 파일**
- `Backend/Dockerfile`
  - `COPY src src` → `COPY . .` (전체 복사)
  - 주석 추가: Spring Boot JAR에 resources 포함 명시
- `Backend/src/main/resources/application-prod.yml`
  - logging.level에 Flyway 로그 추가

**검증 방법**
```bash
# 재빌드 (캐시 제거)
docker-compose build --no-cache backend

# JAR 내부 SQL 파일 확인
docker-compose exec backend sh -c "jar -tf app.jar | grep db/migration"
# 예상 출력:
# BOOT-INF/classes/db/migration/V1__create_schema_and_types.sql
# BOOT-INF/classes/db/migration/V2__create_functions.sql
# ...
```

**효과**
- 마이그레이션 파일 JAR에 포함 보장
- Flyway 실행 및 테이블 생성

#### Docker 환경 API URL 중복 문제 수정 ⭐⭐ (22:30 완료)
**문제 해결 (2단계)**

**1단계: API 경로 중복 해결**
- Docker 환경에서 API 경로 중복으로 404 에러
- Nginx 로그: `POST /api/api/v1/auth/signup → 302` (잘못됨)
- 근본 원인: `NEXT_PUBLIC_API_URL=/api` + API 코드 `/api/v1/...` = `/api/api/...` 중복
- 해결: `NEXT_PUBLIC_API_URL=""` (빈 문자열)로 변경

**2단계: Falsy 값 처리 문제 해결**
- 브라우저 에러: `POST http://localhost:8080/api/v1/auth/signup net::ERR_FAILED`
- 근본 원인: JavaScript에서 빈 문자열(`""`)은 **falsy** 값
  - `const baseURL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'`
  - `"" || 'http://localhost:8080'` → `'http://localhost:8080'` (fallback 사용) ❌
- 해결: `||` 연산자 → `??` (nullish coalescing) 연산자 변경
  - `null`/`undefined`만 fallback 처리
  - 빈 문자열(`""`)은 유효한 값으로 인정 ✅

**수정된 파일**
- `frontend/Dockerfile`
  - `ARG NEXT_PUBLIC_API_URL=` (빈 문자열)
- `docker-compose.yml`
  - `args: NEXT_PUBLIC_API_URL: ""` (빈 문자열)
- `frontend/src/api/axios-instance.ts`
  - `const baseURL = ... || ...` → `const baseURL = ... ?? ...`

**기술적 배경**
- **|| 연산자**: 모든 falsy 값(0, "", false, null, undefined) → fallback 사용
- **?? 연산자**: null/undefined만 → fallback 사용, 빈 문자열은 유효
- 환경별 동작:
  - 로컬: `undefined ?? 'http://localhost:8080'` → `'http://localhost:8080'` ✅
  - Docker: `"" ?? 'http://localhost:8080'` → `""` ✅

**효과**
- API 경로 정상화: `/api/v1/...`
- 회원가입, 로그인, OAuth2 정상 작동

#### Docker 환경 Next.js 빌드 타임 환경 변수 주입 ⭐⭐ (22:15 완료)
**문제 해결**
- Docker 환경에서 프론트엔드 API URL이 undefined
- 브라우저 로그: `GET /undefined/oauth2/authorization/google → 404`

**근본 원인**
- Next.js의 `NEXT_PUBLIC_*` 변수는 **빌드 타임**에 번들에 포함되어야 함
- docker-compose.yml에서 **런타임** environment로만 주입
- 빌드된 JavaScript 파일에 환경 변수가 포함되지 않음

**해결 방법**
1. **Dockerfile에 ARG 추가** (빌드 타임 변수)
   - `ARG NEXT_PUBLIC_API_URL=/api`
   - `ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL`
   - 빌드 시점에 환경 변수 설정

2. **docker-compose.yml에 build args 추가**
   - `args: NEXT_PUBLIC_API_URL: /api`
   - Dockerfile ARG에 값 전달

**기술적 배경**
- **빌드 타임 vs 런타임 환경 변수**
  - 빌드 타임: Dockerfile ARG → 빌드 중 webpack이 번들에 포함
  - 런타임: docker-compose environment → 서버 실행 시에만 사용
- Next.js `NEXT_PUBLIC_*` 변수는 클라이언트 번들에 포함
  - 빌드 시점에 확정되어야 함
  - 런타임 변경 불가

**수정된 파일**
- `frontend/Dockerfile`
  - build 스테이지에 ARG, ENV 추가
- `docker-compose.yml`
  - frontend 빌드에 args 추가
  - 런타임 NEXT_PUBLIC_API_URL 환경 변수 제거 (불필요)

**CORS 설정 검증**
- SecurityConfig.java: `@Value("${FRONTEND_URL}")` 사용
- .env.example: `FRONTEND_URL=http://localhost`
- 브라우저 → http://localhost (포트 80)
- CORS 허용 Origin: http://localhost ✅

**효과**
- 프론트엔드에서 `/api` 경로로 API 호출 성공
- OAuth2 Google 로그인 정상 작동
- CORS 에러 해결

#### Docker 환경 Redis 인증 문제 수정 ⭐⭐ (22:00 완료)
**문제 해결**
- Docker 환경에서 Redis 연결 시 "NOAUTH HELLO must be called with the client already authenticated" 에러
- "Unable to connect to redis/<unresolved>:6379" 에러

**근본 원인 분석**
1. **RedisConfig.java에서 비밀번호 설정 누락** (핵심 원인)
   - `@Value("${spring.data.redis.password}")` 주입 없음
   - `redisConfig.setPassword()` 호출 없음
   - Java Config가 application.yml보다 우선 적용됨

2. **docker-compose.yml 환경 변수 이름 불일치**
   - `SPRING_REDIS_*` → `SPRING_DATA_REDIS_*`로 수정 필요
   - Spring Boot 3.x 매핑 규칙 미준수

**해결 방법**
1. **RedisConfig.java 수정** (핵심 수정)
   - `@Value("${spring.data.redis.password:}")` 추가
   - `redisConfig.setPassword(redisPassword)` 호출 추가
   - 비밀번호가 있을 때만 적용 (null/empty 체크)

2. **docker-compose.yml 수정**
   - `SPRING_REDIS_*` → `SPRING_DATA_REDIS_*` 변경

**수정된 파일**
- `Backend/src/main/java/com/todolist/config/RedisConfig.java`
  - redisPassword 필드 추가
  - setPassword() 호출 추가
- `docker-compose.yml`
  - 환경 변수 이름 수정

**기술적 배경**
- Java Config (`@Configuration`)가 application.yml보다 우선 적용
- RedisConnectionFactory 빈을 직접 생성하면 yml 설정 무시됨
- 환경 변수는 `@Value`로 명시적 주입 필요

**효과**
- Redis 인증 성공
- Rate Limiting, Session Storage 정상 작동

#### Docker 환경 Flyway 마이그레이션 순서 수정 ⭐⭐ (21:45 완료)
**문제 해결**
- Docker 시작 시 "Schema validation: missing table [categories]" 에러
- Hibernate 스키마 검증이 Flyway 마이그레이션보다 먼저 실행되는 문제

**해결 방법**
- `spring.jpa.hibernate.ddl-auto: validate` → `none`으로 변경
  - Flyway가 모든 스키마를 관리, Hibernate는 관여하지 않음
- `spring.flyway.create-schemas: true` 추가
  - Flyway가 스키마를 자동 생성하도록 보장

**기술적 배경**
- Flyway는 Spring Boot에서 Hibernate보다 먼저 실행되지만,
  Docker 첫 실행 시 스키마가 없는 상태에서 타이밍 이슈 발생 가능
- `ddl-auto: none`이 프로덕션 환경에서 가장 안전한 옵션
  - validate: 테이블 없으면 에러
  - none: Flyway만 스키마 관리, Hibernate는 사용만 함

**수정된 파일**
- `Backend/src/main/resources/application-prod.yml`
  - `jpa.hibernate.ddl-auto: none`
  - `flyway.create-schemas: true`

**효과**
- Docker 환경에서 안정적인 초기화 순서 보장
- Flyway 마이그레이션 → Hibernate 초기화 순서 명확화

#### Docker 빌드 최적화 - Google Fonts 로컬 폰트 전환 ⭐ (21:30 완료)
**문제 해결**
- Docker 빌드 환경에서 Google Fonts 네트워크 차단으로 next build 실패
- 에러: Failed to fetch `Inter` from Google Fonts (ETIMEDOUT)

**해결 방법**
- Inter 폰트를 로컬 파일로 전환 (next/font/google → next/font/local)
- Inter Variable Font 다운로드 및 public/fonts/ 추가
- layout.tsx 수정, className 적용 방식 유지

**수정된 파일**
- `frontend/src/app/layout.tsx` (next/font/local 사용)
- `frontend/public/fonts/Inter-VariableFont.woff2` (신규, 291KB)

**효과**
- Docker 빌드 시 네트워크 의존성 제거
- 오프라인 빌드 가능
- 폰트 로딩 성능 개선 (로컬 파일)

#### Docker 컨테이너화 - Phase 3: 앱 설정 수정 ⭐⭐ (21:00 완료)
**Backend: application-prod.yml 생성**
- Docker 환경 설정 (postgres, redis 서비스명 사용)
- 환경 변수 기반 구성 (DB, Redis, JWT, OAuth2, CORS)
- 프로덕션 최적화 (SQL 로그 비활성화, Swagger 비활성화, INFO 레벨 로깅)
- Spring Boot 환경 변수 매핑 (JWT_SECRET → jwt.secret 등)

**Backend: SecurityConfig.java CORS 수정**
- CORS 설정 환경 변수화 (`@Value("${FRONTEND_URL}")`)
- 하드코딩 제거, 동적 설정 지원
- 로컬 개발 환경 보존 (기본값: localhost:3000)

**Frontend: 환경 변수 처리**
- axios-instance.ts 확인 (이미 환경 변수 사용 중)
- `.env.local.example` 생성 (로컬 개발 가이드)

**환경 변수 문서화**
- `.env.example` 개선 (Spring Boot 매핑 규칙 주석 추가)
- 환경 변수 → Spring Boot 프로퍼티 매핑 표 작성

**수정된 파일**
- `Backend/src/main/resources/application-prod.yml` (신규)
- `Backend/src/main/java/com/todolist/config/SecurityConfig.java` (수정)
- `frontend/.env.local.example` (신규)
- `.env.example` (개선)

**다음 단계**
- Phase 4: 클라우드 배포 (오라클 클라우드, SSL/HTTPS, 도메인 연결)

#### Docker 컨테이너화 - Phase 2: docker-compose.yml 작성 ⭐⭐⭐ (20:00 완료)
**Orchestration 구성**
- docker-compose.yml: 5개 서비스 통합 (nginx, frontend, backend, postgres, redis)
- 서비스 의존성 및 헬스체크 설정
- Named Volumes 설정 (postgres_data, redis_data)
- 환경 변수 주입 (.env 파일)

**nginx 리버스 프록시**
- `/api/**` → backend:8080 프록시
- `/oauth2/**` → backend:8080 프록시
- `/**` → frontend:3000 프록시
- 정적 파일 캐싱, gzip 압축 활성화

**환경 변수 관리**
- `.env.example` 생성 (DB, Redis, JWT, OAuth2 설정)
- `.gitignore` 생성 (보안: .env 제외)
- DOCKER_SETUP.md 가이드 작성

**생성된 파일**
- `docker-compose.yml`
- `nginx/nginx.conf`
- `.env.example`
- `.gitignore`
- `DOCKER_SETUP.md`

**다음 단계**
- Phase 3: 앱 설정 수정 (Spring Boot application-prod.yml, Next.js 환경 변수)

#### Docker 컨테이너화 - Phase 1: Dockerfile 작성 ⭐⭐ (19:00 완료)
**배포 전략 변경**
- 기존: AWS (Vercel + ECS + RDS) → 변경: 오라클 클라우드 (Docker Compose)
- 목적: 학습용 프로젝트, 가성비 클라우드에 전체 스택 배포

**Backend Dockerfile**
- 멀티스테이지 빌드 (build: eclipse-temurin:21-jdk, run: eclipse-temurin:21-jre)
- Gradle 빌드 최적화 (의존성 캐싱)
- 비루트 유저 (appuser) 실행
- 포트 8080, 헬스체크 설정
- Spring Boot prod 프로파일 적용

**Frontend Dockerfile**
- 3단계 멀티스테이지 빌드 (deps → build → run)
- node:20-alpine 베이스
- Next.js standalone 모드 빌드
- 비루트 유저 (nextjs) 실행
- 포트 3000

**추가 작업**
- `next.config.mjs`: output: 'standalone' 옵션 추가
- `.dockerignore` 파일 추가 (Backend, Frontend)

**다음 단계**
- Phase 2: docker-compose.yml + 환경변수 파일 작성
- Phase 3: 앱 설정 수정 (Spring Boot + Next.js)

#### Google OAuth2 소셜 로그인 구현 ⭐⭐⭐ (00:30 완료)
**OAuth2 인증 플로우**
- Spring Security OAuth2 Login 통합
- Google OAuth2 클라이언트 설정 (application-local.yml)
- CustomOAuth2UserService: 사용자 조회/생성/병합 로직
- OAuth2AuthenticationSuccessHandler: JWT 발급 및 Redis 세션 저장

**3가지 시나리오 구현**
1. **신규 사용자**: 약관 동의 페이지 → 가입 완료
2. **계정 병합**: 기존 이메일 감지 → 연동 동의 → 기존 계정에 Google 추가
3. **기존 OAuth2 사용자**: 즉시 로그인

**Frontend 페이지**
- `/auth/callback`: OAuth2 콜백 처리 및 자동 분기
- `/auth/oauth2/terms`: 신규 사용자 약관 동의
- `/auth/oauth2/merge`: 계정 연동 확인 및 동의
- `/login`: Google 로그인 버튼 추가

**Backend API**
- `GET /api/v1/auth/oauth2/exchange`: 세션 ID → JWT 토큰 교환
- `POST /api/v1/auth/oauth2/agree-terms`: 약관 동의 처리
- `POST /api/v1/auth/oauth2/agree-merge`: 계정 병합 처리

**DB 마이그레이션 (V7)**
- `terms_agreed_at`, `privacy_agreed_at` 컬럼 추가
- 기존 사용자 자동 약관 동의 처리

#### 일반 회원가입 약관 동의 추가 ⭐⭐ (00:20 완료)
**Frontend**
- 서비스 이용약관 체크박스 (필수)
- 개인정보 처리방침 체크박스 (필수)
- Zod 스키마 검증: 약관 미동의 시 폼 제출 차단

**Backend**
- SignupRequest: `termsAgreed`, `privacyAgreed` 필드 추가
- Jakarta Validation: `@AssertTrue` 검증
- AuthService: 회원가입 시 약관 동의 자동 기록

**보안 강화**
- API 직접 호출 시 약관 미동의 차단 (400 에러)
- 법적 증빙: 약관 동의 일시 DB 저장

#### 계정 병합 약관 처리 개선 ⭐ (00:30 완료)
**문제 해결**
- 기존 약관 동의 시간 덮어쓰기 → 보존 로직으로 변경
- 조건부 약관 동의: `hasAgreedToTerms()` 체크 후 처리
- 원본 동의 시간 유지 (법적 증빙 강화)

**로그 추가**
- 약관 동의 추가 처리 vs 기존 동의 유지 명확히 기록

---

### ✅ 이전 완료 작업 (2026-02-18)

#### Redis & Rate Limiting 구현 ⭐⭐⭐ (22:00 완료)
**Redis 인프라 구축**
- Docker Redis 실행 (localhost:6379)
- build.gradle: Redis, Bucket4j 의존성 추가
- RedisConfig: 연결 팩토리, RedisTemplate, CacheManager
- ObjectMapper: JavaTimeModule 등록

**Rate Limiting 구현**
- RateLimiterService: Redis 기반 토큰 버킷 알고리즘
  - 로그인: 5회/15분 (IP 기반)
  - 회원가입: 3회/1시간 (IP 기반)
  - Todo CRUD: 100회/1분 (사용자 기반)
- RateLimitFilter: 요청 전 Rate Limit 체크, 429 에러 반환
- ErrorCode: COMMON_RATE_LIMIT_EXCEEDED 추가
- Frontend: 429 에러 자동 Toast 표시 (이미 구현됨)

**테스트 완료**
- 로그인 6번 실패 → 429 에러 발생 확인
- Toast: "로그인 시도 한도를 초과했습니다"
- Redis 데이터 저장 및 TTL 확인

#### 에러 처리 & Toast 통합 ⭐⭐⭐ (21:30 완료)
**Phase 1: 자동 토큰 갱신 & 기본 Toast** (20:45)
- 자동 토큰 갱신 확인 (이미 구현되어 있었음)
- Toast 라이브러리 통합 (Sonner)
- Axios Interceptor에 Toast 알림 추가
- 401/403 에러 처리 개선
- 로그아웃 전 3초 딜레이 (Toast 확인 가능)
- 중복 로그아웃 방지 (`isLoggingOut` 플래그)

**Phase 2: 에러 코드 매핑 & 네트워크 에러** (21:30)
- `lib/error-messages.ts` 생성 (20개 에러 코드 매핑)
- 에러 코드 기반 Toast 자동 표시
- HTTP 상태 코드 폴백 메시지
- 네트워크 에러 처리 (타임아웃, 연결 끊김)
- 유효성 검증 에러 상세 표시 (400)
- 로그인 실패 즉각 피드백 (리다이렉트 없음)
- Toast 닫기 버튼 추가

#### 할일 수정 & 휴지통 기능 ⭐ (18:15 완료)
- TodoItem에 Edit 버튼 및 클릭 이벤트 추가
- Dialog + TodoForm으로 수정 UI 구현
- 폼 스키마 개선 (빈 문자열 처리)
- 휴지통 API 타입 불일치 해결 (PageResponse)
- 삭제/복원/영구삭제 기능 정상 작동 확인

#### Optimistic Updates 구현 ⭐ (17:20 완료)
- useCreateTodo: onMutate로 임시 ID 생성, 즉시 UI에 추가
- useUpdateTodo: onMutate로 즉시 변경사항 반영
- useToggleTodo: 완료 상태 즉시 토글 (이미 구현됨)
- useDeleteTodo: 즉시 UI에서 제거 (이미 구현됨)
- 모든 mutation: onError 시 previousQueries로 자동 롤백
- onSettled로 서버 데이터 재검증

#### Flyway 마이그레이션 시스템 구축 ⭐ (16:05 완료)
- V1~V5 마이그레이션 파일 작성 (스키마, 함수, 테이블, 파티션, 트리거)
- application.yml Flyway 설정 완료
- 엔티티 스키마 검증 수정 (Category, Todo)
- FLYWAY_MIGRATION_GUIDE.md 문서 작성
- 애플리케이션 정상 시작 확인

#### PostgreSQL 동적 쿼리 개선
- TodoRepository: JpaSpecificationExecutor 추가
- TodoSpecification: 동적 쿼리 클래스 작성
- PostgreSQL 파라미터 타입 에러 해결

#### 테스트 & 문서
- TodoServiceTest: 10개 단위 테스트 (모두 통과)
- TEST_GUIDE.md: 테스트 가이드 작성
- DTO Builder 패턴 추가

### ⏭️ 다음 우선순위 (PROJECT_STATUS.md 참조)

**Phase 1: 사용자 경험 개선** ✅ 완료
1. ✅ Optimistic Updates
2. ✅ 에러 처리 & Toast 통합

**Phase 2: 성능 & 보안** ✅ 완료 (2026-02-19)
1. ✅ **Redis & Rate Limiting** - 완료 (2026-02-18)
2. ✅ **OAuth2 소셜 로그인** - 완료 (2026-02-19)
3. ✅ **JWT 인증 보안 강화** - 완료 (2026-02-19)
   - SessionCreationPolicy.STATELESS
   - authenticationEntryPoint (Google 리다이렉트 방지)
   - 로그아웃 토큰 블랙리스트 (Redis)

**Phase 3: Docker 로컬 배포** ✅ 완료 (2026-02-19)
1. ✅ **Dockerfile 작성** (Backend, Frontend Multi-stage)
2. ✅ **docker-compose.yml** (5개 서비스)
3. ✅ **nginx.conf** (리버스 프록시, OAuth2 경로)
4. ✅ **Flyway 안정화** (FlywayConfig.java 수동 Bean, V7 마이그레이션)
5. ✅ **환경 변수 설정** (.env.example, DOCKER_SETUP.md)

**Phase 4: 클라우드 배포** ⭐⭐⭐ (다음 우선순위)
1. **Oracle Cloud VM 설정** - 인스턴스 생성, Docker 설치
2. **운영 환경 설정** - 환경 변수, 보안 그룹, 방화벽
3. **도메인 & SSL** - 도메인 연결, Let's Encrypt 인증서
4. **nginx HTTPS** - SSL 설정, HTTP → HTTPS 리다이렉트
5. **모니터링** - 로그 수집, 헬스 체크

**Phase 5: 캐싱 & 테스트** (나중에)
1. **Redis 캐싱** ⭐⭐ - 카테고리, 통계 캐싱
2. **테스트 확장** ⭐⭐ - AuthService (OAuth2 포함), Controller 테스트
3. **E2E 테스트** ⭐ - Playwright 기반 주요 플로우 테스트

**Phase 6: 추가 기능** (선택)
1. **StatisticsService** ⭐ - 대시보드 통계
2. **다크모드 & 반응형** ⭐ - UI 개선

### 📊 전체 진행률
**약 92%** (상세: [PROJECT_STATUS.md](./docs/PROJECT_STATUS.md))

**주요 완료 기능**:
- ✅ 인증/인가 (JWT + OAuth2 + 블랙리스트)
- ✅ Todo CRUD + 휴지통
- ✅ 카테고리 관리
- ✅ Rate Limiting + Redis
- ✅ 에러 처리 + Toast
- ✅ Optimistic Updates
- ✅ Docker 로컬 배포 (5개 서비스 컨테이너화)
- ✅ Flyway 마이그레이션 (7개 버전)

**남은 작업**:
- ⏳ **클라우드 배포** (Oracle Cloud) - 다음 우선순위
- ⏳ 캐싱 (Redis)
- ⏳ 통계 대시보드
- ⏳ 테스트 확장

---

## 💡 Claude Code 사용 시 주의사항

1. **문서 우선 확인**: 구현 전 반드시 PROJECT_STATUS.md 확인
2. **컨벤션 준수**: CLAUDE.md의 코딩 컨벤션 엄격히 준수
3. **테스트 작성**: 새 기능은 반드시 테스트 코드 포함
4. **문서 업데이트**: 주요 변경 시 관련 문서 업데이트
5. **보안 검증**: 민감 정보 하드코딩 금지, 소유권 검증 필수
