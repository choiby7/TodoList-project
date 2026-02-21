-- =====================================================
-- V4: 파티션 생성
-- 작성일: 2026-02-18
-- 설명: activity_logs 월별 파티션 (2026년 + 2027년 Q1)
-- =====================================================

-- 2026년 1월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_01
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- 2026년 2월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_02
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 2026년 3월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_03
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');

-- 2026년 4월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_04
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');

-- 2026년 5월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_05
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

-- 2026년 6월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_06
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

-- 2026년 7월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_07
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

-- 2026년 8월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_08
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

-- 2026년 9월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_09
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

-- 2026년 10월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_10
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

-- 2026년 11월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_11
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

-- 2026년 12월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2026_12
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- 2027년 1월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2027_01
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2027-01-01') TO ('2027-02-01');

-- 2027년 2월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2027_02
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2027-02-01') TO ('2027-03-01');

-- 2027년 3월
CREATE TABLE IF NOT EXISTS todolist_db.activity_logs_2027_03
PARTITION OF todolist_db.activity_logs
FOR VALUES FROM ('2027-03-01') TO ('2027-04-01');

-- 파티션 인덱스 (각 파티션별로 자동 생성됨)
-- created_at, user_id, todo_id, metadata에 대한 인덱스는 V5에서 생성
