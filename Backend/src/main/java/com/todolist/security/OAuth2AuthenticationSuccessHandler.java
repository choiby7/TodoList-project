package com.todolist.security;

import com.todolist.domain.RefreshToken;
import com.todolist.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2 인증 성공 핸들러
 * 1. JWT 토큰 생성
 * 2. 세션에 토큰 저장 (30초 TTL)
 * 3. Frontend 리다이렉트 (세션 ID 전달)
 *
 * @since 2026-02-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        // 1. CustomOAuth2User에서 정보 추출
        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oauth2User.getUserId();
        boolean isNewUser = oauth2User.isNewUser();
        boolean needsAccountMerge = oauth2User.isNeedsAccountMerge();
        String existingEmail = oauth2User.getExistingEmail();

        log.info("[OAuth2 Success] 사용자 인증 성공 - userId: {}, isNewUser: {}, needsAccountMerge: {}",
                userId, isNewUser, needsAccountMerge);

        HttpSession session = request.getSession(true);
        String sessionId = session.getId();
        session.setMaxInactiveInterval(30); // 30초

        Map<String, String> sessionData = new HashMap<>();

        // 2. 신규 사용자 또는 계정 병합 필요 시 - 토큰 발급하지 않음
        if (isNewUser || needsAccountMerge) {
            sessionData.put("userId", userId.toString());
            sessionData.put("status", isNewUser ? "NEEDS_TERMS" : "NEEDS_MERGE");
            if (needsAccountMerge) {
                sessionData.put("existingEmail", existingEmail);
            }

            redisTemplate.opsForValue().set(
                    "oauth2:session:" + sessionId,
                    sessionData,
                    30,
                    TimeUnit.SECONDS
            );

            log.info("[OAuth2 Success] 약관/병합 동의 필요 - sessionId: {}, status: {}",
                    sessionId, sessionData.get("status"));
        } else {
            // 3. 기존 사용자 - 정상 토큰 발급
            String accessToken = jwtTokenProvider.generateAccessToken(userId);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            saveRefreshToken(userId, refreshToken, ipAddress, userAgent);

            sessionData.put("status", "SUCCESS");
            sessionData.put("accessToken", accessToken);
            sessionData.put("refreshToken", refreshToken);

            redisTemplate.opsForValue().set(
                    "oauth2:session:" + sessionId,
                    sessionData,
                    30,
                    TimeUnit.SECONDS
            );

            log.info("[OAuth2 Success] 토큰 발급 완료 - sessionId: {}", sessionId);
        }

        // 5. Frontend로 리다이렉트 (세션 ID 전달)
        String targetUrl = redirectUri + "?session=" + sessionId;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * RefreshToken 저장 (순환 참조 방지를 위해 AuthService 대신 직접 저장)
     */
    private void saveRefreshToken(Long userId, String token, String ipAddress, String userAgent) {
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
     * 클라이언트 IP 주소 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
