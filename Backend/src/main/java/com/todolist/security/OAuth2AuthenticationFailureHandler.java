package com.todolist.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 인증 실패 핸들러
 * 실패 시 에러 메시지와 함께 Frontend로 리다이렉트
 *
 * @since 2026-02-18
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                         HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        log.error("[OAuth2 Failure] 인증 실패 - {}", exception.getMessage());

        // 에러 메시지 인코딩
        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

        // Frontend로 리다이렉트 (에러 정보 포함)
        String targetUrl = redirectUri + "?error=oauth2&message=" + errorMessage;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
