-- V7: 약관 동의 기능 추가
-- 작성일: 2026-02-18
-- 설명: 서비스 이용약관 및 개인정보 처리방침 동의 일시 추가

SET search_path TO todolist_db;

-- 약관 동의 일시 컬럼 추가
ALTER TABLE todolist_db.users
    ADD COLUMN IF NOT EXISTS terms_agreed_at    TIMESTAMP DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS privacy_agreed_at  TIMESTAMP DEFAULT NULL;

-- 기존 사용자는 자동 동의 처리 (마이그레이션 시점)
UPDATE todolist_db.users
SET terms_agreed_at = created_at,
    privacy_agreed_at = created_at
WHERE terms_agreed_at IS NULL;

-- 인덱스 추가 (약관 미동의 사용자 빠른 조회)
CREATE INDEX IF NOT EXISTS idx_users_terms_agreed
    ON todolist_db.users (terms_agreed_at)
    WHERE terms_agreed_at IS NULL;
