# TodoList 프로젝트 구현 상태 분석

> CLAUDE.md 기준 구현 상태 및 다음 스텝
> 최종 업데이트: 2026-02-19 14:00

---

## 📊 전체 진행률: 약 92%

**최근 완료:** Docker 로컬 배포 환경 구축 완료 (2026-02-19)

---

## ✅ 구현 완료 항목

### Backend (Spring Boot)

#### 1. 도메인 & 엔티티
- ✅ Todo 엔티티 (소프트 삭제, 상태 관리)
- ✅ Category 엔티티
- ✅ User 엔티티 (추정)
- ⚠️ RefreshToken, ActivityLog 확인 필요

#### 2. Repository Layer
- ✅ TodoRepository (JpaRepository + JpaSpecificationExecutor)
- ✅ CategoryRepository
- ✅ UserRepository (추정)
- ✅ TodoSpecification (동적 쿼리) ← **방금 수정**

#### 3. Service Layer
- ✅ TodoService (CRUD, 필터링, 소프트 삭제, 완료 토글)
- ✅ CategoryService
- ✅ AuthService
- ❌ StatisticsService (미확인)

#### 4. Controller Layer
- ✅ TodoController
- ✅ CategoryController
- ✅ AuthController
- ❌ StatisticsController (미확인)

#### 5. Security & JWT
- ✅ JwtTokenProvider (getRemainingExpiration 추가) ⭐
- ✅ JwtAuthenticationFilter (블랙리스트 체크 추가) ⭐
- ✅ UserDetailsServiceImpl
- ✅ SecurityConfig
  - ✅ SessionCreationPolicy.STATELESS (보안 강화) ⭐
  - ✅ authenticationEntryPoint (Google 리다이렉트 방지) ⭐
- ✅ Rate Limiting (Redis 기반)
  - RateLimiterService: 토큰 버킷 알고리즘
  - RateLimitFilter: 요청 가로채기
  - 로그인: 5회/15분 (IP 기반)
  - 회원가입: 3회/1시간 (IP 기반)
  - Todo CRUD: 100회/1분 (사용자 기반)
- ✅ 로그아웃 토큰 블랙리스트 (Redis blacklist:*) ⭐ **2026-02-19 완료**
- ✅ OAuth2 소셜 로그인 (Google) ⭐⭐⭐ **2026-02-19 완료**
  - CustomOAuth2UserService (사용자 조회/생성/병합)
  - OAuth2AuthenticationSuccessHandler (JWT 발급, Redis 세션)
  - OAuth2AuthenticationFailureHandler
  - 3가지 시나리오: 신규/병합/기존 사용자
  - 약관 동의 처리 (V7 마이그레이션)
- ✅ CORS 설정 (환경 변수 기반)

#### 6. DTO & Exception
- ✅ Request DTOs (TodoCreateRequest, TodoUpdateRequest, etc.) ← **Builder 추가**
- ✅ Response DTOs (TodoResponse, PageResponse, etc.)
- ✅ ErrorCode Enum (체계적 정의, COMMON_RATE_LIMIT_EXCEEDED 추가)
- ✅ Custom Exceptions (ResourceNotFound, Forbidden, BadRequest, etc.)

#### 7. Configuration
- ✅ SecurityConfig (RateLimitFilter 추가)
- ✅ SwaggerConfig
- ✅ RedisConfig ⭐ **최근 완료**
  - RedisConnectionFactory (Lettuce)
  - RedisTemplate (Rate Limiting용)
  - CacheManager (캐싱용, TTL 10분)
  - ObjectMapper (JSR310 모듈 등록)

#### 8. 테스트
- ✅ TodoServiceTest (10개 단위 테스트, 모두 통과)
- ✅ TEST_README.md (테스트 가이드 문서)
- ❌ Repository 통합 테스트 (Testcontainers 필요)
- ❌ Controller 테스트 (MockMvc)
- ❌ 다른 Service 테스트

