package com.todolist.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth2 세션 교환 응답 DTO
 * 약관 동의 또는 계정 병합 필요 여부를 포함
 */
@Getter
@Builder
public class OAuth2ExchangeResponse {

    /**
     * 성공 여부
     */
    private Boolean success;

    /**
     * 약관 동의 필요 여부 (신규 사용자)
     */
    private Boolean needsTermsAgreement;

    /**
     * 계정 병합 동의 필요 여부 (기존 이메일 계정 존재)
     */
    private Boolean needsAccountMerge;

    /**
     * 병합 대상 이메일 (needsAccountMerge=true일 때만)
     */
    private String existingEmail;

    /**
     * 임시 세션 ID (동의 처리 후 토큰 교환에 사용)
     */
    private String pendingSessionId;

    /**
     * Access Token (약관 동의 완료된 경우)
     */
    private String accessToken;

    /**
     * Refresh Token (약관 동의 완료된 경우)
     */
    private String refreshToken;

    /**
     * Token Type
     */
    private String tokenType;

    /**
     * Access Token 유효 시간 (초)
     */
    private Long expiresIn;

    /**
     * 약관 동의 불필요 (정상 로그인)
     */
    public static OAuth2ExchangeResponse ofSuccess(String accessToken, String refreshToken, Long expiresIn) {
        return OAuth2ExchangeResponse.builder()
                .success(true)
                .needsTermsAgreement(false)
                .needsAccountMerge(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * 약관 동의 필요 (신규 사용자)
     */
    public static OAuth2ExchangeResponse ofNeedsTermsAgreement(String pendingSessionId) {
        return OAuth2ExchangeResponse.builder()
                .success(false)
                .needsTermsAgreement(true)
                .needsAccountMerge(false)
                .pendingSessionId(pendingSessionId)
                .build();
    }

    /**
     * 계정 병합 동의 필요
     */
    public static OAuth2ExchangeResponse ofNeedsAccountMerge(String existingEmail, String pendingSessionId) {
        return OAuth2ExchangeResponse.builder()
                .success(false)
                .needsTermsAgreement(false)
                .needsAccountMerge(true)
                .existingEmail(existingEmail)
                .pendingSessionId(pendingSessionId)
                .build();
    }
}
