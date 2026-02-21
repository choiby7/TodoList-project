# 🐳 Docker 배포 가이드

TodoList 프로젝트를 Docker Compose로 배포하는 가이드입니다.

---

## 📋 사전 요구사항

- Docker Engine 20.10.0 이상
- Docker Compose V2 이상
- 최소 2GB RAM, 10GB 디스크 공간

---

## 🚀 빠른 시작

### 1. 환경 변수 설정

```bash
# .env.example을 복사하여 .env 파일 생성
cp .env.example .env

# .env 파일을 편집하여 실제 값 입력
# 필수: DB_PASSWORD, REDIS_PASSWORD, JWT_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
vi .env  # 또는 nano .env
```

**⚠️ 중요**: `.env` 파일은 민감한 정보를 포함하므로 Git에 커밋하지 마세요!

---

### 2. Docker Compose 실행

```bash
# 전체 스택 빌드 및 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 특정 서비스 로그만 확인
docker-compose logs -f backend
docker-compose logs -f frontend
```

---

### 3. 서비스 확인

```bash
# 실행 중인 컨테이너 확인
docker-compose ps

# 헬스체크 확인
curl http://localhost/actuator/health
```

**접속 URL**:
- 프론트엔드: http://localhost
- 백엔드 API: http://localhost/api
- 헬스체크: http://localhost/actuator/health

---

## 🛠️ 관리 명령어

### 서비스 중지

```bash
# 중지 (컨테이너 유지)
docker-compose stop

# 중지 및 삭제 (볼륨 유지)
docker-compose down

# 중지 및 삭제 (볼륨 포함, 데이터 삭제됨!)
docker-compose down -v
```

### 재시작

```bash
# 전체 재시작
docker-compose restart

# 특정 서비스만 재시작
docker-compose restart backend
```

### 재빌드

```bash
# 코드 변경 후 재빌드
docker-compose up -d --build

# 특정 서비스만 재빌드
docker-compose up -d --build backend
```

---

## 🔍 문제 해결

### 1. 포트 충돌

**증상**: `Error: Bind for 0.0.0.0:80 failed: port is already allocated`

**해결**:
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :80

# 해당 프로세스 종료 또는 docker-compose.yml의 포트 변경
# ports:
#   - "8080:80"  # 80 대신 8080 사용
```

### 2. 데이터베이스 연결 실패

**증상**: `Connection refused` 또는 `could not connect to server`

**해결**:
```bash
# PostgreSQL 컨테이너 로그 확인
docker-compose logs postgres

# 헬스체크 상태 확인
docker-compose ps

# 컨테이너 재시작
docker-compose restart postgres backend
```

### 3. Flyway 마이그레이션 실패

**증상**: Backend 시작 시 마이그레이션 에러

**해결**:
```bash
# Backend 로그 확인
docker-compose logs backend

# 데이터베이스 초기화 (데이터 삭제됨!)
docker-compose down -v
docker-compose up -d
```

### 4. 빌드 실패

**증상**: `Error during build`

**해결**:
```bash
# Docker 빌드 캐시 삭제
docker builder prune -a

# 재빌드
docker-compose build --no-cache
docker-compose up -d
```

---

## 📊 모니터링

### 리소스 사용량 확인

```bash
# 실시간 리소스 모니터링
docker stats

# 디스크 사용량
docker system df
```

### 로그 관리

```bash
# 로그 크기 제한 (docker-compose.yml에 추가)
# logging:
#   driver: "json-file"
#   options:
#     max-size: "10m"
#     max-file: "3"
```

---

## 🔐 보안 체크리스트

- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는지 확인
- [ ] 강력한 비밀번호 설정 (DB, Redis, JWT Secret)
- [ ] Google OAuth2 클라이언트 ID/Secret 설정
- [ ] 프로덕션 환경에서는 `/actuator` 엔드포인트 비활성화 또는 인증 추가

---

## 📝 환경 변수 설명

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `DB_USERNAME` | PostgreSQL 사용자명 | `todolist_user` |
| `DB_PASSWORD` | PostgreSQL 비밀번호 | `strong_password` |
| `REDIS_PASSWORD` | Redis 비밀번호 | `redis_password` |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | `your_secret_key_here` |
| `JWT_ACCESS_EXPIRATION` | Access Token 유효기간 (ms) | `3600000` (1시간) |
| `JWT_REFRESH_EXPIRATION` | Refresh Token 유효기간 (ms) | `604800000` (7일) |
| `GOOGLE_CLIENT_ID` | Google OAuth2 클라이언트 ID | `xxx.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 클라이언트 시크릿 | `GOCSPX-xxx` |
| `FRONTEND_URL` | 프론트엔드 URL (CORS) | `http://localhost` |

---

## 🌐 클라우드 배포 (Phase 4 예정)

오라클 클라우드 또는 다른 클라우드에 배포 시:
1. `.env` 파일 서버에 업로드
2. `FRONTEND_URL` 수정 (실제 도메인)
3. nginx SSL 설정 추가 (HTTPS)
4. 방화벽 규칙 설정 (포트 80, 443 오픈)

---

**문제가 있으면 Issue를 남겨주세요!** 🙋
