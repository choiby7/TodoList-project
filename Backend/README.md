# TodoList Backend API

Spring Boot 기반의 TodoList 서비스 백엔드 API입니다.

## 🚀 빠른 시작

### 사전 요구사항

- Java 17 이상
- PostgreSQL 15 이상
- Gradle 8.x

### 환경 설정

1. **PostgreSQL 데이터베이스 생성**
   ```sql
   CREATE DATABASE todolist_db;
   CREATE USER todolist_app WITH PASSWORD 'MySecure2026!Password';
   GRANT ALL PRIVILEGES ON DATABASE todolist_db TO todolist_app;
   ```

2. **환경 변수 설정** (선택사항)
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/todolist_db
   export DB_USERNAME=todolist_app
   export DB_PASSWORD=MySecure2026!Password
   export JWT_SECRET=your-256-bit-secret-key
   ```

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

4. **빌드**
   ```bash
   ./gradlew build
   ```

## 📚 API 문서

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## 🗂️ 주요 엔드포인트

### 인증 (Auth)
- `POST /api/v1/auth/signup` - 회원가입
- `POST /api/v1/auth/login` - 로그인
- `POST /api/v1/auth/refresh` - 토큰 갱신
- `GET /api/v1/auth/me` - 내 정보 조회

### 할일 (Todo)
- `GET /api/v1/todos` - 할일 목록 조회 (페이징, 필터링)
- `POST /api/v1/todos` - 할일 생성
- `PUT /api/v1/todos/{id}` - 할일 수정
- `DELETE /api/v1/todos/{id}` - 할일 삭제 (소프트 삭제)
- `PATCH /api/v1/todos/{id}/toggle` - 완료 상태 토글

### 카테고리 (Category)
- `GET /api/v1/categories` - 카테고리 목록 조회
- `POST /api/v1/categories` - 카테고리 생성
- `PUT /api/v1/categories/{id}` - 카테고리 수정
- `DELETE /api/v1/categories/{id}` - 카테고리 삭제

## 🔧 기술 스택

- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: PostgreSQL 15+
- **Migration**: Flyway
- **Security**: Spring Security 6.x + JWT
- **Documentation**: Swagger (springdoc-openapi)
- **Build**: Gradle
- **Test**: JUnit 5 + Mockito

## 📖 문서

- [Flyway 마이그레이션 가이드](./docs/FLYWAY_MIGRATION_GUIDE.md)
- [테스트 가이드](./docs/TEST_GUIDE.md)

## 🧪 테스트 실행

```bash
# 단위 테스트
./gradlew test

# 테스트 커버리지 리포트
./gradlew jacocoTestReport
```

## 🔒 보안

- JWT 기반 인증/인가
- BCrypt 비밀번호 해싱
- Brute Force 방어 (5회 실패 시 15분 잠금)
- CORS 설정
- SQL Injection 방어

## 📝 라이선스

이 프로젝트는 교육 목적으로 작성되었습니다.
