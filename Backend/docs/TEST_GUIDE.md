# 테스트 가이드

## 작성된 테스트

### 1. TodoServiceTest (단위 테스트)
위치: `src/test/java/com/todolist/service/TodoServiceTest.java`

**테스트 케이스:**
- ✅ Todo 생성 - 성공
- ✅ Todo 목록 조회 - 필터 없이 전체 조회 성공
- ✅ Todo 목록 조회 - status 필터링 성공
- ✅ Todo 목록 조회 - priority 필터링 성공
- ✅ Todo 목록 조회 - categoryId 필터링 성공
- ✅ Todo 상세 조회 - 성공
- ✅ Todo 상세 조회 - 존재하지 않는 ID로 조회 시 예외 발생
- ✅ Todo 상세 조회 - 다른 사용자의 Todo 접근 시 403 예외
- ✅ Todo 완료 토글 - 성공
- ✅ Todo 소프트 삭제 - 성공

**특징:**
- Mockito를 사용한 순수 단위 테스트
- SecurityContext 모킹으로 인증 처리
- JPA Specification을 사용한 동적 쿼리 테스트
- Given-When-Then 패턴 준수

## 테스트 실행 방법

### 전체 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 클래스 실행
```bash
./gradlew test --tests "TodoServiceTest"
```

### 특정 테스트 메서드 실행
```bash
./gradlew test --tests "TodoServiceTest.createTodo_WhenValidRequest_ShouldReturnTodoResponse"
```

### 테스트 커버리지 확인
```bash
./gradlew test jacocoTestReport
```

## 향후 추가 예정

### Repository 통합 테스트 (Testcontainers)
- PostgreSQL Testcontainers를 사용한 실제 DB 통합 테스트
- Specification 동적 쿼리의 실제 동작 검증
- 복잡한 쿼리 조건 조합 테스트

### Controller 테스트 (MockMvc)
- API 엔드포인트 테스트
- 요청/응답 JSON 검증
- 인증/인가 테스트

## 테스트 설정

### application-test.yml
- H2 인메모리 데이터베이스 사용
- PostgreSQL 호환 모드
- Flyway 비활성화 (ddl-auto: create-drop 사용)

## 주의사항

1. **PostgreSQL ENUM 타입**
   - 통합 테스트에서 PostgreSQL ENUM 타입 사용 시 H2에서 호환 문제 발생
   - Testcontainers 사용 권장

2. **SecurityContext 모킹**
   - 각 테스트에서 `@BeforeEach`로 SecurityContext 설정 필요
   - 현재 사용자 ID는 `TEST_USER_ID = 1L` 사용

3. **JPA Specification 테스트**
   - `any(Specification.class)`로 모킹
   - 실제 Specification 로직은 통합 테스트에서 검증 필요
