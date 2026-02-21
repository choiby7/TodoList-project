package com.todolist.domain;

/**
 * OAuth2 인증 제공자 (소셜 로그인 타입)
 *
 * @since 2026-02-18
 */
public enum OAuth2Provider {
    /**
     * Google 로그인
     */
    GOOGLE,

    /**
     * GitHub 로그인
     */
    GITHUB,

    /**
     * Kakao 로그인
     */
    KAKAO
}
