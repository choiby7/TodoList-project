package com.todolist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting 서비스
 * Redis 기반 토큰 버킷 알고리즘 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Rate Limit 설정 (요청 수, 시간 창)
    private static final int LOGIN_LIMIT = 5;          // 로그인: 5회
    private static final int LOGIN_WINDOW_MINUTES = 15; // 15분

    private static final int SIGNUP_LIMIT = 3;         // 회원가입: 3회
    private static final int SIGNUP_WINDOW_HOURS = 1;  // 1시간

    private static final int TODO_CRUD_LIMIT = 100;    // Todo CRUD: 100회
    private static final int TODO_CRUD_WINDOW_MINUTES = 1; // 1분

    /**
     * Rate Limit 체크 (IP 기반 - 로그인)
     *
     * @param ipAddress 클라이언트 IP
     * @return 허용 여부 (true: 허용, false: 차단)
     */
    public boolean allowLoginAttempt(String ipAddress) {
        String key = "rate_limit:login:" + ipAddress;
        return checkRateLimit(key, LOGIN_LIMIT, Duration.ofMinutes(LOGIN_WINDOW_MINUTES));
    }

    /**
     * Rate Limit 체크 (IP 기반 - 회원가입)
     *
     * @param ipAddress 클라이언트 IP
     * @return 허용 여부
     */
    public boolean allowSignupAttempt(String ipAddress) {
        String key = "rate_limit:signup:" + ipAddress;
        return checkRateLimit(key, SIGNUP_LIMIT, Duration.ofHours(SIGNUP_WINDOW_HOURS));
    }

    /**
     * Rate Limit 체크 (사용자 기반 - Todo CRUD)
     *
     * @param userId 사용자 ID
     * @return 허용 여부
     */
    public boolean allowTodoCrudOperation(Long userId) {
        String key = "rate_limit:todo:" + userId;
        return checkRateLimit(key, TODO_CRUD_LIMIT, Duration.ofMinutes(TODO_CRUD_WINDOW_MINUTES));
    }

    /**
     * Rate Limit 체크 로직 (토큰 버킷 알고리즘)
     *
     * @param key      Redis 키
     * @param limit    최대 요청 수
     * @param duration 시간 창
     * @return 허용 여부
     */
    private boolean checkRateLimit(String key, int limit, Duration duration) {
        try {
            // 현재 요청 수 조회
            Long currentCount = redisTemplate.opsForValue().increment(key, 1);

            if (currentCount == null) {
                log.warn("Redis increment 실패: key={}", key);
                return true; // Redis 실패 시 허용 (Fail-Open 전략)
            }

            // 첫 요청이면 TTL 설정
            if (currentCount == 1) {
                redisTemplate.expire(key, duration.getSeconds(), TimeUnit.SECONDS);
            }

            boolean allowed = currentCount <= limit;

            if (!allowed) {
                log.warn("Rate limit 초과: key={}, count={}, limit={}", key, currentCount, limit);
            }

            return allowed;

        } catch (Exception e) {
            log.error("Rate limit 체크 중 오류 발생: key={}", key, e);
            return true; // 오류 시 허용 (Fail-Open 전략)
        }
    }

    /**
     * Rate Limit 초기화 (테스트용)
     *
     * @param key Redis 키
     */
    public void reset(String key) {
        redisTemplate.delete(key);
    }
}
