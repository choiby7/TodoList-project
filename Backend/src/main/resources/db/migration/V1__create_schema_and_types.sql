-- =====================================================
-- V1: 스키마 및 ENUM 타입 생성
-- 작성일: 2026-02-18
-- 설명: TodoList 서비스의 스키마와 커스텀 타입 정의
-- =====================================================

-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS todolist_db;

COMMENT ON SCHEMA todolist_db IS 'TodoList 서비스 전용 스키마 — public 스키마 미사용';

-- ENUM 타입: activity_type
CREATE TYPE todolist_db.activity_type AS ENUM (
    'CREATE',
    'UPDATE',
    'DELETE',
    'COMPLETE',
    'REOPEN'
);

COMMENT ON TYPE todolist_db.activity_type IS '활동 로그 타입 (생성, 수정, 삭제, 완료, 재오픈)';

-- ENUM 타입: priority_level
CREATE TYPE todolist_db.priority_level AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH'
);

COMMENT ON TYPE todolist_db.priority_level IS 'Todo 우선순위 (낮음, 중간, 높음)';

-- ENUM 타입: todo_status
CREATE TYPE todolist_db.todo_status AS ENUM (
    'TODO',
    'IN_PROGRESS',
    'COMPLETED'
);

COMMENT ON TYPE todolist_db.todo_status IS 'Todo 상태 (할 일, 진행 중, 완료)';