#### 9. 데이터베이스 & 마이그레이션
- ✅ PostgreSQL 18.2 스키마 (모든 테이블 생성 완료)
- ✅ Flyway 의존성 추가 (flyway-core, flyway-database-postgresql)
- ✅ Flyway 마이그레이션 파일 7개 작성
  - V1: 스키마 & ENUM 타입
  - V2: 함수 (cleanup, triggers 등)
  - V3: 테이블 (users, todos, categories 등)
  - V4: 파티션 (activity_logs 월별)
  - V5: 트리거 (자동 타임스탬프, 비즈니스 로직)
  - V6: OAuth2 지원 (provider, provider_id, profile_image)
  - V7: 약관 동의 (terms_agreed_at, privacy_agreed_at) ⭐
- ✅ FlywayConfig.java (수동 Bean 등록, Spring Boot 4.x 호환) ⭐
- ✅ application.yml Flyway 설정 (enabled: false, 수동 설정)
- ✅ DDL-auto: none (Flyway가 모든 스키마 관리)
- ✅ RefreshToken last_used_at 기본값 설정 ⭐
- ✅ FLYWAY_MIGRATION_GUIDE.md 작성

#### 10. Docker & 배포 ⭐⭐⭐ **2026-02-19 완료**
- ✅ docker-compose.yml (5개 서비스: nginx, frontend, backend, postgres, redis)
- ✅ Backend Dockerfile (Multi-stage: eclipse-temurin JDK→JRE)
  - JAR 검증 (db/migration 확인)
  - 비루트 유저 실행
- ✅ Frontend Dockerfile (Multi-stage: deps→build→run, standalone 모드)
  - 로컬 폰트 (Inter) 사용 (Google Fonts 네트워크 차단 해결)
  - 환경 변수 빌드 타임 주입
- ✅ nginx.conf (리버스 프록시)
  - /api/ → Backend
  - /oauth2/, /login/oauth2/ → Backend (OAuth2 경로) ⭐
  - / → Frontend
- ✅ .env.example (환경 변수 템플릿)
- ✅ DOCKER_SETUP.md (배포 가이드)
- ✅ application-prod.yml (Docker 환경 설정)
  - Flyway logging: DEBUG
  - DataSource URL currentSchema 제거 (Flyway 실행 보장)

---

### Frontend (Next.js)

#### 1. 페이지 구조
- ✅ App Router 구조
- ✅ 인증 페이지 (/login, /signup)
- ✅ 대시보드 (/dashboard)
- ✅ 휴지통 (/dashboard/trash)
- ❌ 카테고리별 페이지 (/category/[id]) 확인 필요

#### 2. 컴포넌트
- ✅ UI Components (shadcn/ui: Button, Input, Dialog, Select, etc.)
- ✅ Todo 컴포넌트 (TodoList, TodoItem, TodoForm, TodoFilters)
- ✅ Category 컴포넌트 (CategoryList, CategoryForm)
- ✅ Layout 컴포넌트 (Header, Sidebar)

#### 3. API & 상태 관리
- ✅ Axios Instance
- ✅ API 클라이언트 (auth, todos, categories)
- ✅ TanStack Query Hooks (useAuth, useTodos, useCategories)
- ✅ TanStack Query Provider
- ✅ Optimistic Updates 구현 ⭐
  - useCreateTodo: 즉시 UI에 새 Todo 추가
  - useUpdateTodo: 즉시 UI에 변경사항 반영
  - useToggleTodo: 완료 상태 즉시 토글
  - useDeleteTodo: 즉시 UI에서 제거
  - 모든 mutation: onError 시 자동 롤백
- ✅ 에러 처리 & Toast 통합 ⭐ **최근 완료**
  - 자동 토큰 갱신 (axios interceptor)
  - 에러 코드 → 사용자 메시지 매핑 (20개 에러 코드)
  - 네트워크 에러 처리 (타임아웃, 연결 끊김)
  - 로그인 실패 즉각 피드백 (리다이렉트 없음)
  - Toast 닫기 버튼
  - 중복 로그아웃 방지
- ⚠️ Zustand Store 확인 필요

#### 4. 스타일 & UI
- ✅ Tailwind CSS 설정
- ✅ shadcn/ui 컴포넌트
- ❌ 다크모드 구현 확인 필요
- ❌ 반응형 디자인 테스트 필요

---

## ❌ 미구현 / 확인 필요 항목

### Backend 우선순위 높음

1. **Redis & 캐싱** ⭐⭐
   - RedisConfig 구현
   - 카테고리 목록, 통계 데이터 캐싱 (@Cacheable)
   - RefreshToken Redis 저장소로 전환

