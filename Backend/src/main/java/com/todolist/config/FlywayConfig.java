package com.todolist.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway 설정 - Spring Boot 4.x에서 직접 Bean 등록 방식
 *
 * 목적:
 * 1. Spring Boot 4.x AutoConfiguration 패키지 변경 대응
 * 2. Flyway를 Hibernate보다 먼저 실행되도록 보장
 * 3. Docker 환경에서 스키마 자동 생성
 */
@Configuration
public class FlywayConfig {

    /**
     * Flyway Bean 직접 등록
     *
     * Spring Boot AutoConfiguration을 사용하지 않고
     * 명시적으로 Flyway를 설정하고 migration을 실행합니다.
     *
     * @param dataSource Spring Boot가 자동 생성한 DataSource
     * @param schemas Flyway가 관리할 스키마 (환경 변수 또는 YML에서 주입)
     * @param locations Migration 파일 위치
     * @return 설정 완료된 Flyway 인스턴스
     */
    @Bean
    public Flyway flyway(DataSource dataSource,
                         @Value("${spring.flyway.schemas:todolist_db}") String schemas,
                         @Value("${spring.flyway.locations:classpath:db/migration}") String locations) {

        System.out.println("=== Flyway Manual Configuration 시작 ===");
        System.out.println("Schemas: " + schemas);
        System.out.println("Locations: " + locations);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemas)
                .locations(locations)
                .createSchemas(true)           // 스키마 자동 생성
                .baselineOnMigrate(true)       // 기존 DB 자동 baseline
                .baselineVersion("0")          // 기존 스키마를 버전 0으로 간주
                .load();

        System.out.println("=== Flyway Migration 실행 중 ===");
        flyway.migrate();
        System.out.println("=== Flyway Migration 완료 ===");

        return flyway;
    }
}
