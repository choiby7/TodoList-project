-- =====================================================
-- V5: 트리거 생성
-- 작성일: 2026-02-18
-- 설명: 자동 타임스탬프 업데이트, 비즈니스 로직 트리거
-- =====================================================

-- ========== updated_at 자동 갱신 트리거 ==========

-- users 테이블
CREATE TRIGGER tg_users_updated_at
    BEFORE UPDATE ON todolist_db.users
    FOR EACH ROW
    EXECUTE FUNCTION todolist_db.update_updated_at_column();

-- categories 테이블
CREATE TRIGGER tg_categories_updated_at
    BEFORE UPDATE ON todolist_db.categories
    FOR EACH ROW
    EXECUTE FUNCTION todolist_db.update_updated_at_column();

-- todos 테이블
CREATE TRIGGER tg_todos_updated_at
    BEFORE UPDATE ON todolist_db.todos
    FOR EACH ROW
    EXECUTE FUNCTION todolist_db.update_updated_at_column();

-- comments 테이블
CREATE TRIGGER tg_comments_updated_at
    BEFORE UPDATE ON todolist_db.comments
    FOR EACH ROW
    EXECUTE FUNCTION todolist_db.update_updated_at_column();


-- ========== 비즈니스 로직 트리거 ==========

-- Todo 완료 상태 변경 시 completed_at 자동 설정
CREATE TRIGGER tg_todos_completed_at
    BEFORE UPDATE ON todolist_db.todos
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION todolist_db.update_completed_at();

-- Todo 소프트 삭제 시 deleted_at 자동 설정
CREATE TRIGGER tg_todos_deleted_at
    BEFORE UPDATE ON todolist_db.todos
    FOR EACH ROW
    WHEN (OLD.is_deleted IS DISTINCT FROM NEW.is_deleted)
    EXECUTE FUNCTION todolist_db.update_deleted_at();

-- 로그인 성공 시 실패 카운터 초기화 및 잠금 해제
CREATE TRIGGER tg_users_reset_failed_login
    BEFORE UPDATE ON todolist_db.users
    FOR EACH ROW
    EXECUTE FUNCTION todolist_db.reset_failed_login_on_success();
