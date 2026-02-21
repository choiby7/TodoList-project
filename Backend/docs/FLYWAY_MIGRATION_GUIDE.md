# Flyway 마이그레이션 가이드

> 작성일: 2026-02-18
> 목적: 기존 수동 스키마를 Flyway 버전 관리로 전환

---

## 📋 목차

1. [개요](#개요)
2. [마이그레이션 파일 구조](#마이그레이션-파일-구조)
3. [적용 방법](#적용-방법)
4. [검증](#검증)
5. [문제 해결](#문제-해결)

---

## 개요

### 현재 상태
- **Before**: 수동으로 생성된 PostgreSQL 스키마 (DDL 스크립트)
- **After**: Flyway로 관리되는 버전 관리 스키마

### 왜 Flyway?
- ✅ 스키마 버전 관리
- ✅ 팀 간 DB 동기화
- ✅ 배포 자동화
- ✅ 변경 이력 추적
- ✅ 롤백 가능 (Pro 버전)

### Flyway 설정 요약
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true  # 기존 DB가 있는 경우 자동 baseline
    baseline-version: 0
    locations: classpath:db/migration
    schemas: todolist_db
    validate-on-migrate: true
```

---

## 마이그레이션 파일 구조

### 파일 위치
```
Backend/src/main/resources/db/migration/
├── V1__create_schema_and_types.sql
├── V2__create_functions.sql
├── V3__create_tables.sql
├── V4__create_partitions.sql
└── V5__create_triggers.sql
```

### 파일 설명

#### V1__create_schema_and_types.sql
- todolist_db 스키마 생성
- ENUM 타입 (activity_type, priority_level, todo_status)

#### V2__create_functions.sql
- 유틸리티 함수 (cleanup_expired_tokens, empty_trash 등)
- 트리거 함수 (update_updated_at, update_completed_at 등)

#### V3__create_tables.sql
- 주요 테이블 (users, categories, todos, tags, attachments 등)
- 외래 키 제약조건
- 기본 인덱스

#### V4__create_partitions.sql
- activity_logs 월별 파티션 (2026년 전체 + 2027년 Q1)

#### V5__create_triggers.sql
- updated_at 자동 갱신 트리거
- 비즈니스 로직 트리거 (completed_at, deleted_at 등)

---

## 적용 방법

### 방법 1: 기존 DB 유지 (Baseline 사용) ⭐ 권장

기존 데이터를 유지하면서 Flyway 적용

#### Step 1: Flyway 테이블 확인
```sql
-- Flyway가 자동으로 생성하는 메타데이터 테이블
SELECT * FROM todolist_db.flyway_schema_history;
```

#### Step 2: 애플리케이션 시작
```bash
cd Backend
./gradlew bootRun
```

Flyway가 자동으로:
1. 기존 스키마를 버전 0으로 baseline 설정
2. V1~V5 마이그레이션은 "이미 적용됨"으로 간주
3. 향후 새 마이그레이션(V6 이상)만 적용

#### Step 3: 검증
```sql
-- Flyway 히스토리 확인
SELECT version, description, type, installed_on, success
FROM todolist_db.flyway_schema_history
ORDER BY installed_rank;
```

**예상 결과:**
```
version | description                | type     | installed_on         | success
--------+---------------------------+----------+---------------------+---------
0       | << Flyway Baseline >>      | BASELINE | 2026-02-18 10:00:00 | true
1       | create schema and types    | SQL      | 2026-02-18 10:00:01 | true
2       | create functions           | SQL      | 2026-02-18 10:00:01 | true
3       | create tables              | SQL      | 2026-02-18 10:00:02 | true
4       | create partitions          | SQL      | 2026-02-18 10:00:02 | true
5       | create triggers            | SQL      | 2026-02-18 10:00:03 | true
```

---

### 방법 2: 깨끗한 시작 (스키마 재생성)

**⚠️ 주의: 모든 데이터가 삭제됩니다!**

#### Step 1: 기존 스키마 백업 (선택사항)
```bash
PGPASSWORD='MySecure2026!Password' pg_dump \
  -h localhost -U todolist_app -d todolist_db \
  -n todolist_db \
  -f ~/todolist_backup_$(date +%Y%m%d).sql
```

#### Step 2: 기존 스키마 삭제
```sql
DROP SCHEMA IF EXISTS todolist_db CASCADE;
```

#### Step 3: application.yml 수정 (baseline 비활성화)
```yaml
spring:
  flyway:
    baseline-on-migrate: false  # 새 DB이므로 baseline 불필요
```

#### Step 4: 애플리케이션 시작
```bash
./gradlew bootRun
```

Flyway가 자동으로:
1. V1부터 순차적으로 마이그레이션 실행
2. 깨끗한 스키마 생성

---

## 검증

### 1. Flyway 메타데이터 확인
```sql
SELECT version, description, type, success
FROM todolist_db.flyway_schema_history
ORDER BY installed_rank;
```

### 2. 테이블 존재 확인
```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'todolist_db'
ORDER BY table_name;
```

**예상 테이블 목록:**
- activity_logs (+ 파티션들)
- attachments
- categories
- comments
- flyway_schema_history
- refresh_tokens
- tags
- todo_tags
- todos
- users

### 3. 함수 존재 확인
```sql
SELECT routine_name
FROM information_schema.routines
WHERE routine_schema = 'todolist_db'
ORDER BY routine_name;
```

### 4. 트리거 존재 확인
```sql
SELECT trigger_name, event_object_table
FROM information_schema.triggers
WHERE trigger_schema = 'todolist_db'
ORDER BY event_object_table, trigger_name;
```

### 5. 애플리케이션 로그 확인
```
Flyway Community Edition ... by Redgate
Database: jdbc:postgresql://localhost:5432/todolist_db (PostgreSQL 18.2)
Successfully validated 5 migrations (execution time 00:00.123s)
Creating Schema History table "todolist_db"."flyway_schema_history" ...
Current version of schema "todolist_db": << Empty Schema >>
Migrating schema "todolist_db" to version "1 - create schema and types"
Migrating schema "todolist_db" to version "2 - create functions"
...
Successfully applied 5 migrations to schema "todolist_db" (execution time 00:00.456s)
```

---

## 문제 해결

### 문제 1: "Schema todolist_db already exists"
**원인:** 기존 스키마가 있지만 baseline이 설정되지 않음
**해결:**
```yaml
# application.yml
spring:
  flyway:
    baseline-on-migrate: true
```

### 문제 2: "Checksum mismatch"
**원인:** 마이그레이션 파일이 수정됨
**해결 (개발 환경만):**
```sql
-- Flyway 히스토리 초기화 (⚠️ 운영 절대 금지)
DELETE FROM todolist_db.flyway_schema_history;
```

### 문제 3: "Migration V3 failed"
**원인:** 테이블이 이미 존재
**해결:**
- 방법 1의 baseline 사용
- 또는 기존 스키마 삭제 후 재실행

### 문제 4: "Type already exists"
**원인:** ENUM 타입이 이미 존재
**해결:**
```sql
-- 마이그레이션 파일에서 CREATE TYPE을 CREATE TYPE IF NOT EXISTS로 변경
-- 또는 기존 타입 삭제
DROP TYPE IF EXISTS todolist_db.activity_type CASCADE;
```

---

## 향후 마이그레이션 추가

### 새 마이그레이션 생성 규칙

1. **파일명 규칙**
   ```
   V{번호}__{설명}.sql
   예: V6__add_user_avatar_column.sql
   ```

2. **멱등성 보장**
   ```sql
   -- 좋은 예
   ALTER TABLE todolist_db.users
   ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);

   -- 나쁜 예
   ALTER TABLE todolist_db.users
   ADD COLUMN avatar_url VARCHAR(500);  -- 재실행 시 에러
   ```

3. **트랜잭션 분리**
   ```sql
   -- 각 마이그레이션은 자동으로 트랜잭션 내에서 실행
   -- DDL과 DML을 섞을 때 주의
   ```

4. **테스트 환경 먼저**
   ```bash
   # 로컬 → 개발 → 스테이징 → 운영 순서로 적용
   ```

### 예시: V6 마이그레이션 추가

```sql
-- V6__add_user_preferences.sql
-- 사용자 설정 테이블 추가

CREATE TABLE IF NOT EXISTS todolist_db.user_preferences (
    user_id BIGINT PRIMARY KEY,
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'ko',
    timezone VARCHAR(50) DEFAULT 'Asia/Seoul',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_preferences_user
        FOREIGN KEY (user_id)
        REFERENCES todolist_db.users(user_id)
        ON DELETE CASCADE
);

-- 트리거 추가
CREATE TRIGGER tg_user_preferences_updated_at
    BEFORE UPDATE ON todolist_db.user_preferences
    FOR EACH ROW
    EXECUTE FUNCTION todolist_db.update_updated_at_column();
```

---

## 참고 자료

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [CLAUDE.md - DB 마이그레이션 섹션](../CLAUDE.md#db-마이그레이션)

---

## 체크리스트

마이그레이션 적용 전:
- [ ] 기존 DB 백업 완료
- [ ] Flyway 의존성 추가 확인
- [ ] application.yml Flyway 설정 확인
- [ ] 마이그레이션 파일 5개 확인

마이그레이션 적용 후:
- [ ] flyway_schema_history 테이블 확인
- [ ] 모든 테이블 존재 확인
- [ ] 함수/트리거 동작 확인
- [ ] 애플리케이션 정상 구동 확인
- [ ] 기존 데이터 유지 확인 (방법 1 사용 시)
