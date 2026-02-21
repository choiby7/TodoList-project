# Redis 아키텍처 및 구현 가이드

> TodoList 프로젝트에서 Redis가 어떻게 적용되었는지 이해하기 쉽게 정리한 문서
> 작성일: 2026-02-18

---

## 📌 Redis란?

**Redis (Remote Dictionary Server)**
- **In-Memory 데이터 저장소**: 데이터를 메모리(RAM)에 저장하여 매우 빠름
- **Key-Value 구조**: 간단한 키-값 구조로 데이터 저장
- **용도**: 캐싱, 세션 관리, Rate Limiting, 실시간 분석 등

### PostgreSQL vs Redis 비교

| 구분 | PostgreSQL | Redis |
|------|-----------|-------|
| **저장 위치** | 디스크 (HDD/SSD) | **메모리 (RAM)** |
| **속도** | 느림 (밀리초) | **매우 빠름 (마이크로초)** |
| **데이터 구조** | 테이블, 관계 | **Key-Value** |
| **용도** | **영구 데이터** (Todo, User) | **임시 데이터** (Cache, Rate Limit) |
| **데이터 손실** | 안전 (디스크 저장) | 가능 (메모리 기반) |

---

## 🏗️ 전체 아키텍처에서 Redis의 위치

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Next.js)                      │
│  - React Components                                          │
│  - Axios API Client                                          │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP Request
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                   Backend (Spring Boot)                      │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  1. RateLimitFilter (Rate Limit 체크)                │  │
│  │     ↓                                                  │  │
│  │  2. JwtAuthenticationFilter (인증 확인)               │  │
│  │     ↓                                                  │  │
│  │  3. Controller → Service → Repository                 │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                               │
│         ↙                              ↘                     │
└────────┼────────────────────────────────┼─────────────────────┘
         │                                │
         ↓                                ↓
┌─────────────────┐            ┌──────────────────┐
│  PostgreSQL     │            │  Redis           │
│  (영구 데이터)   │            │  (임시 데이터)    │
├─────────────────┤            ├──────────────────┤
│ • users         │            │ • Rate Limit     │
│ • todos         │            │ • Cache (예정)   │
│ • categories    │            │ • RefreshToken   │
│ • refresh_tokens│            │   (예정)         │
└─────────────────┘            └──────────────────┘
   영구 저장                      빠른 읽기/쓰기
   (디스크)                       (메모리)
```

---

## 🔧 현재 구현된 Redis 기능

### 1. Rate Limiting (속도 제한)

#### 목적
- **보안**: Brute Force 공격 방어
- **안정성**: 서버 과부하 방지

#### 동작 방식

```
┌─────────────┐
│ 사용자 로그인│
│   시도      │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────────────────────┐
│ RateLimitFilter                             │
│                                             │
│ 1. IP 주소 추출: 127.0.0.1                  │
│ 2. Redis 키 생성: "rate_limit:login:127..."│
│ 3. Redis 카운트 증가: INCR                  │
│ 4. 현재 카운트 확인                          │
└──────┬───────────────────────┬──────────────┘
       │                       │
   카운트 <= 5              카운트 > 5
       │                       │
       ↓                       ↓
┌──────────────┐      ┌─────────────────┐
│ 로그인 허용   │      │ 429 에러 반환   │
│ 다음 필터로   │      │ "한도 초과"     │
└──────────────┘      └─────────────────┘
```

#### Redis 데이터 구조

```
키: rate_limit:login:127.0.0.1
값: 6 (시도 횟수)
TTL: 900초 (15분)