2. **Rate Limiting** ⭐⭐
   - Bucket4j + Redis 구현
   - 로그인 시도: 5회/15분 (IP 기준)
   - Todo CRUD: 100회/분 (사용자 기준)

3. **StatisticsService & Controller** ⭐
   - `/api/v1/statistics/dashboard`
   - `/api/v1/statistics/trends`
   - 통계 계산 로직 구현

4. **테스트 확장** ⭐⭐
   - Repository 통합 테스트 (Testcontainers PostgreSQL)
   - Controller 테스트 (MockMvc)
   - AuthService, CategoryService 단위 테스트

### Frontend 우선순위 높음

1. **에러 처리 & Toast 통합** ⭐⭐⭐
   - Axios Interceptor 개선
   - 에러 코드별 Toast 메시지 매핑
   - 자동 토큰 갱신 로직 개선
   - 401/403 에러 핸들링

2. **Zustand Store** ⭐⭐
   - UI 상태 관리 (사이드바, 테마, 모달)
   - API 데이터 중복 저장 금지 확인

3. **다크모드** ⭐
   - 테마 전환 기능
   - localStorage 저장

4. **반응형 & 접근성** ⭐
   - 모바일 대응
   - 키보드 네비게이션

### 인프라 & DevOps

1. **Docker 설정** ⭐⭐⭐
   - Backend Dockerfile
   - Frontend Dockerfile
   - docker-compose.yml (PostgreSQL, Redis)

2. **환경 변수 관리** ⭐⭐
   - `.env.example` 파일
   - 운영 환경 변수 분리

3. **CI/CD** ⭐
   - GitHub Actions 워크플로우
   - 테스트 자동화
   - 배포 파이프라인

---

## 🎯 다음 스텝 (우선순위 순)

### ✅ 최근 완료 (2026-02-19)

**1. Docker 로컬 배포 환경 구축 완료** ⭐⭐⭐
- ✅ docker-compose.yml 작성 (5개 서비스)
- ✅ Backend/Frontend Dockerfile 작성 (Multi-stage)
- ✅ nginx.conf 리버스 프록시 설정
- ✅ .env.example 환경 변수 템플릿
- ✅ DOCKER_SETUP.md 배포 가이드 작성

**2. Flyway 실행 문제 해결** ⭐⭐
- ✅ DataSource URL currentSchema 파라미터 제거 (Flyway 실행 보장)
- ✅ FlywayConfig.java 수동 Bean 등록 (Spring Boot 4.x 호환)
- ✅ FlywayMigrationStrategy 패키지 변경 대응
- ✅ Flyway 로깅 DEBUG로 변경 (문제 진단)
- ✅ V7 마이그레이션 추가 (약관 동의 컬럼)

**3. JWT 인증 보안 강화** ⭐⭐⭐
- ✅ RefreshToken last_used_at 기본값 설정 (NOT NULL 제약 해결)
- ✅ SessionCreationPolicy.STATELESS 변경 (익명 세션 우회 차단)
- ✅ authenticationEntryPoint 추가 (Google 리다이렉트 방지, 401 JSON 반환)
- ✅ 로그아웃 토큰 블랙리스트 구현 (Redis blacklist:*)
  - JwtTokenProvider.getRemainingExpiration() 추가
  - JwtAuthenticationFilter.isTokenBlacklisted() 추가
  - AuthService.logout() Redis 저장 로직 추가
  - AuthController.logout() Authorization 헤더 추가

**4. nginx OAuth2 경로 프록시 설정** ⭐
- ✅ /login/oauth2/ → Backend (OAuth2 콜백)
- ✅ /oauth2/ → Backend (OAuth2 시작)
- ✅ Google OAuth2 정상 작동 확인

---

### ✅ 이전 완료 (2026-02-18)

**1. Flyway 마이그레이션 시스템 구축** (16:05 완료)
- ✅ V1~V5 마이그레이션 파일 작성
- ✅ application.yml 설정 완료
- ✅ 엔티티 스키마 검증 수정
- ✅ FLYWAY_MIGRATION_GUIDE.md 작성

