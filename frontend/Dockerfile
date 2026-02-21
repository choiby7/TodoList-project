# ================================
# Stage 1: Dependencies
# ================================
FROM node:20-alpine AS deps

WORKDIR /app

# package.json과 lock 파일 복사
COPY package*.json ./

# 의존성 설치 (프로덕션 전용)
RUN npm ci --only=production

# ================================
# Stage 2: Build
# ================================
FROM node:20-alpine AS build

WORKDIR /app

# deps 레이어에서 node_modules 복사
COPY --from=deps /app/node_modules ./node_modules

# package.json 복사
COPY package*.json ./

# 개발 의존성 포함하여 재설치 (빌드에 필요)
RUN npm ci

# 소스 코드 복사
COPY . .

# 빌드 타임 환경 변수 (NEXT_PUBLIC_* 변수는 빌드 시 번들에 포함됨)
# 빈 문자열: API 코드에서 이미 /api/v1/... 경로를 사용하므로 baseURL은 비움
ARG NEXT_PUBLIC_API_URL=
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

# Next.js 빌드 (standalone 모드)
RUN npm run build

# ================================
# Stage 3: Run
# ================================
FROM node:20-alpine AS run

WORKDIR /app

# 비루트 유저 생성
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

# standalone 결과물 복사
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public

# 소유권 변경
RUN chown -R nextjs:nodejs /app

# 비루트 유저로 전환
USER nextjs

# 포트 노출
EXPOSE 3000

# 환경 변수 (런타임에 docker-compose에서 오버라이드 가능)
ENV NODE_ENV=production \
    PORT=3000

# Next.js 서버 실행
CMD ["node", "server.js"]
