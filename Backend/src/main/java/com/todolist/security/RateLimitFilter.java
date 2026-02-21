package com.todolist.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.response.ErrorResponse;
import com.todolist.exception.ErrorCode;
import com.todolist.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting 필터
 * IP 기반 및 사용자 기반 Rate Limit 체크
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = getClientIpAddress(request);

        // OAuth2 경로는 Rate Limit 제외
        if (requestUri.startsWith("/login/oauth2/code/") ||
                requestUri.startsWith("/oauth2/authorization/") ||
                requestUri.equals("/api/v1/auth/oauth2/exchange")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 로그인 Rate Limit (IP 기반)
        if ("/api/v1/auth/login".equals(requestUri) && "POST".equals(method)) {
            if (!rateLimiterService.allowLoginAttempt(ipAddress)) {
                sendRateLimitError(response, "로그인 시도 한도를 초과했습니다. 15분 후 다시 시도해주세요");
                return;
            }
        }

        // 회원가입 Rate Limit (IP 기반)
        if ("/api/v1/auth/signup".equals(requestUri) && "POST".equals(method)) {
            if (!rateLimiterService.allowSignupAttempt(ipAddress)) {
                sendRateLimitError(response, "회원가입 시도 한도를 초과했습니다. 1시간 후 다시 시도해주세요");
                return;
            }
        }

        // Todo CRUD Rate Limit (사용자 기반)
        if (requestUri.startsWith("/api/v1/todos") && isModifyingMethod(method)) {
            Long userId = getCurrentUserId();
            if (userId != null) {
                if (!rateLimiterService.allowTodoCrudOperation(userId)) {
                    sendRateLimitError(response, "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요");
                    return;
                }
            }
        }

        // Rate Limit 통과 시 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
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

        // 여러 IP가 있는 경우 첫 번째 IP 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 현재 인증된 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.warn("사용자 ID 파싱 실패: {}", authentication.getName());
            }
        }

        return null;
    }

    /**
     * 데이터 변경 메서드 여부 확인
     */
    private boolean isModifyingMethod(String method) {
        return "POST".equals(method) ||
                "PUT".equals(method) ||
                "DELETE".equals(method) ||
                "PATCH".equals(method);
    }

    /**
     * Rate Limit 초과 에러 응답
     */
    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.COMMON_RATE_LIMIT_EXCEEDED.getCode(),
                message,
                null  // path는 null (필터에서는 경로 정보 불필요)
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
