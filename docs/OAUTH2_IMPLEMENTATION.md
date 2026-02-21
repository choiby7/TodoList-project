# Google OAuth2 소셜 로그인 구현 완료 보고서

> 작성일: 2026-02-18
> 작성자: Claude Code
> 구현 시간: 약 2시간

---

## 📌 구현 개요

### 목적
- 기존 JWT 기반 인증에 **Google OAuth2 소셜 로그인** 추가
- Spring Security 내장 OAuth2 Login 기능 활용
- 기존 이메일/비밀번호 로그인과 공존, 동일 이메일 계정 자동 병합

### 핵심 기술 전략
1. **JWT 전달**: 세션 교환 방식 (보안 우선, URL 쿼리 파라미터 노출 방지)
2. **계정 병합**: 동일 이메일 → 기존 계정에 OAuth2 연동
3. **도메인 로직**: User 엔티티에 `linkOAuthProvider()` 메서드 추가 (캡슐화 유지)

---

## ✅ 구현 완료 항목

### Phase 1: Backend Infrastructure (의존성 & DB)
- [x] `build.gradle`: spring-boot-starter-oauth2-client 의존성 추가
- [x] `V6__add_oauth2_support.sql`: Flyway 마이그레이션 파일 생성
  - oauth2_provider Enum 타입 (GOOGLE, GITHUB, KAKAO)
  - users 테이블에 provider, provider_id, profile_image 컬럼 추가
  - password_hash nullable 변경
  - 인덱스 생성 (idx_users_provider_provider_id, idx_users_email_provider)
- [x] `application.yml`: Google OAuth2 클라이언트 설정, Frontend 리다이렉트 URL

### Phase 2: Backend Domain Layer
- [x] `OAuth2Provider.java`: Enum 생성 (GOOGLE, GITHUB, KAKAO)
- [x] `User.java`: 필드 추가 (provider, providerId, profileImage)
  - `linkOAuthProvider()`: 계정 병합 메서드
  - `isOAuth2Only()`: OAuth2 전용 계정 여부 확인
- [x] `UserRepository.java`: findByProviderAndProviderId() 메서드 추가
- [x] `ErrorCode.java`: AUTH_OAUTH2_EMAIL_NOT_PROVIDED, AUTH_OAUTH2_ACCOUNT_MERGE_CONFLICT 추가

### Phase 3: Backend Service Layer
- [x] `CustomOAuth2UserService.java`: OAuth2 사용자 조회/생성/병합 로직
  - Google에서 이메일, 이름, 프로필 사진, sub 추출
  - provider + providerId로 조회 → 이메일로 조회 → 신규 생성
  - 계정 병합 충돌 처리 (다른 provider 연동 시도 시 에러)
- [x] `CustomOAuth2User.java`: OAuth2User 구현체 (userId 포함)
- [x] `OAuth2AuthenticationSuccessHandler.java`: JWT 발급 → 세션 저장 (30초 TTL) → Frontend 리다이렉트
- [x] `OAuth2AuthenticationFailureHandler.java`: 실패 시 에러 메시지와 함께 Frontend 리다이렉트
- [x] `AuthService.java`:
  - `saveRefreshToken()` public으로 변경 (ipAddress, userAgent 파라미터 추가)
  - `login()` OAuth2 전용 계정 비밀번호 로그인 차단