**2. Optimistic Updates 구현** (17:20 완료) ⭐
- ✅ useCreateTodo: onMutate로 임시 ID 생성, 즉시 UI에 추가
- ✅ useUpdateTodo: onMutate로 즉시 변경사항 반영
- ✅ useToggleTodo: 완료 상태 즉시 토글 (이미 구현됨)
- ✅ useDeleteTodo: 즉시 UI에서 제거 (이미 구현됨)
- ✅ 모든 mutation: onError 시 previousQueries로 자동 롤백
- ✅ onSettled로 서버 데이터 재검증

**3. 할일 수정 기능 구현** (17:45 완료) ⭐
- ✅ TodoItem에 Edit 버튼 추가 (연필 아이콘)
- ✅ 제목/설명 클릭 시 수정 다이얼로그 열림
- ✅ Dialog + TodoForm으로 수정 UI 구현
- ✅ useUpdateTodo와 연동 (Optimistic Update 적용)
- ✅ 폼 스키마 개선 (빈 문자열 → undefined 변환)
- ✅ 저장 성공 시 다이얼로그 자동 닫힘

**4. 휴지통 기능 수정** (18:15 완료) ⭐
- ✅ API 타입 불일치 해결 (TodoResponse[] → PageResponse<TodoResponse>)
- ✅ 휴지통 페이지 데이터 바인딩 수정 (data?.content)
- ✅ 삭제된 Todo가 휴지통에 정상 표시
- ✅ 복원 기능 작동 확인
- ✅ 영구 삭제 기능 작동 확인
- ✅ 휴지통 비우기 기능 확인

**5. 에러 처리 & Toast 통합** (21:30 완료) ⭐⭐⭐
- ✅ **Phase 1: 자동 토큰 갱신 & 기본 Toast** (20:45)
  - 자동 토큰 갱신 확인 (이미 구현되어 있었음)
  - Toast 라이브러리 통합 (Sonner)
  - Axios Interceptor에 Toast 알림 추가
  - 401/403 에러 처리 개선
  - 로그아웃 전 3초 딜레이 (Toast 확인 가능)
  - 중복 로그아웃 방지 (`isLoggingOut` 플래그)

- ✅ **Phase 2: 에러 코드 매핑 & 네트워크 에러** (21:30)
  - `lib/error-messages.ts` 생성 (20개 에러 코드 매핑)
  - 에러 코드 기반 Toast 자동 표시
  - HTTP 상태 코드 폴백 메시지
  - 네트워크 에러 처리 (타임아웃, 연결 끊김)
  - 유효성 검증 에러 상세 표시 (400)
  - 로그인 실패 즉각 피드백 (리다이렉트 없음)
  - Toast 닫기 버튼 추가

**6. Redis & Rate Limiting 구현** (22:00 완료) ⭐⭐⭐
- ✅ **Redis 인프라 구축**
  - Docker Redis 실행 (localhost:6379)
  - build.gradle: Redis, Bucket4j 의존성 추가
  - RedisConfig: 연결 팩토리, RedisTemplate, CacheManager 설정
  - application.yml: Redis 접속 정보 설정
  - ObjectMapper: JavaTimeModule 등록 (LocalDateTime 직렬화)

- ✅ **Rate Limiting 구현**
  - ErrorCode: COMMON_RATE_LIMIT_EXCEEDED 추가
  - RateLimiterService: Redis 기반 토큰 버킷 알고리즘
    - 로그인: 5회/15분 (IP 기반)
    - 회원가입: 3회/1시간 (IP 기반)
    - Todo CRUD: 100회/1분 (사용자 기반)
  - RateLimitFilter: 요청 전 Rate Limit 체크, 429 에러 반환
  - SecurityConfig: RateLimitFilter 추가 (JWT 필터 앞)

- ✅ **Frontend 대응** (이미 구현됨)
  - error-messages.ts: COMMON_RATE_LIMIT_EXCEEDED 매핑
  - Axios Interceptor: 429 에러 자동 Toast 표시
  - 로그인 페이지: 리다이렉트 방지 로직

- ✅ **테스트 & 검증**
  - 로그인 6번 실패 → 429 에러 발생 확인
  - Toast: "로그인 시도 한도를 초과했습니다. 15분 후 다시 시도해주세요"
  - Redis KEYS 확인: rate_limit:login:127.0.0.1
  - Redis 초기화 후 정상 로그인 확인

