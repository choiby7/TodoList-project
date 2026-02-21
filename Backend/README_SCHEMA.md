# 데이터베이스 스키마 관리

## 파일 구조

### 1. `schema_initial.sql`
- **용도**: DB 공장초기화 시 사용하는 전체 스키마
- **내용**: 테이블, 뷰, 함수, 트리거, 인덱스 등 모든 오브젝트
- **실행 방법**: `psql -d todolist_db -f schema_initial.sql`
- **주의**: 이 파일은 이미 ENUM → VARCHAR 변환이 적용된 최신 스키마입니다

### 2. `schema_fix_enum_to_varchar.sql`
- **용도**: 기존 DB에 ENUM 타입이 있을 때 VARCHAR로 마이그레이션
- **실행 시점**: 이미 운영 중인 DB에 적용할 때만 사용
- **실행 방법**: `psql -d todolist_db -f schema_fix_enum_to_varchar.sql`

## DB 공장초기화 방법

### 방법 1: 전체 재생성 (권장)

```bash
# 1. 기존 DB 삭제 (주의: 모든 데이터 삭제됨)
dropdb todolist_db

# 2. DB 재생성
createdb todolist_db

# 3. 초기 스키마 적용
psql -d todolist_db -f Backend/schema_initial.sql
```

### 방법 2: 스키마만 재생성

```bash
# 1. 기존 스키마 삭제
psql -d todolist_db -c "DROP SCHEMA IF EXISTS todolist_db CASCADE;"

# 2. 초기 스키마 적용
psql -d todolist_db -f Backend/schema_initial.sql
```

## 중요 사항

### ❌ 잘못된 방법
```bash
# 이렇게 하지 마세요!
psql -d todolist_db -f old_schema_with_enum.sql        # ENUM으로 생성
psql -d todolist_db -f schema_fix_enum_to_varchar.sql  # VARCHAR로 변환 (불필요한 작업)
```

### ✅ 올바른 방법
```bash
# schema_initial.sql은 이미 VARCHAR로 되어 있으므로 바로 사용
psql -d todolist_db -f schema_initial.sql
```

## 스키마 업데이트 시

새로운 변경사항이 있을 때:
1. `schema_initial.sql` 파일을 직접 수정
2. 운영 DB에는 마이그레이션 스크립트 작성하여 적용
3. 공장초기화는 항상 최신 `schema_initial.sql` 사용