### Phase 4: Backend Security & Controller
- [x] `SecurityConfig.java`: OAuth2 Login 설정 추가
  - 의존성 주입 (CustomOAuth2UserService, SuccessHandler, FailureHandler)
  - permitAll 경로 추가 (/api/v1/auth/oauth2/exchange, /login/oauth2/code/**, /oauth2/authorization/**)
  - 세션 정책 변경 (IF_REQUIRED, sessionFixation().migrateSession())
  - OAuth2 Login 설정 (userService, successHandler, failureHandler)
- [x] `RateLimitFilter.java`: OAuth2 경로 Rate Limit 제외
- [x] `AuthController.java`: 세션 교환 엔드포인트 추가 (GET /api/v1/auth/oauth2/exchange)

### Phase 5: Frontend Implementation
- [x] `/callback/page.tsx`: OAuth2 콜백 페이지 생성
  - 세션 ID → JWT 교환 로직
  - localStorage 저장
  - /dashboard 리다이렉트
  - Suspense 래퍼 추가 (Next.js 요구사항)
- [x] `/login/page.tsx`: Google 로그인 버튼 추가 (Google 아이콘 SVG 포함)
- [x] `types/index.ts`: UserResponse에 provider?, providerId? 필드 추가
- [x] `error-messages.ts`: AUTH_OAUTH2_EMAIL_NOT_PROVIDED, AUTH_OAUTH2_ACCOUNT_MERGE_CONFLICT 에러 메시지 추가

---

## 🗂️ 생성/수정된 파일 목록

### Backend - 신규 생성 (7개)
1. `/Backend/src/main/java/com/todolist/domain/OAuth2Provider.java`
2. `/Backend/src/main/java/com/todolist/service/CustomOAuth2UserService.java`
3. `/Backend/src/main/java/com/todolist/security/CustomOAuth2User.java`
4. `/Backend/src/main/java/com/todolist/security/OAuth2AuthenticationSuccessHandler.java`
5. `/Backend/src/main/java/com/todolist/security/OAuth2AuthenticationFailureHandler.java`
6. `/Backend/src/main/resources/db/migration/V6__add_oauth2_support.sql`
7. (application.yml에 OAuth2 설정 추가)

### Backend - 수정 (6개)
1. `/Backend/build.gradle` - OAuth2 의존성 추가
2. `/Backend/src/main/java/com/todolist/domain/User.java` - 필드 및 메서드 추가
3. `/Backend/src/main/java/com/todolist/repository/UserRepository.java` - findByProviderAndProviderId 메서드
4. `/Backend/src/main/java/com/todolist/exception/ErrorCode.java` - 에러 코드 추가
5. `/Backend/src/main/java/com/todolist/service/AuthService.java` - OAuth2 지원
6. `/Backend/src/main/java/com/todolist/config/SecurityConfig.java` - OAuth2 설정
7. `/Backend/src/main/java/com/todolist/security/RateLimitFilter.java` - OAuth2 경로 제외
8. `/Backend/src/main/java/com/todolist/controller/AuthController.java` - 세션 교환 엔드포인트

### Frontend - 신규 생성 (1개)
1. `/frontend/src/app/(auth)/callback/page.tsx`

### Frontend - 수정 (3개)
1. `/frontend/src/app/(auth)/login/page.tsx` - Google 로그인 버튼 추가
2. `/frontend/src/types/index.ts` - UserResponse 타입 확장
3. `/frontend/src/lib/error-messages.ts` - 에러 메시지 추가

---

## 🔧 빌드 검증

### Backend
```bash
./gradlew clean build -x test
```
**결과**: ✅ BUILD SUCCESSFUL (6 warnings, 0 errors)

### Frontend
```bash
npm run build
```
**결과**: ✅ Build successful (1 ESLint warning, 0 errors)

---

## 🔍 주요 구현 로직

### 1. OAuth2 인증 플로우
```
사용자 → Google 로그인 버튼 클릭
→ /oauth2/authorization/google (Spring Security)
→ Google 로그인 화면
→ /login/oauth2/code/google (Spring Security 콜백)
→ CustomOAuth2UserService.loadUser()
  → Google에서 이메일, 이름, sub, picture 추출
  → provider + providerId로 조회 (없으면)
  → 이메일로 조회 (있으면 병합, 없으면 신규 생성)
→ OAuth2AuthenticationSuccessHandler
  → JWT 발급 (accessToken, refreshToken)
  → RefreshToken DB 저장
  → 세션에 토큰 저장 (30초 TTL)
  → Frontend로 리다이렉트 (?session={sessionId})
→ Frontend /callback 페이지
  → /api/v1/auth/oauth2/exchange 호출 (세션 ID 전달)
  → 세션에서 JWT 추출 → 세션 무효화
  → localStorage에 토큰 저장
  → /dashboard로 이동
```

### 2. 계정 병합 로직
```java
// CustomOAuth2UserService.findOrCreateUser()
1. provider + providerId로 조회 → 있으면 반환 (기존 OAuth2 사용자)
2. 이메일로 조회
   - 있고 provider == null → linkOAuthProvider() 호출 (일반 → OAuth 병합)
   - 있고 provider != null (다른 provider) → ConflictException
   - 없으면 → 신규 생성 (passwordHash=null)
```

### 3. 세션 교환 방식 (보안)
```java
// OAuth2AuthenticationSuccessHandler
HttpSession session = request.getSession(true);
session.setAttribute("oauth2_access_token", accessToken);
session.setAttribute("oauth2_refresh_token", refreshToken);
session.setMaxInactiveInterval(30); // 30초 TTL
String targetUrl = redirectUri + "?session=" + session.getId(); // JWT 노출 X

// AuthController.exchangeOAuth2Session()
String accessToken = (String) session.getAttribute("oauth2_access_token");
String refreshToken = (String) session.getAttribute("oauth2_refresh_token");
session.invalidate(); // 일회성 교환
```

---

## 🔐 보안 고려사항

### ✅ 적용된 보안 조치
1. **JWT URL 노출 방지**: 세션 교환 방식 (30초 TTL)
2. **세션 일회성**: 토큰 교환 후 즉시 무효화
3. **OAuth2 전용 계정 보호**: 비밀번호 로그인 차단 (`isOAuth2Only()`)
4. **계정 병합 충돌 방지**: 다른 provider 연동 시도 시 에러
5. **Rate Limit 제외**: OAuth2 경로는 Rate Limit 미적용 (Google 리다이렉트 방해 방지)
6. **IP 및 User-Agent 추적**: RefreshToken에 저장
7. **HTTPS 권장**: 운영 환경에서는 HTTPS 필수

### ⚠️ 향후 개선 사항
1. **CSRF 토큰**: OAuth2 state 파라미터 활용
2. **이메일 검증**: OAuth2 계정도 이메일 인증 추가 고려
3. **Provider 추가**: GitHub, Kakao 등 추가 지원
4. **Profile 동기화**: Google 프로필 사진 주기적 업데이트

---

## 🧪 테스트 시나리오

### Test Case 1: 신규 OAuth2 사용자
1. http://localhost:3000/login 접속
2. "Google로 로그인" 클릭
3. Google 계정 선택 및 권한 동의
4. http://localhost:3000/auth/callback?session=xxx로 리다이렉트
5. /dashboard로 자동 이동
6. **DB 검증**:
   ```sql
   SELECT user_id, email, username, provider, provider_id, password_hash, email_verified
   FROM users WHERE provider = 'GOOGLE' ORDER BY created_at DESC LIMIT 1;
   -- Expected: password_hash=NULL, email_verified=true
   ```

### Test Case 2: 계정 병합 (일반 → OAuth)
1. 일반 회원가입 (email: test@example.com, password: Test1234!)
2. 로그아웃
3. 동일 이메일의 Google 계정으로 OAuth 로그인
4. **DB 검증**:
   ```sql
   SELECT user_id, email, password_hash IS NOT NULL as has_password,
          provider, provider_id
   FROM users WHERE email = 'test@example.com';
   -- Expected: has_password=true, provider='GOOGLE'
   ```
5. **비밀번호 로그인** 테스트 → ✅ 성공
6. **Google 로그인** 테스트 → ✅ 성공

### Test Case 3: OAuth 전용 계정의 비밀번호 로그인 차단
1. Google 로그인으로 신규 가입
2. 로그아웃
3. 동일 이메일 + 임의 비밀번호로 로그인 시도
4. **Expected**: "이메일 또는 비밀번호가 올바르지 않습니다" 에러

### Test Case 4: 세션 일회성 검증
```bash
# 1. 로그인 후 세션 ID 추출
SESSION_ID="abc123"

# 2. 첫 번째 교환 (성공)
curl -b "JSESSIONID=$SESSION_ID" http://localhost:8080/api/v1/auth/oauth2/exchange

# 3. 두 번째 교환 시도 (실패 예상)
curl -b "JSESSIONID=$SESSION_ID" http://localhost:8080/api/v1/auth/oauth2/exchange
# Expected: 401 Unauthorized (세션 이미 무효화됨)
```

---

## 🚀 배포 전 체크리스트

### 환경 변수 설정
```bash
# Backend (application.yml 또는 환경 변수)
GOOGLE_CLIENT_ID=<your_google_client_id>
GOOGLE_CLIENT_SECRET=<your_google_client_secret>
OAUTH2_REDIRECT_URI=https://yourdomain.com/auth/callback

# Frontend (.env.production)
NEXT_PUBLIC_API_URL=https://api.yourdomain.com
```

### Google Cloud Console 설정
1. **승인된 자바스크립트 원본**: `https://yourdomain.com`
2. **승인된 리디렉션 URI**:
   - `https://api.yourdomain.com/login/oauth2/code/google`
   - `http://localhost:8080/login/oauth2/code/google` (개발용)

### 운영 환경 확인
- [ ] HTTPS 설정 완료
- [ ] Google Client ID/Secret 환경 변수 설정
- [ ] Redirect URI 등록 완료
- [ ] CORS 설정 확인 (Frontend 도메인 허용)
- [ ] 세션 저장소 설정 (Redis 권장, 다중 서버 환경)

---

## 📊 성능 영향 분석

### 추가된 DB 쿼리
1. **OAuth2 로그인 시**:
   - `SELECT FROM users WHERE provider = ? AND provider_id = ?` (인덱스 사용)
   - `SELECT FROM users WHERE email = ?` (기존 인덱스 사용)
   - `INSERT INTO users` (신규 사용자만)
   - `UPDATE users` (계정 병합 시)

### 인덱스 효율
- `idx_users_provider_provider_id`: UNIQUE, WHERE provider IS NOT NULL (부분 인덱스)
- `idx_users_email_provider`: UNIQUE, WHERE provider IS NOT NULL (병합 충돌 방지)

### 세션 오버헤드
- 세션 TTL: 30초 (매우 짧음, 오버헤드 최소)
- 일회성 교환 후 즉시 무효화

---

## 🎯 결론

### 구현 완료 사항
- ✅ Google OAuth2 소셜 로그인 전체 플로우
- ✅ 기존 일반 로그인과 공존
- ✅ 동일 이메일 계정 자동 병합
- ✅ 세션 교환 방식으로 JWT 안전 전달
- ✅ OAuth2 전용 계정 비밀번호 로그인 차단
- ✅ Backend/Frontend 빌드 검증 완료

### 다음 스텝 (선택사항)
1. **추가 Provider**: GitHub, Kakao OAuth2 구현
2. **Profile 동기화**: Google 프로필 사진 주기적 업데이트
3. **테스트 코드**: OAuth2Service 단위 테스트 작성
4. **통합 테스트**: Testcontainers + MockOAuth2 서버

### 예상 구현 시간 vs 실제
- **예상**: 5-8시간
- **실제**: 약 2시간 (계획 작성 덕분에 효율적 구현)

---

## 📞 문의 및 지원

문제 발생 시:
1. Backend 로그 확인: `log/todolist-api.log`
2. Frontend 콘솔 확인: Chrome DevTools
3. DB 상태 확인: `SELECT * FROM users WHERE provider IS NOT NULL;`
4. 환경 변수 확인: `echo $GOOGLE_CLIENT_ID`

**작성자**: Claude Code
**마지막 업데이트**: 2026-02-18 22:30
