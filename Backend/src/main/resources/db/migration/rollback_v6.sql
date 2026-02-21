-- V6 마이그레이션 롤백 스크립트
-- 실행 방법: psql -U todolist_app -d todolist_db -f rollback_v6.sql

SET search_path TO todolist_db;

-- 1. 인덱스 삭제
DROP INDEX IF EXISTS todolist_db.idx_users_provider_provider_id;
DROP INDEX IF EXISTS todolist_db.idx_users_email_provider;

-- 2. CHECK 제약조건 삭제 (있을 경우)
ALTER TABLE todolist_db.users DROP CONSTRAINT IF EXISTS chk_users_provider;

-- 3. 컬럼 삭제
ALTER TABLE todolist_db.users DROP COLUMN IF EXISTS provider;
ALTER TABLE todolist_db.users DROP COLUMN IF EXISTS provider_id;
ALTER TABLE todolist_db.users DROP COLUMN IF EXISTS profile_image;

-- 4. password_hash를 다시 NOT NULL로 변경 (기존 데이터 확인 필요)
-- 주의: password_hash가 NULL인 사용자가 있으면 실패합니다
-- ALTER TABLE todolist_db.users ALTER COLUMN password_hash SET NOT NULL;

-- 5. Enum 타입 삭제 (있을 경우)
DROP TYPE IF EXISTS todolist_db.oauth2_provider;

-- 6. Flyway 이력 삭제
DELETE FROM todolist_db.flyway_schema_history WHERE version = '6';

-- 완료 메시지
SELECT 'V6 마이그레이션 롤백 완료' AS status;