# Redis CLI에서 확인
KEYS rate_limit:*
GET rate_limit:login:127.0.0.1  → "6"
TTL rate_limit:login:127.0.0.1  → 899 (남은 초)
```

#### Rate Limit 설정

| 엔드포인트 | 제한 | 기준 | Redis 키 패턴 |
|-----------|------|------|---------------|
| 로그인 | 5회/15분 | IP | `rate_limit:login:{IP}` |
| 회원가입 | 3회/1시간 | IP | `rate_limit:signup:{IP}` |
| Todo CRUD | 100회/1분 | User ID | `rate_limit:todo:{userId}` |

---

### 2. Redis 캐싱 (다음 구현 예정)

#### 목적
- **성능 향상**: DB 조회 횟수 감소
- **응답 속도**: 밀리초 → 마이크로초

#### 동작 방식 (예정)

```
사용자: 카테고리 목록 요청
         ↓
┌─────────────────────────────────────────┐
│ CategoryService.getCategories()         │
│                                         │
│ 1. Redis에 캐시 있나? (키: categories:1)│
└──────┬──────────────────┬───────────────┘
       │                  │
   있음 (HIT)          없음 (MISS)
       │                  │
       ↓                  ↓
┌──────────────┐   ┌─────────────────────┐
│ Redis에서    │   │ PostgreSQL 조회     │
│ 즉시 반환    │   │ → Redis 저장        │
│ (0.1ms)     │   │ → 클라이언트 반환   │
└──────────────┘   └─────────────────────┘
  매우 빠름!           첫 요청만 느림
```

---

## 💻 코드 구조

### Backend 구조

```
Backend/src/main/java/com/todolist/
├── config/
│   └── RedisConfig.java               # Redis 설정
│       ├── redisConnectionFactory()   # Redis 연결
│       ├── redisTemplate()            # Rate Limiting용
│       ├── cacheManager()             # 캐싱용
│       └── objectMapper()             # JSON 직렬화
│
├── service/
│   └── RateLimiterService.java        # Rate Limit 로직
│       ├── allowLoginAttempt()        # 로그인 체크
│       ├── allowSignupAttempt()       # 회원가입 체크
│       └── allowTodoCrudOperation()   # Todo CRUD 체크
│
├── security/
│   └── RateLimitFilter.java           # HTTP 필터
│       └── doFilterInternal()         # 요청 가로채기
│
└── exception/
    └── ErrorCode.java
        └── COMMON_RATE_LIMIT_EXCEEDED # 429 에러 코드
```

### RedisConfig 핵심 코드

```java
@Configuration
@EnableCaching
public class RedisConfig {

    // Redis 연결 (localhost:6379)
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        return new LettuceConnectionFactory(config);
    }

    // Rate Limiting용 RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(...) {
        // String Key, JSON Value
    }

    // 캐싱용 CacheManager (TTL 10분)
    @Bean
    public CacheManager cacheManager(...) {
        // @Cacheable 어노테이션 지원
    }
}
```

### RateLimiterService 핵심 코드

```java
@Service
public class RateLimiterService {

    // 토큰 버킷 알고리즘
    private boolean checkRateLimit(String key, int limit, Duration duration) {
        // 1. Redis 카운트 증가
        Long count = redisTemplate.opsForValue().increment(key, 1);

        // 2. 첫 요청이면 TTL 설정
        if (count == 1) {
            redisTemplate.expire(key, duration);
        }

        // 3. 제한 초과 여부 반환
        return count <= limit;
    }
}
```

---

## 🔍 실제 동작 흐름

### 로그인 6번 연속 시도 시나리오

```
1. 첫 번째 로그인 시도 (잘못된 비밀번호)
   ┌─────────────────────────────────────┐
   │ RateLimitFilter                     │
   │ - IP: 127.0.0.1 추출                │
   │ - Redis: INCR rate_limit:login:127  │
   │   → 1                               │
   │ - 1 <= 5 → 허용 ✅                  │
   └─────────────────────────────────────┘
   → AuthController
   → AUTH_INVALID_CREDENTIALS 에러
   → Frontend Toast: "로그인 실패"

2~5. 두 번째~다섯 번째 시도
   → Redis 카운트: 2, 3, 4, 5
   → 모두 허용 ✅

