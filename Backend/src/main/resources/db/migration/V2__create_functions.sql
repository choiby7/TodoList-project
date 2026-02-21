-- =====================================================
-- V2: 함수 생성
-- 작성일: 2026-02-18
-- 설명: 비즈니스 로직 및 유틸리티 함수, 트리거 함수
-- =====================================================

-- ========== 유틸리티 함수 ==========

-- 만료/폐기된 Refresh Token 삭제
CREATE OR REPLACE FUNCTION todolist_db.cleanup_expired_tokens()
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
    v_deleted INTEGER;
BEGIN
    DELETE FROM todolist_db.refresh_tokens
    WHERE expires_at < CURRENT_TIMESTAMP OR is_revoked = TRUE;

    GET DIAGNOSTICS v_deleted = ROW_COUNT;
    RAISE NOTICE '[cleanup_expired_tokens] % 건 삭제 (%)', v_deleted, CURRENT_TIMESTAMP;
    RETURN v_deleted;
END;
$$;

COMMENT ON FUNCTION todolist_db.cleanup_expired_tokens() IS '만료/폐기된 Refresh Token 삭제 | 권장 주기: 매일 00:00';

-- 지정 일수 이전 활동 로그 삭제
CREATE OR REPLACE FUNCTION todolist_db.cleanup_old_activity_logs(p_days integer DEFAULT 90)
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
    v_deleted INTEGER;
    v_cutoff  TIMESTAMP;
BEGIN
    v_cutoff := CURRENT_TIMESTAMP - (p_days || ' days')::INTERVAL;

    DELETE FROM todolist_db.activity_logs WHERE created_at < v_cutoff;

    GET DIAGNOSTICS v_deleted = ROW_COUNT;
    RAISE NOTICE '[cleanup_old_activity_logs] %일 이전 로그 % 건 삭제 (%)',
                 p_days, v_deleted, CURRENT_TIMESTAMP;
    RETURN v_deleted;
END;
$$;

COMMENT ON FUNCTION todolist_db.cleanup_old_activity_logs(p_days integer) IS '지정 일수(기본 90일) 이전 활동 로그 삭제 | 권장 주기: 매일 00:05';

-- 다음 달 activity_logs 파티션 자동 생성
CREATE OR REPLACE FUNCTION todolist_db.create_next_month_partition()
RETURNS text
LANGUAGE plpgsql
AS $$
DECLARE
    v_next_month     DATE;
    v_month_after    DATE;
    v_partition_name TEXT;
    v_sql            TEXT;
BEGIN
    v_next_month     := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '1 month');
    v_month_after    := v_next_month + INTERVAL '1 month';
    v_partition_name := 'activity_logs_' || TO_CHAR(v_next_month, 'YYYY_MM');

    v_sql := FORMAT(
        'CREATE TABLE IF NOT EXISTS todolist_db.%I '
        'PARTITION OF todolist_db.activity_logs '
        'FOR VALUES FROM (%L) TO (%L)',
        v_partition_name, v_next_month, v_month_after
    );
    EXECUTE v_sql;

    RAISE NOTICE '[create_next_month_partition] 파티션 생성: % (%)',
                 v_partition_name, CURRENT_TIMESTAMP;
    RETURN v_partition_name;
END;
$$;

COMMENT ON FUNCTION todolist_db.create_next_month_partition() IS '다음 달 activity_logs 파티션 자동 생성 | 권장 주기: 매월 1일 00:00';

-- 휴지통 오래된 Todo 영구 삭제
CREATE OR REPLACE FUNCTION todolist_db.empty_trash(p_days integer DEFAULT 30)
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
    v_deleted INTEGER;
    v_cutoff  TIMESTAMP;
BEGIN
    v_cutoff := CURRENT_TIMESTAMP - (p_days || ' days')::INTERVAL;

    DELETE FROM todolist_db.todos
    WHERE is_deleted = TRUE AND deleted_at < v_cutoff;

    GET DIAGNOSTICS v_deleted = ROW_COUNT;
    RAISE NOTICE '[empty_trash] 휴지통 %일 초과 % 건 영구 삭제 (%)',
                 p_days, v_deleted, CURRENT_TIMESTAMP;
    RETURN v_deleted;
