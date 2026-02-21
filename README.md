# TodoList 프로젝트

> 프로덕션 레벨의 TodoList 서비스 (Full Stack)

개인 및 팀 협업이 가능한 할일 관리 서비스입니다. Spring Boot 백엔드와 Next.js 프론트엔드로 구성된 3-Tier 아키텍처 기반 프로젝트입니다.

## 🎯 프로젝트 개요

- **목적**: 실무 수준의 TodoList 서비스 구현
- **아키텍처**: 3-Tier (Next.js + Spring Boot + PostgreSQL)
- **진행률**: 약 80% 완료
- **상태**: 개발 진행 중

## 🏗️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: PostgreSQL 15+
- **ORM**: Spring Data JPA + Flyway
- **Security**: Spring Security 6.x + JWT
- **API Docs**: Swagger (springdoc-openapi)

### Frontend
- **Framework**: Next.js 14+ (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS + shadcn/ui
- **State**: TanStack Query + Zustand
- **Form**: React Hook Form + Zod

## ✨ 주요 기능

### 인증 & 보안
- ✅ 회원가입/로그인 (JWT)
- ✅ 자동 토큰 갱신
- ✅ Brute Force 방어 (5회 실패 시 잠금)
- ✅ BCrypt 비밀번호 해싱

### Todo 관리
- ✅ CRUD (생성, 조회, 수정, 삭제)
- ✅ 완료 상태 토글
- ✅ 소프트 삭제 & 휴지통
- ✅ 우선순위 설정 (LOW/MEDIUM/HIGH)
- ✅ 마감일 설정
- ✅ 카테고리 분류
- ✅ 필터링 & 검색

### UX 개선
- ✅ Optimistic Updates (즉시 UI 반응)
- ✅ 실패 시 자동 롤백
- ✅ Toast 알림

### 데이터베이스
- ✅ Flyway 마이그레이션
- ✅ 테이블 파티셔닝 (activity_logs)
- ✅ 인덱스 최적화
- ✅ 트리거 자동화

## 🚀 빠른 시작

### 방법 1: Docker Compose (권장)

**로컬 환경에서 Docker로 전체 스택 실행**

```bash
# 1. 환경 변수 파일 생성
cp .env.db.example .env.db
cp .env.app.example .env.app

# 2. .env.db 파일 편집 (DB 사용자명/비밀번호 설정)
# 3. .env.app 파일 편집 (JWT Secret, OAuth2 키 등 설정)

# 4. 전체 스택 시작 (DB 서버 → 앱 서버 순서)
make up

# 접속 정보
# 프론트엔드: http://localhost
# 백엔드 API: http://localhost/api
# Swagger UI: http://localhost/swagger-ui/index.html

# 로그 확인
make logs

# 중지
make down
```

**프로덕션 서버 배포**

```bash
# DB 서버에서
cp .env.db.example .env.db
# .env.db 편집 (실제 비밀번호 입력)
make up-db

# 앱 서버에서
cp .env.app.prod.example .env.app
# .env.app 편집 (실제 비밀번호, 도메인 설정)
make up-app
```

### 방법 2: 로컬 개발 환경 (개발용)

#### 1. PostgreSQL 설정

```sql
CREATE DATABASE todolist_db;
CREATE USER todolist_app WITH PASSWORD 'MySecure2026!Password';
GRANT ALL PRIVILEGES ON DATABASE todolist_db TO todolist_app;
```

#### 2. 백엔드 실행

```bash
cd Backend
./gradlew bootRun
```

백엔드가 http://localhost:8080 에서 실행됩니다.

#### 3. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

프론트엔드가 http://localhost:3000 에서 실행됩니다.

#### 4. 접속 및 테스트

브라우저에서 http://localhost:3000 접속 후:
1. 회원가입
2. 로그인
3. Todo 생성/수정/삭제 테스트

## 📁 프로젝트 구조

```
TodoList-project/
├── CLAUDE.md                    # 개발 가이드 (컨벤션, 보안, API 설계)
├── README.md                    # 이 파일
├── Makefile                     # Docker Compose 실행 명령어
├── docs/
│   └── PROJECT_STATUS.md        # 전체 진행 상황
│
├── docker-compose.db.yml        # DB 서버 (PostgreSQL)
├── docker-compose.app.yml       # 앱 서버 (nginx, backend, frontend, redis)
├── docker-compose.single.yml    # 단일 서버용 (백업)
├── .env.db.example              # DB 환경 변수 템플릿
├── .env.app.example             # 앱 환경 변수 템플릿 (로컬)
├── .env.app.prod.example        # 앱 환경 변수 템플릿 (프로덕션)
│
├── nginx/                       # Nginx 리버스 프록시 설정
│   └── nginx.conf
│
├── Backend/                     # Spring Boot 백엔드
│   ├── Dockerfile
│   ├── README.md
│   ├── docs/
│   │   ├── FLYWAY_MIGRATION_GUIDE.md
│   │   └── TEST_GUIDE.md
│   └── src/...
│
└── frontend/                    # Next.js 프론트엔드
    ├── Dockerfile
    ├── README.md
    ├── docs/
    │   └── IMPLEMENTATION_SUMMARY.md
    └── src/...
```

## 📚 문서

### 전체 프로젝트
- **[CLAUDE.md](./CLAUDE.md)** - 개발 가이드 (필독)
- **[PROJECT_STATUS.md](./docs/PROJECT_STATUS.md)** - 진행 상황 및 다음 스텝

### Backend
- **[Backend README](./Backend/README.md)** - 백엔드 실행 가이드
- **[Flyway 마이그레이션 가이드](./Backend/docs/FLYWAY_MIGRATION_GUIDE.md)**
- **[테스트 가이드](./Backend/docs/TEST_GUIDE.md)**

### Frontend
- **[Frontend README](./frontend/README.md)** - 프론트엔드 실행 가이드
- **[구현 요약](./frontend/docs/IMPLEMENTATION_SUMMARY.md)**

## 🧪 테스트

### Backend
```bash
cd Backend
./gradlew test
```

### Frontend
```bash
cd frontend
npm run test
```

## 🎯 개발 현황

- **전체 진행률**: 약 80%
- **최근 완료**: Optimistic Updates, 할일 수정 기능, 휴지통 기능
- **다음 작업**: 에러 처리 & Toast 통합

상세 현황은 [PROJECT_STATUS.md](./docs/PROJECT_STATUS.md)를 참조하세요.

## 🔧 개발 환경

- **IDE**: IntelliJ IDEA (Backend), VS Code (Frontend)
- **JDK**: OpenJDK 17
- **Node**: 18.x 이상
- **Database**: PostgreSQL 15+

## 🔐 보안

- JWT 기반 인증/인가
- BCrypt 비밀번호 해싱
- Rate Limiting (계획 중)
- CORS 설정
- SQL Injection 방어

## 📝 커밋 컨벤션

```
feat(todo): add soft delete functionality
fix(auth): resolve jwt token expiration issue
refactor(service): optimize todo query performance
test(todo): add unit tests for TodoService
docs(api): update swagger documentation
```

## 🤝 기여

이 프로젝트는 교육 목적으로 작성되었습니다.

## 📄 라이선스

MIT License
