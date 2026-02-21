-- =====================================================
-- V3: 테이블 생성
-- 작성일: 2026-02-18
-- 설명: 사용자, 카테고리, Todo, 태그, 첨부파일 등 주요 테이블
-- =====================================================

-- ========== 사용자 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.users (
    user_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email character varying(255) NOT NULL UNIQUE,
    password_hash character varying(255) NOT NULL,
    username character varying(100) NOT NULL,
    profile_image_url character varying(500),
    is_active boolean DEFAULT true NOT NULL,
    email_verified boolean DEFAULT false NOT NULL,
    failed_login_attempts integer DEFAULT 0 NOT NULL,
    locked_until timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login_at timestamp without time zone,
    CONSTRAINT chk_users_email_format
        CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_users_failed_attempts
        CHECK (failed_login_attempts >= 0 AND failed_login_attempts <= 10),
    CONSTRAINT chk_users_username_length
        CHECK (length(username) >= 2 AND length(username) <= 100)
);

COMMENT ON TABLE todolist_db.users IS '사용자 정보';
COMMENT ON COLUMN todolist_db.users.email_verified IS '이메일 인증 여부 (기본 false)';
COMMENT ON COLUMN todolist_db.users.failed_login_attempts IS '로그인 실패 횟수 (5회 이상 시 잠금)';
COMMENT ON COLUMN todolist_db.users.locked_until IS '계정 잠금 해제 시각 (NULL이면 잠금 없음)';


-- ========== 카테고리 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.categories (
    category_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id bigint NOT NULL,
    name character varying(50) NOT NULL,
    color_code character varying(7) DEFAULT '#3B82F6'::character varying NOT NULL,
    icon character varying(50),
    display_order integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id)
        REFERENCES todolist_db.users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_categories_user_name UNIQUE (user_id, name),
    CONSTRAINT chk_categories_color_format
        CHECK (color_code ~* '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT chk_categories_name_length
        CHECK (length(name) >= 1 AND length(name) <= 50)
);

CREATE INDEX idx_categories_user_id ON todolist_db.categories(user_id);

COMMENT ON TABLE todolist_db.categories IS '사용자 정의 카테고리';
COMMENT ON COLUMN todolist_db.categories.color_code IS 'HEX 색상 코드 (기본 #3B82F6)';
COMMENT ON COLUMN todolist_db.categories.display_order IS '표시 순서 (낮을수록 앞)';


-- ========== Todo 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.todos (
    todo_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id bigint NOT NULL,
    category_id bigint,
    title character varying(200) NOT NULL,
    description text,
    priority todolist_db.priority_level DEFAULT 'MEDIUM'::todolist_db.priority_level NOT NULL,
    status todolist_db.todo_status DEFAULT 'TODO'::todolist_db.todo_status NOT NULL,
    due_date timestamp without time zone,
    is_important boolean DEFAULT false NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    display_order integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at timestamp without time zone,
    deleted_at timestamp without time zone,
    CONSTRAINT fk_todos_user FOREIGN KEY (user_id)
        REFERENCES todolist_db.users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_todos_category FOREIGN KEY (category_id)
        REFERENCES todolist_db.categories(category_id) ON DELETE SET NULL,
    CONSTRAINT chk_todos_title_length
        CHECK (length(title) >= 1 AND length(title) <= 200),
    CONSTRAINT chk_todos_description_length
        CHECK (length(description) <= 5000)
);

-- Todo 주요 인덱스
CREATE INDEX idx_todos_user_id ON todolist_db.todos(user_id) WHERE is_deleted = false;
CREATE INDEX idx_todos_user_status ON todolist_db.todos(user_id, status) WHERE is_deleted = false;
CREATE INDEX idx_todos_user_priority ON todolist_db.todos(user_id, priority) WHERE is_deleted = false;
CREATE INDEX idx_todos_category_id ON todolist_db.todos(category_id) WHERE is_deleted = false;
CREATE INDEX idx_todos_due_date ON todolist_db.todos(due_date) WHERE is_deleted = false AND due_date IS NOT NULL;
CREATE INDEX idx_todos_deleted ON todolist_db.todos(user_id, deleted_at) WHERE is_deleted = true;

COMMENT ON TABLE todolist_db.todos IS '할 일 목록';
COMMENT ON COLUMN todolist_db.todos.is_important IS '중요 표시 여부';
COMMENT ON COLUMN todolist_db.todos.is_deleted IS '소프트 삭제 여부';
COMMENT ON COLUMN todolist_db.todos.display_order IS '표시 순서 (낮을수록 앞)';