6. 여섯 번째 로그인 시도
   ┌─────────────────────────────────────┐
   │ RateLimitFilter                     │
   │ - Redis: INCR rate_limit:login:127  │
   │   → 6                               │
   │ - 6 > 5 → 차단 ❌                   │
   │ - 429 에러 반환                      │
   └─────────────────────────────────────┘
   → Frontend Toast: "로그인 시도 한도를 초과했습니다. 15분 후 다시 시도해주세요"

7. 15분 후
   - Redis TTL 만료
   - rate_limit:login:127.0.0.1 키 자동 삭제
   - 다시 로그인 가능 ✅
```

---

## 🛠️ Redis 관리 명령어

### Docker Redis 제어

```bash
# Redis 시작
docker start redis

# Redis 중지
docker stop redis

# Redis 삭제
docker rm -f redis

# Redis 재시작
docker restart redis

# Redis 로그 확인
docker logs redis
```

### Redis CLI 명령어

```bash
# Redis CLI 접속
docker exec -it redis redis-cli

# Rate Limit 키 확인
KEYS rate_limit:*

# 특정 키 값 확인
GET rate_limit:login:127.0.0.1

# TTL 확인 (남은 시간, 초)
TTL rate_limit:login:127.0.0.1

# 모든 데이터 삭제 (테스트용)
FLUSHDB

# 특정 키 삭제
DEL rate_limit:login:127.0.0.1

# 종료
exit
```

---

## 📊 성능 비교

### Rate Limit 체크 성능

| 방식 | 평균 응답 시간 | 분산 환경 | 서버 재시작 시 |
|------|---------------|----------|---------------|
| **Redis** | **0.1~0.5ms** | ✅ 지원 | ✅ 데이터 유지 |
| 메모리 (HashMap) | 0.01ms | ❌ 불가 | ❌ 데이터 손실 |

### 캐싱 성능 (예상)

| 시나리오 | PostgreSQL | Redis 캐시 | 성능 향상 |
|---------|-----------|-----------|----------|
| 카테고리 목록 조회 | 10ms | 0.1ms | **100배** |
| 통계 데이터 조회 | 50ms | 0.5ms | **100배** |

---

## 🎯 다음 단계: Redis 캐싱 구현

### 구현 예정

```java
@Service
public class CategoryService {

    // 캐싱 적용 (TTL 10분)
    @Cacheable(value = "categories", key = "#userId")
    public List<CategoryResponse> getCategories(Long userId) {
        // 첫 요청: PostgreSQL 조회 → Redis 저장
        // 이후 요청: Redis에서 즉시 반환 (100배 빠름)
    }

    // 캐시 무효화
    @CacheEvict(value = "categories", key = "#userId")
    public void createCategory(Long userId, ...) {
        // 카테고리 생성 시 캐시 삭제 → 다음 조회 시 재생성
    }
}
```

---

## 📝 요약

### Redis의 역할

1. **Rate Limiting** ✅ 완료
   - 속도 제한 카운팅
   - TTL 자동 만료
   - 분산 환경 지원

2. **캐싱** ⏭️ 다음 작업
   - 카테고리 목록
   - 통계 데이터
   - 100배 빠른 응답

3. **RefreshToken 저장** 📋 계획
   - PostgreSQL → Redis 전환
   - 빠른 조회/삭제

### 전체 데이터 플로우

```
영구 데이터 (PostgreSQL)
  ↓
  └─→ Redis 캐싱 (첫 조회 시)
        ↓
        └─→ 이후 조회는 Redis에서 즉시 반환 (빠름!)

Rate Limit 데이터 (Redis만)
  - 생성 → TTL 설정
  - 자동 만료
  - PostgreSQL 저장 안 함
```

---

## 🔗 관련 문서

- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - 전체 구현 현황
- [CLAUDE.md](../CLAUDE.md) - 프로젝트 가이드
- [Backend README](../Backend/README.md) - 백엔드 실행 가이드
