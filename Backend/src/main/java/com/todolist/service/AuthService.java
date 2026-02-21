package com.todolist.service;

import com.todolist.domain.OAuth2Provider;
import com.todolist.domain.RefreshToken;
import com.todolist.domain.User;
import com.todolist.dto.request.LoginRequest;
import com.todolist.dto.request.SignupRequest;
import com.todolist.dto.response.OAuth2ExchangeResponse;
import com.todolist.dto.response.TokenResponse;
import com.todolist.dto.response.UserResponse;
import com.todolist.exception.*;
import com.todolist.repository.RefreshTokenRepository;
import com.todolist.repository.UserRepository;
import com.todolist.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 인증 서비스
 * 회원가입, 로그인, 토큰 갱신, 로그아웃 등 인증 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(ErrorCode.USER_EMAIL_DUPLICATE);
        }

        // 비밀번호 해싱
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .username(request.getUsername())
                .build();

        // 약관 동의 처리 (일반 회원가입은 프론트엔드에서 동의 후 요청)
        user.agreeToTerms();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        return UserResponse.from(savedUser);
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        // 계정 잠금 확인
        if (user.isAccountLocked()) {
            throw new UnauthorizedException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        // OAuth2 전용 계정 체크 (비밀번호 로그인 불가)
        if (user.isOAuth2Only()) {
            log.warn("OAuth2 전용 계정 비밀번호 로그인 시도: email={}, provider={}", user.getEmail(), user.getProvider());
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 로그인 실패 처리
            user.incrementFailedAttempts();
            userRepository.save(user);
            log.warn("로그인 실패: email={}, failedAttempts={}", request.getEmail(), user.getFailedLoginAttempts());
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // 로그인 성공 처리
        user.resetFailedAttempts();
        userRepository.save(user);

        // Access Token & Refresh Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // Refresh Token DB 저장
        saveRefreshToken(user.getUserId(), refreshToken);

        log.info("로그인 성공: userId={}, email={}", user.getUserId(), user.getEmail());

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenValidityInSeconds()
        );
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        // Refresh Token 검증
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndIsRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired()) {
            throw new UnauthorizedException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        // 토큰 사용 기록
        refreshToken.use();
        refreshTokenRepository.save(refreshToken);

        // 새 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUserId());

        // 새 Refresh Token 생성 (Token Rotation)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(refreshToken.getUserId());

        // 이전 Refresh Token 폐기
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // 새 Refresh Token 저장
        saveRefreshToken(refreshToken.getUserId(), newRefreshToken);

        log.info("토큰 갱신 완료: userId={}", refreshToken.getUserId());

        return TokenResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenValidityInSeconds()
        );
    }

    /**
     * 로그아웃
     * Access Token을 블랙리스트에 등록하고 Refresh Token을 폐기합니다.
     */
    @Transactional
    public void logout(String accessToken, String refreshTokenValue) {
        // 1. Access Token 블랙리스트 등록 (Redis)
        if (accessToken != null && !accessToken.isEmpty()) {
            long remainingTime = jwtTokenProvider.getRemainingExpiration(accessToken);
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + accessToken,
                        "logout",
                        remainingTime,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                );
                log.info("Access Token 블랙리스트 등록 완료: remainingTime={}ms", remainingTime);
            }
        }

        // 2. Refresh Token 폐기 (DB)
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndIsRevokedFalse(refreshTokenValue)
                .orElse(null);

        if (refreshToken != null) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            log.info("로그아웃 완료: userId={}", refreshToken.getUserId());
        }
    }

    /**
     * OAuth2 세션 교환 - Redis에서 상태 조회 및 적절한 응답 반환
     */
    public OAuth2ExchangeResponse exchangeOAuth2Session(String sessionId) {
        String redisKey = "oauth2:session:" + sessionId;

        // Redis에서 세션 데이터 조회
        @SuppressWarnings("unchecked")
        Map<String, String> sessionData = (Map<String, String>) redisTemplate.opsForValue().get(redisKey);

        if (sessionData == null) {
            log.warn("[OAuth2 Exchange] Redis에 세션이 없거나 만료됨: sessionId={}", sessionId);
            throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        String status = sessionData.get("status");

        // 1. 약관 동의 필요 (신규 사용자)
        if ("NEEDS_TERMS".equals(status)) {
            log.info("[OAuth2 Exchange] 약관 동의 필요: sessionId={}", sessionId);
            return OAuth2ExchangeResponse.ofNeedsTermsAgreement(sessionId);
        }

        // 2. 계정 병합 동의 필요
        if ("NEEDS_MERGE".equals(status)) {
            String existingEmail = sessionData.get("existingEmail");
            log.info("[OAuth2 Exchange] 계정 병합 동의 필요: sessionId={}, email={}", sessionId, existingEmail);
            return OAuth2ExchangeResponse.ofNeedsAccountMerge(existingEmail, sessionId);
        }

        // 3. 정상 토큰 발급 (기존 사용자)
        String accessToken = sessionData.get("accessToken");
        String refreshToken = sessionData.get("refreshToken");

        if (accessToken == null || refreshToken == null) {
            log.warn("[OAuth2 Exchange] 토큰 데이터가 불완전함: sessionId={}", sessionId);
            throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        log.info("[OAuth2 Exchange] 토큰 교환 성공: sessionId={}", sessionId);
        return OAuth2ExchangeResponse.ofSuccess(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenValidityInSeconds()
        );
    }

    /**
     * 현재 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    /**
     * Refresh Token 저장
     * OAuth2 로그인에서도 사용하기 위해 public으로 변경
     */
    public void saveRefreshToken(Long userId, String token) {
        saveRefreshToken(userId, token, null, null);
    }

    /**
     * Refresh Token 저장 (IP 주소 및 User-Agent 포함)
     */
    public void saveRefreshToken(Long userId, String token, String ipAddress, String userAgent) {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenValidityInSeconds());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 약관 동의 처리 및 토큰 발급
     */
    @Transactional
    public TokenResponse agreeToTermsAndIssueToken(String sessionId) {
        String redisKey = "oauth2:session:" + sessionId;

        @SuppressWarnings("unchecked")
        Map<String, String> sessionData = (Map<String, String>) redisTemplate.opsForValue().get(redisKey);

        if (sessionData == null || !"NEEDS_TERMS".equals(sessionData.get("status"))) {
            log.warn("[OAuth2 Agree Terms] 유효하지 않은 세션: sessionId={}", sessionId);
            throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Long userId = Long.parseLong(sessionData.get("userId"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 약관 동의 처리
        user.agreeToTerms();
        userRepository.save(user);

        // 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        saveRefreshToken(userId, refreshToken);

        // Redis 세션 삭제
        redisTemplate.delete(redisKey);

        log.info("[OAuth2 Agree Terms] 약관 동의 완료 및 토큰 발급: userId={}", userId);

        return TokenResponse.of(accessToken, refreshToken, jwtTokenProvider.getAccessTokenValidityInSeconds());
    }

    /**
     * 계정 병합 동의 처리 및 토큰 발급
     */
    @Transactional
    public TokenResponse agreeToMergeAndIssueToken(String sessionId) {
        String redisKey = "oauth2:session:" + sessionId;

        @SuppressWarnings("unchecked")
        Map<String, String> sessionData = (Map<String, String>) redisTemplate.opsForValue().get(redisKey);

        if (sessionData == null || !"NEEDS_MERGE".equals(sessionData.get("status"))) {
            log.warn("[OAuth2 Agree Merge] 유효하지 않은 세션: sessionId={}", sessionId);
            throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Long pendingUserId = Long.parseLong(sessionData.get("userId"));
        String existingEmail = sessionData.get("existingEmail");

        // 임시 사용자 조회
        User pendingUser = userRepository.findById(pendingUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 기존 사용자 조회
        User existingUser = userRepository.findByEmail(existingEmail)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // OAuth2 정보 추출 (삭제 전에)
        OAuth2Provider provider = pendingUser.getProvider();
        String providerId = pendingUser.getProviderId();
        String profileImage = pendingUser.getProfileImage();

        // 1. 먼저 임시 사용자 삭제 (unique constraint 해제)
        userRepository.delete(pendingUser);
        userRepository.flush(); // 즉시 DB 반영

        // 2. 계정 병합: 기존 사용자에 OAuth2 정보 추가
        existingUser.linkOAuthProvider(provider, providerId, profileImage);

        // 약관 동의가 없는 경우에만 동의 처리 (기존 동의 시간 보존)
        if (!existingUser.hasAgreedToTerms()) {
            existingUser.agreeToTerms();
            log.info("[OAuth2 Agree Merge] 약관 동의 추가 처리: userId={}", existingUser.getUserId());
        } else {
            log.info("[OAuth2 Agree Merge] 기존 약관 동의 유지: userId={}, termsAgreedAt={}",
                    existingUser.getUserId(), existingUser.getTermsAgreedAt());
        }

        userRepository.save(existingUser);

        // 토큰 발급 (기존 사용자 ID로)
        String accessToken = jwtTokenProvider.generateAccessToken(existingUser.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(existingUser.getUserId());
        saveRefreshToken(existingUser.getUserId(), refreshToken);

        // Redis 세션 삭제
        redisTemplate.delete(redisKey);

        log.info("[OAuth2 Agree Merge] 계정 병합 완료 및 토큰 발급: userId={}", existingUser.getUserId());

        return TokenResponse.of(accessToken, refreshToken, jwtTokenProvider.getAccessTokenValidityInSeconds());
    }

    /**
     * SecurityContext에서 현재 사용자 ID 추출
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID);
        }
        return Long.parseLong(authentication.getName());
    }
}
