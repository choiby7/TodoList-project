# ================================
# TodoList Docker Compose Makefile
# ================================
# 2-서버 구조: DB 서버 + 앱 서버
#
# 주요 명령어:
#   make up      - 전체 스택 시작 (DB 서버 → 앱 서버 순서)
#   make down    - 전체 스택 중지
#   make logs    - 전체 로그 확인
#   make restart - 전체 스택 재시작
#   make clean   - 전체 중지 + 볼륨 삭제 (데이터 삭제 주의!)

.PHONY: help up down logs restart clean status \
        up-db down-db logs-db \
        up-app down-app logs-app \
        build rebuild

# ================================
# 기본 명령어 (help)
# ================================
help:
	@echo "================================"
	@echo "TodoList Docker Compose 명령어"
	@echo "================================"
	@echo ""
	@echo "전체 스택 관리:"
	@echo "  make up        - 전체 스택 시작 (DB → 앱 순서)"
	@echo "  make down      - 전체 스택 중지"
	@echo "  make logs      - 전체 로그 확인 (실시간)"
	@echo "  make restart   - 전체 스택 재시작"
	@echo "  make status    - 전체 컨테이너 상태 확인"
	@echo "  make build     - 전체 이미지 빌드"
	@echo "  make rebuild   - 캐시 없이 전체 재빌드"
	@echo "  make clean     - 전체 중지 + 볼륨 삭제 ⚠️"
	@echo ""
	@echo "DB 서버 관리:"
	@echo "  make up-db     - DB 서버만 시작"
	@echo "  make down-db   - DB 서버만 중지"
	@echo "  make logs-db   - DB 서버 로그 확인"
	@echo ""
	@echo "앱 서버 관리:"
	@echo "  make up-app    - 앱 서버만 시작"
	@echo "  make down-app  - 앱 서버만 중지"
	@echo "  make logs-app  - 앱 서버 로그 확인"
	@echo ""

# ================================
# 전체 스택 관리
# ================================

# 전체 시작 (올바른 순서: DB 먼저, 앱 나중)
up:
	@echo "🚀 DB 서버 시작 중..."
	docker compose --env-file .env.db -f docker-compose.db.yml up -d
	@echo "⏳ DB 서버 헬스체크 대기 중..."
	@sleep 5
	@echo "🚀 앱 서버 시작 중..."
	docker compose --env-file .env.app -f docker-compose.app.yml up -d
	@echo "✅ 전체 스택 시작 완료!"
	@echo ""
	@echo "접속 정보:"
	@echo "  프론트엔드: http://localhost"
	@echo "  백엔드 API: http://localhost/api"
	@echo "  Swagger UI: http://localhost/swagger-ui/index.html"
	@echo ""

# 전체 중지
down:
	@echo "🛑 앱 서버 중지 중..."
	docker compose --env-file .env.app -f docker-compose.app.yml down
	@echo "🛑 DB 서버 중지 중..."
	docker compose --env-file .env.db -f docker-compose.db.yml down
	@echo "✅ 전체 스택 중지 완료!"

# 전체 로그 확인 (실시간)
logs:
	@echo "📋 전체 로그 확인 중... (Ctrl+C로 종료)"
	docker compose --env-file .env.db -f docker-compose.db.yml --env-file .env.app -f docker-compose.app.yml logs -f

# 전체 재시작
restart: down up

# 전체 상태 확인
status:
	@echo "================================"
	@echo "DB 서버 상태"
	@echo "================================"
	docker compose --env-file .env.db -f docker-compose.db.yml ps
	@echo ""
	@echo "================================"
	@echo "앱 서버 상태"
	@echo "================================"
	docker compose --env-file .env.app -f docker-compose.app.yml ps

# 전체 이미지 빌드
build:
	@echo "🔨 이미지 빌드 중..."
	docker compose --env-file .env.app -f docker-compose.app.yml build

# 캐시 없이 재빌드
rebuild:
	@echo "🔨 캐시 없이 재빌드 중..."
	docker compose --env-file .env.app -f docker-compose.app.yml build --no-cache

# 전체 중지 + 볼륨 삭제 (데이터 삭제 주의!)
clean:
	@echo "⚠️  경고: 모든 데이터가 삭제됩니다!"
	@read -p "계속하시겠습니까? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		echo "🗑️  앱 서버 중지 + 볼륨 삭제 중..."; \
		docker compose --env-file .env.app -f docker-compose.app.yml down -v; \
		echo "🗑️  DB 서버 중지 + 볼륨 삭제 중..."; \
		docker compose --env-file .env.db -f docker-compose.db.yml down -v; \
		echo "✅ 전체 정리 완료!"; \
	else \
		echo "❌ 취소되었습니다."; \
	fi

# ================================
# DB 서버 전용 명령어
# ================================

up-db:
	@echo "🚀 DB 서버 시작 중..."
	docker compose --env-file .env.db -f docker-compose.db.yml up -d
	@echo "✅ DB 서버 시작 완료!"

down-db:
	@echo "🛑 DB 서버 중지 중..."
	docker compose --env-file .env.db -f docker-compose.db.yml down
	@echo "✅ DB 서버 중지 완료!"

logs-db:
	@echo "📋 DB 서버 로그 확인 중... (Ctrl+C로 종료)"
	docker compose --env-file .env.db -f docker-compose.db.yml logs -f

# ================================
# 앱 서버 전용 명령어
# ================================

up-app:
	@echo "🚀 앱 서버 시작 중..."
	docker compose --env-file .env.app -f docker-compose.app.yml up -d
	@echo "✅ 앱 서버 시작 완료!"

down-app:
	@echo "🛑 앱 서버 중지 중..."
	docker compose --env-file .env.app -f docker-compose.app.yml down
	@echo "✅ 앱 서버 중지 완료!"

logs-app:
	@echo "📋 앱 서버 로그 확인 중... (Ctrl+C로 종료)"
	docker compose --env-file .env.app -f docker-compose.app.yml logs -f
