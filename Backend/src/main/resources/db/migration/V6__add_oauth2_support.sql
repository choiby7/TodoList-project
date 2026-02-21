-- V6: OAuth2 소셜 로그인 지원 추가
-- 작성일: 2026-02-18
-- 설명: Google OAuth2 로그인을 위한 컬럼 추가 및 password_hash nullable 변경

SET search_path TO todolist_db;

-- 컬럼 추가 (VARCHAR 타입 사용, Hibernate와 호환)
ALTER TABLE todolist_db.users
    ADD COLUMN IF NOT EXISTS provider       VARCHAR(50)  DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS provider_id    VARCHAR(255) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS profile_image  VARCHAR(500) DEFAULT NULL;

-- password_hash nullable 변경 (OAuth2 전용 계정은 비밀번호 없음)
ALTER TABLE todolist_db.users
    ALTER COLUMN password_hash DROP NOT NULL;

-- provider 값 제약조건 (GOOGLE, GITHUB, KAKAO만 허용)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_users_provider'
        AND connamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'todolist_db')
    ) THEN
        ALTER TABLE todolist_db.users
            ADD CONSTRAINT chk_users_provider
            CHECK (provider IS NULL OR provider IN ('GOOGLE', 'GITHUB', 'KAKAO'));
    END IF;
END $$;

-- OAuth2 전용 인덱스 (provider + provider_id 조합으로 빠른 조회)
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_provider_provider_id
    ON todolist_db.users (provider, provider_id)
    WHERE provider IS NOT NULL;

-- 계정 병합 검증을 위한 부분 인덱스 (동일 이메일에 다른 provider 방지)
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_provider
    ON todolist_db.users (email, provider)
    WHERE provider IS NOT NULL;
