# ================================
# Stage 1: Build
# ================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Gradle wrapper와 설정 파일 복사 (캐싱 최적화를 위해 먼저 복사)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱 최적화)
RUN ./gradlew dependencies --no-daemon || true

# 전체 소스 코드 복사 (src/main/java, src/main/resources 모두 포함)
# .dockerignore에 의해 불필요한 파일은 자동 제외됨
COPY . .

# jar 빌드 (테스트 스킵)
# Spring Boot는 src/main/resources의 모든 파일을 JAR에 포함
RUN ./gradlew bootJar -x test --no-daemon

# JAR 내부 SQL 파일 확인 (빌드 검증)
RUN echo "=== Checking JAR contents ===" && \
    ls -lh build/libs/*.jar && \
    jar -tf build/libs/*.jar | grep "db/migration" || \
    (echo "ERROR: SQL files NOT found in JAR!" && exit 1)

# ================================
# Stage 2: Run
# ================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# 비루트 유저 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 빌드된 jar 복사
COPY --from=build /app/build/libs/todoList-0.0.1-SNAPSHOT.jar app.jar

# 소유권 변경
RUN chown -R appuser:appuser /app

# 비루트 유저로 전환
USER appuser

# 포트 노출
EXPOSE 8080

# 헬스체크 (선택사항, docker-compose에서도 설정 가능)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Spring Boot 실행 (prod 프로파일)
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