-- ========== 태그 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.tags (
    tag_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id bigint NOT NULL,
    name character varying(30) NOT NULL,
    color_code character varying(7) DEFAULT '#6B7280'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id)
        REFERENCES todolist_db.users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_tags_user_name UNIQUE (user_id, name),
    CONSTRAINT chk_tags_color_format
        CHECK (color_code ~* '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT chk_tags_name_length
        CHECK (length(name) >= 1 AND length(name) <= 30)
);

CREATE INDEX idx_tags_user_id ON todolist_db.tags(user_id);

COMMENT ON TABLE todolist_db.tags IS '사용자 정의 태그';


-- ========== Todo-Tag 연결 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.todo_tags (
    todo_id bigint NOT NULL,
    tag_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (todo_id, tag_id),
    CONSTRAINT fk_todo_tags_todo FOREIGN KEY (todo_id)
        REFERENCES todolist_db.todos(todo_id) ON DELETE CASCADE,
    CONSTRAINT fk_todo_tags_tag FOREIGN KEY (tag_id)
        REFERENCES todolist_db.tags(tag_id) ON DELETE CASCADE
);

CREATE INDEX idx_todo_tags_tag_id ON todolist_db.todo_tags(tag_id);

COMMENT ON TABLE todolist_db.todo_tags IS 'Todo와 Tag 다대다 관계';


-- ========== 첨부파일 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.attachments (
    attachment_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    todo_id bigint NOT NULL,
    file_name character varying(255) NOT NULL,
    file_path character varying(500) NOT NULL,
    file_size bigint NOT NULL,
    mime_type character varying(100) NOT NULL,
    uploaded_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_attachments_todo FOREIGN KEY (todo_id)
        REFERENCES todolist_db.todos(todo_id) ON DELETE CASCADE,
    CONSTRAINT chk_attachments_file_size
        CHECK (file_size > 0 AND file_size <= 10485760)
);

CREATE INDEX idx_attachments_todo_id ON todolist_db.attachments(todo_id);

COMMENT ON TABLE todolist_db.attachments IS 'Todo 첨부파일 (최대 10MB)';


-- ========== 댓글 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.comments (
    comment_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    todo_id bigint NOT NULL,
    user_id bigint NOT NULL,
    content text NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_comments_todo FOREIGN KEY (todo_id)
        REFERENCES todolist_db.todos(todo_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
        REFERENCES todolist_db.users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_comments_content_length
        CHECK (length(content) >= 1 AND length(content) <= 1000)
);

CREATE INDEX idx_comments_todo_id ON todolist_db.comments(todo_id);
CREATE INDEX idx_comments_user_id ON todolist_db.comments(user_id);

COMMENT ON TABLE todolist_db.comments IS 'Todo 댓글';


-- ========== Refresh Token 테이블 ==========

CREATE TABLE IF NOT EXISTS todolist_db.refresh_tokens (
    token_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id bigint NOT NULL,
    token character varying(500) NOT NULL UNIQUE,
    expires_at timestamp without time zone NOT NULL,
    is_revoked boolean DEFAULT false NOT NULL,
    ip_address character varying(45),
    user_agent character varying(500),
    last_used_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES todolist_db.users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON todolist_db.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON todolist_db.refresh_tokens(expires_at);

COMMENT ON TABLE todolist_db.refresh_tokens IS 'JWT Refresh Token 저장소';
COMMENT ON COLUMN todolist_db.refresh_tokens.ip_address IS '요청 IP (IPv4/IPv6 지원)';


-- ========== 활동 로그 파티션 테이블 (마스터) ==========

CREATE TABLE IF NOT EXISTS todolist_db.activity_logs (
    log_id bigint GENERATED ALWAYS AS IDENTITY,
    user_id bigint NOT NULL,
    todo_id bigint,
    activity_type todolist_db.activity_type NOT NULL,
    description character varying(500),
    metadata jsonb,
    ip_address character varying(45),
    user_agent character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id)
        REFERENCES todolist_db.users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_logs_todo FOREIGN KEY (todo_id)
        REFERENCES todolist_db.todos(todo_id) ON DELETE SET NULL
) PARTITION BY RANGE (created_at);

COMMENT ON TABLE todolist_db.activity_logs IS '사용자 활동 로그 (월별 파티셔닝)';
COMMENT ON COLUMN todolist_db.activity_logs.metadata IS 'JSON 형식 추가 정보';