### Phase 1: 사용자 경험 개선 ✅ 완료

1. **Optimistic Updates** ⭐⭐⭐ ✅
   - Todo 생성/수정/삭제 시 즉시 UI 반응
   - 실패 시 자동 롤백
   - onSettled로 서버 데이터 재검증

2. **에러 처리 & Toast 통합** ⭐⭐⭐ ✅
   ```
   ✅ Phase 1 완료 (자동 토큰 갱신 & 기본 Toast):
   - Axios Interceptor에 Toast 통합
   - 401/403 에러 자동 처리
   - 로그아웃 시 Toast 표시
   - 중복 로그아웃 방지

   ✅ Phase 2 완료 (에러 코드 매핑 & 네트워크 에러):
   - lib/error-messages.ts: 20개 에러 코드 매핑
   - 에러 코드 기반 Toast 자동 표시
   - 네트워크 에러 처리 (타임아웃, 연결 끊김)
   - 유효성 검증 에러 상세 표시
   - 로그인 실패 즉각 피드백
   - Toast 닫기 버튼
   ```

### Phase 2: 성능 & 보안 강화 ✅ 완료

3. **Redis & Rate Limiting** ⭐⭐⭐ ✅ 완료
   ```
   ✅ Redis 인프라: Docker, RedisConfig, RedisTemplate
   ✅ Rate Limiting: 로그인(5/15분), 회원가입(3/1h), Todo(100/1분)
   ✅ 429 에러 처리 및 Toast 표시
   ✅ 테스트 완료
   ```

4. **OAuth2 소셜 로그인** ⭐⭐⭐ ✅ 완료
   ```
   ✅ Google OAuth2 클라이언트 설정
   ✅ CustomOAuth2UserService (사용자 조회/생성/병합)
   ✅ OAuth2AuthenticationSuccessHandler (JWT 발급, Redis 세션)
   ✅ 3가지 시나리오: 신규/병합/기존 사용자
   ✅ 약관 동의 처리 (V7 마이그레이션)
   ✅ Frontend 콜백 페이지 (/auth/callback)
   ```

5. **JWT 인증 보안 강화** ⭐⭐⭐ ✅ 완료
   ```
   ✅ SessionCreationPolicy.STATELESS (익명 세션 우회 차단)
   ✅ authenticationEntryPoint (Google 리다이렉트 방지, 401 JSON)
   ✅ 로그아웃 토큰 블랙리스트 (Redis blacklist:*)
   ✅ RefreshToken last_used_at 기본값 설정
   ```

6. **Docker 로컬 배포** ⭐⭐⭐ ✅ 완료
   ```
   ✅ docker-compose.yml (nginx, backend, frontend, postgres, redis)
   ✅ Backend/Frontend Dockerfile (Multi-stage)
   ✅ nginx.conf (리버스 프록시, OAuth2 경로 추가)
   ✅ FlywayConfig.java (Spring Boot 4.x 호환)
   ✅ DOCKER_SETUP.md (배포 가이드)
   ```

---

### Phase 3: 추가 기능 & 캐싱 (다음 작업)

7. **Redis 캐싱 구현** ⭐⭐ (높은 우선순위)
   ```
   목표: API 응답 속도 개선
   작업:
   - @Cacheable 적용 (카테고리 목록: TTL 10분)
   - @Cacheable 적용 (통계 데이터: TTL 5분)
   - @CacheEvict 적용 (생성/수정/삭제 시)
   - RefreshToken Redis 저장소로 전환 (선택)
   ```

8. **테스트 커버리지 확대** ⭐⭐
   ```
   목표: 70% 이상 커버리지 달성
   작업:
   - CategoryService 단위 테스트 (CRUD, 소유권 검증)
   - AuthService 단위 테스트 (회원가입, 로그인, 토큰 갱신, OAuth2)
   - TodoController 테스트 (MockMvc, 인증/인가)
   - TodoRepository 통합 테스트 (Testcontainers PostgreSQL)
   ```

9. **StatisticsService 구현** ⭐
   ```
   엔드포인트:
   - GET /api/v1/statistics/dashboard (전체 통계)
   - GET /api/v1/statistics/trends (기간별 추이)
   작업:
   - 완료율, 우선순위별 분포, 카테고리별 통계 계산
   - Redis 캐싱 적용 (TTL 5분)
   ```

