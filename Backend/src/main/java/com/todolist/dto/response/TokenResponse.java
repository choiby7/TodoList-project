package com.todolist.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 응답 DTO
 * 로그인 및 토큰 갱신 시 반환
 */
@Getter
@Builder
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final Long expiresIn;  // 초 단위

    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