END;
$$;

COMMENT ON FUNCTION todolist_db.empty_trash(p_days integer) IS '휴지통 p_days일 초과 Todo 영구 삭제 | 권장 주기: 매일 00:10';

-- 전체 테이블 통계 갱신
CREATE OR REPLACE FUNCTION todolist_db.refresh_statistics()
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
    ANALYZE todolist_db.users;
    ANALYZE todolist_db.categories;
    ANALYZE todolist_db.todos;
    ANALYZE todolist_db.tags;
    ANALYZE todolist_db.todo_tags;
    ANALYZE todolist_db.attachments;
    ANALYZE todolist_db.comments;
    ANALYZE todolist_db.refresh_tokens;
    RAISE NOTICE '[refresh_statistics] 통계 갱신 완료 (%)', CURRENT_TIMESTAMP;
END;
$$;

COMMENT ON FUNCTION todolist_db.refresh_statistics() IS '전체 테이블 통계 갱신 (쿼리 플래너 최적화) | 권장 주기: 매주 일요일 00:00';

-- 잠금 시각 만료 계정 자동 해제
CREATE OR REPLACE FUNCTION todolist_db.unlock_expired_accounts()
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
    v_unlocked INTEGER;
BEGIN
    UPDATE todolist_db.users
       SET locked_until = NULL
     WHERE locked_until IS NOT NULL AND locked_until < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS v_unlocked = ROW_COUNT;
    IF v_unlocked > 0 THEN
        RAISE NOTICE '[unlock_expired_accounts] % 계정 잠금 해제 (%)',
                     v_unlocked, CURRENT_TIMESTAMP;
    END IF;
    RETURN v_unlocked;
END;
$$;

COMMENT ON FUNCTION todolist_db.unlock_expired_accounts() IS '잠금 시각 만료 계정 자동 해제 | 권장 주기: 매시간';


-- ========== 트리거 함수 ==========

-- 로그인 성공 시 실패 카운터 초기화 및 잠금 해제
CREATE OR REPLACE FUNCTION todolist_db.reset_failed_login_on_success()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.last_login_at IS NOT NULL
       AND (OLD.last_login_at IS NULL OR NEW.last_login_at != OLD.last_login_at)
    THEN
        NEW.failed_login_attempts = 0;
        NEW.locked_until          = NULL;
    END IF;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION todolist_db.reset_failed_login_on_success() IS '로그인 성공(last_login_at 갱신) 시 실패 카운터 초기화 및 잠금 해제';

-- Todo 완료 시 completed_at 자동 설정
CREATE OR REPLACE FUNCTION todolist_db.update_completed_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        NEW.completed_at = CURRENT_TIMESTAMP;
    ELSIF NEW.status != 'COMPLETED' AND OLD.status = 'COMPLETED' THEN
        NEW.completed_at = NULL;
    END IF;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION todolist_db.update_completed_at() IS 'status가 COMPLETED로 변경 시 completed_at 자동 설정';

-- 소프트 삭제 시 deleted_at 자동 설정
CREATE OR REPLACE FUNCTION todolist_db.update_deleted_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.is_deleted = TRUE AND OLD.is_deleted = FALSE THEN
        NEW.deleted_at = CURRENT_TIMESTAMP;
    ELSIF NEW.is_deleted = FALSE AND OLD.is_deleted = TRUE THEN
        NEW.deleted_at = NULL;
    END IF;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION todolist_db.update_deleted_at() IS 'is_deleted 변경 시 deleted_at 자동 설정 / 해제';

-- UPDATE 시 updated_at 자동 갱신
CREATE OR REPLACE FUNCTION todolist_db.update_updated_at_column()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION todolist_db.update_updated_at_column() IS 'UPDATE 실행 시 updated_at을 현재 시각으로 자동 갱신';