---

### Phase 4: 배포 & UI 개선

10. **Oracle Cloud 서버 배포** ⭐⭐⭐ (다음 우선순위)
   ```
   작업:
   - Oracle Cloud VM 인스턴스 생성
   - Docker 설치 및 설정
   - docker-compose.yml 운영 환경 설정
   - 도메인 연결 및 SSL 인증서
   - nginx HTTPS 설정
   ```

11. **CI/CD 파이프라인** ⭐
   ```
   파일: .github/workflows/ci.yml
   작업:
   - PR: Lint + Type Check + Test
   - develop: 위 + Dev 배포 (자동)
   - main: 위 + E2E + Prod 배포 (수동 승인)
   ```

12. **다크모드 & 반응형** ⭐
   ```
   작업:
   - next-themes 테마 전환
   - localStorage 저장
   - 모바일 레이아웃 최적화
   - 키보드 네비게이션 접근성
   ```

---

## 🔍 즉시 확인 필요 항목

### Backend
- [x] ~~Flyway 마이그레이션~~ ✅ 완료 (2026-02-18)
- [x] ~~엔티티 스키마 검증~~ ✅ 완료
- [ ] StatisticsController 존재 여부
- [ ] Redis 설정 확인
- [ ] CORS 상세 설정 확인

### Frontend
- [ ] Zustand Store 구현 상태
- [ ] Optimistic Updates 구현 여부 ← 최우선
- [ ] 에러 처리 Interceptor 상태 ← 최우선
- [ ] Toast 알림 구현 상태

### Database
- [x] ~~PostgreSQL 스키마~~ ✅ 확인 완료 (9개 테이블)
- [x] ~~인덱스 설정~~ ✅ V3 마이그레이션에 포함
- [x] ~~트리거 설정~~ ✅ V5 마이그레이션에 포함

---

## 💡 권장 작업 순서 (이번 주)

```
✅ Day 1 (완료): Flyway 마이그레이션 시스템 구축
Day 2: 프론트엔드 Optimistic Updates 구현
Day 3: 에러 처리 & Toast 통합
Day 4: Redis 캐싱 + Rate Limiting
Day 5: 테스트 커버리지 확대 + Docker 설정
```

---

## 📝 참고사항

### 현재 작동 상태 (2026-02-18 16:05 기준)
- ✅ 백엔드 애플리케이션 정상 시작
- ✅ Todo CRUD API 작동 (Specification 동적 쿼리)
- ✅ JWT 인증/인가 시스템 작동
- ✅ PostgreSQL 연결 정상
- ✅ Hibernate 스키마 검증 통과
- ✅ 단위 테스트 10개 모두 통과

### 최근 완료 작업 (2026-02-18)
1. **Flyway 마이그레이션 시스템 구축**
   - V1~V5 마이그레이션 파일 작성
   - application.yml Flyway 설정
   - FLYWAY_MIGRATION_GUIDE.md 작성
2. **엔티티 스키마 검증 수정**
   - Category: color_code, display_order NOT NULL
   - Todo: display_order NOT NULL
3. **PostgreSQL 파라미터 타입 에러 해결**
   - TodoRepository: JpaSpecificationExecutor 추가
   - TodoSpecification: 동적 쿼리 클래스 생성
   - TodoService: Specification 방식 적용
4. **테스트 코드 작성**
   - TodoServiceTest: 10개 단위 테스트
   - TEST_README.md: 테스트 가이드
5. **DTO Builder 패턴 추가**
   - TodoCreateRequest, TodoUpdateRequest

---

## 🚀 최우선 작업 추천

**다음 단계 (Phase 1: 사용자 경험 개선):**

1. **Optimistic Updates** ⭐⭐⭐ (Frontend UX 최우선)
   - Todo 생성/수정/삭제 시 즉시 UI 반응
   - 실패 시 자동 롤백
   - 사용자 대기 시간 제로

2. **에러 처리 & Toast** ⭐⭐⭐ (사용자 피드백)
   - 에러 코드별 친화적 메시지
   - 자동 토큰 갱신
   - 통일된 에러 경험

이 2가지를 완료하면 사용자가 체감하는 품질이 크게 향상됩니다.
