-- =====================================================
-- PostgreSQL ENUM to VARCHAR Migration Script
-- =====================================================
-- 목적: Hibernate 7.2.1과 PostgreSQL ENUM 타입 호환성 문제 해결
-- 변경일: 2026-02-18
--
-- 문제: Hibernate가 PostgreSQL ENUM 타입으로 매핑하지 못하고 VARCHAR로 전송하여 타입 불일치 에러 발생
-- 해결: ENUM 타입을 VARCHAR로 변경하여 Hibernate의 기본 String 매핑 사용
--
-- 실행 방법: psql -d todolist_db -f schema_fix_enum_to_varchar.sql
-- =====================================================

-- Step 1: 모든 의존 뷰 삭제
DROP VIEW IF EXISTS todolist_db.v_trash_todos CASCADE;
DROP VIEW IF EXISTS todolist_db.v_user_todo_stats CASCADE;
DROP VIEW IF EXISTS todolist_db.v_category_todo_count CASCADE;

-- Step 2: 의존하는 트리거와 함수 삭제
DROP TRIGGER IF EXISTS tg_todos_completed_at ON todolist_db.todos;
DROP FUNCTION IF EXISTS todolist_db.update_completed_at() CASCADE;

-- Step 3: priority 컬럼 타입 변경 (ENUM → VARCHAR)
ALTER TABLE todolist_db.todos
  ALTER COLUMN priority TYPE VARCHAR(10) USING priority::text;

-- Step 4: status 컬럼 타입 변경 (새 컬럼 생성 → 데이터 복사 → 기존 컬럼 삭제 → 이름 변경)
-- 직접 ALTER가 작동하지 않는 경우를 대비한 방법
ALTER TABLE todolist_db.todos ADD COLUMN status_new VARCHAR(20);
UPDATE todolist_db.todos SET status_new = status::text;
ALTER TABLE todolist_db.todos ALTER COLUMN status_new SET NOT NULL;
ALTER TABLE todolist_db.todos DROP COLUMN status;
ALTER TABLE todolist_db.todos RENAME COLUMN status_new TO status;

-- Step 5: update_completed_at 함수 재생성
CREATE OR REPLACE FUNCTION todolist_db.update_completed_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        NEW.completed_at = CURRENT_TIMESTAMP;
    ELSIF NEW.status != 'COMPLETED' AND OLD.status = 'COMPLETED' THEN
        NEW.completed_at = NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Step 6: tg_todos_completed_at 트리거 재생성
CREATE TRIGGER tg_todos_completed_at
  BEFORE UPDATE ON todolist_db.todos
  FOR EACH ROW
  WHEN (old.status IS DISTINCT FROM new.status)
  EXECUTE FUNCTION todolist_db.update_completed_at();

-- Step 7: 뷰 재생성 (ENUM 캐스팅 제거, 문자열 비교로 변경)

-- v_trash_todos 재생성
CREATE OR REPLACE VIEW todolist_db.v_trash_todos AS
  SELECT
    t.todo_id,
    t.user_id,
    t.title,
    t.description,
    t.category_id,
    c.name AS category_name,
    t.priority,
    t.status,
    t.deleted_at,
    EXTRACT(day FROM CURRENT_TIMESTAMP - t.deleted_at::timestamp with time zone)::integer AS days_in_trash,
    GREATEST(0, 30 - EXTRACT(day FROM CURRENT_TIMESTAMP - t.deleted_at::timestamp with time zone)::integer) AS days_until_permanent_delete
  FROM todolist_db.todos t
    LEFT JOIN todolist_db.categories c ON c.category_id = t.category_id
  WHERE t.is_deleted = true
  ORDER BY t.deleted_at DESC;

-- v_user_todo_stats 재생성
CREATE OR REPLACE VIEW todolist_db.v_user_todo_stats AS
  SELECT
    u.user_id,
    u.username,
    u.email,
    COUNT(t.todo_id) AS total_todos,
    COUNT(t.todo_id) FILTER (WHERE t.status = 'COMPLETED') AS completed_todos,
    COUNT(t.todo_id) FILTER (WHERE t.status = 'IN_PROGRESS') AS in_progress_todos,
    COUNT(t.todo_id) FILTER (WHERE t.status = 'TODO') AS pending_todos,
    COUNT(t.todo_id) FILTER (WHERE t.due_date < CURRENT_TIMESTAMP AND t.status <> 'COMPLETED') AS overdue_todos,
    COUNT(t.todo_id) FILTER (WHERE DATE(t.due_date) = CURRENT_DATE) AS today_todos,
    COUNT(t.todo_id) FILTER (WHERE t.is_important = true) AS important_todos,
    ROUND(100.0 * COUNT(t.todo_id) FILTER (WHERE t.status = 'COMPLETED')::numeric / NULLIF(COUNT(t.todo_id), 0)::numeric, 2) AS completion_rate
  FROM todolist_db.users u
    LEFT JOIN todolist_db.todos t ON t.user_id = u.user_id AND t.is_deleted = false
  GROUP BY u.user_id, u.username, u.email;

-- v_category_todo_count 재생성
CREATE OR REPLACE VIEW todolist_db.v_category_todo_count AS
  SELECT
    c.category_id,
    c.user_id,
    c.name AS category_name,
    c.color_code,
    c.icon,
    c.display_order,
    COUNT(t.todo_id) AS total_count,
    COUNT(t.todo_id) FILTER (WHERE t.status = 'COMPLETED') AS completed_count,
    COUNT(t.todo_id) FILTER (WHERE t.status = 'IN_PROGRESS') AS in_progress_count,
    COUNT(t.todo_id) FILTER (WHERE t.status = 'TODO') AS pending_count
  FROM todolist_db.categories c
    LEFT JOIN todolist_db.todos t ON t.category_id = c.category_id AND t.is_deleted = false
  GROUP BY c.category_id, c.user_id, c.name, c.color_code, c.icon, c.display_order
  ORDER BY c.display_order;

-- Step 8: (선택사항) 사용하지 않는 ENUM 타입 삭제
-- 주의: 다른 곳에서 사용 중이면 에러 발생
-- DROP TYPE IF EXISTS todolist_db.priority_level CASCADE;
-- DROP TYPE IF EXISTS todolist_db.todo_status CASCADE;

-- =====================================================
-- 검증 쿼리
-- =====================================================

-- 변경된 컬럼 타입 확인
SELECT
  column_name,
  data_type,
  character_maximum_length
FROM information_schema.columns
WHERE table_schema = 'todolist_db'
  AND table_name = 'todos'
  AND column_name IN ('priority', 'status')
ORDER BY column_name;

-- 뷰 재생성 확인
SELECT table_name
FROM information_schema.views
WHERE table_schema = 'todolist_db'
ORDER BY table_name;

-- 트리거 확인
SELECT tgname
FROM pg_trigger
WHERE tgrelid = 'todolist_db.todos'::regclass
  AND tgname NOT LIKE 'RI_%'
ORDER BY tgname;

-- =====================================================
-- 마이그레이션 완료
-- =====================================================
-- priority: priority_level ENUM → VARCHAR(10)
-- status: todo_status ENUM → VARCHAR(20)
--
-- 주의사항:
-- 1. 기존 ENUM 타입(priority_level, todo_status)은 아직 삭제되지 않았습니다.
-- 2. 다른 테이블이나 함수에서 사용 중이 아니라면 수동으로 삭제 가능합니다.
-- 3. Hibernate 애플리케이션 재시작 후 정상 작동 확인이 필요합니다.
-- =====================================================
