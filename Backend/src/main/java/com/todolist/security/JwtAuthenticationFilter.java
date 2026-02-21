package com.todolist.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 모든 요청에 대해 JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 블랙리스트 체크 (로그아웃된 토큰 차단)
                if (isTokenBlacklisted(jwt)) {
                    log.warn("블랙리스트에 등록된 토큰 접근 시도: {}", jwt.substring(0, Math.min(20, jwt.length())));
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Logged out token");
                    return;
                }

                Long userId = jwtTokenProvider.extractUserId(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("사용자 인증 완료: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("인증 처리 중 오류 발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request의 Authorization 헤더에서 JWT 토큰 추출
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 토큰이 블랙리스트에 등록되어 있는지 확인
     */
    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }
}
