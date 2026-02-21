package com.todolist.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 전역 에러 코드 정의
 * 각 에러 코드는 고유한 코드와 메시지를 가집니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증 관련 (A001 ~ A099)
    AUTH_INVALID_CREDENTIALS("A001", "이메일 또는 비밀번호가 올바르지 않습니다"),
    AUTH_TOKEN_EXPIRED("A002", "토큰이 만료되었습니다"),
    AUTH_TOKEN_INVALID("A003", "유효하지 않은 토큰입니다"),
    AUTH_ACCOUNT_LOCKED("A004", "계정이 잠겨있습니다. 나중에 다시 시도해주세요"),
    AUTH_EMAIL_NOT_VERIFIED("A005", "이메일 인증이 필요합니다"),
    AUTH_REFRESH_TOKEN_NOT_FOUND("A006", "리프레시 토큰을 찾을 수 없습니다"),
    AUTH_REFRESH_TOKEN_EXPIRED("A007", "리프레시 토큰이 만료되었습니다"),
    AUTH_OAUTH2_EMAIL_NOT_PROVIDED("A008", "OAuth2 제공자로부터 이메일을 받지 못했습니다"),
    AUTH_OAUTH2_ACCOUNT_MERGE_CONFLICT("A009", "이미 다른 OAuth2 제공자와 연결된 계정입니다"),

    // 사용자 관련 (U001 ~ U099)
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다"),
    USER_EMAIL_DUPLICATE("U002", "이미 사용 중인 이메일입니다"),
    USER_INVALID_PASSWORD("U003", "비밀번호가 정책을 만족하지 않습니다"),
    USER_PASSWORD_NOT_MATCH("U004", "현재 비밀번호가 일치하지 않습니다"),

    // Todo 관련 (T001 ~ T099)
    TODO_NOT_FOUND("T001", "요청한 할 일을 찾을 수 없습니다"),
    TODO_FORBIDDEN("T002", "이 할 일에 접근할 권한이 없습니다"),
    TODO_ALREADY_DELETED("T003", "이미 삭제된 할 일입니다"),
    TODO_NOT_DELETED("T004", "삭제되지 않은 할 일입니다"),

    // 카테고리 관련 (C001 ~ C099)
    CATEGORY_NOT_FOUND("C001", "카테고리를 찾을 수 없습니다"),
    CATEGORY_NAME_DUPLICATE("C002", "이미 존재하는 카테고리 이름입니다"),
    CATEGORY_FORBIDDEN("C003", "이 카테고리에 접근할 권한이 없습니다"),
    CATEGORY_HAS_TODOS("C004", "카테고리에 할 일이 존재하여 삭제할 수 없습니다"),

    // 공통 (G001 ~ G999)
    COMMON_INVALID_PARAMETER("G001", "유효하지 않은 요청 파라미터입니다"),
    COMMON_RESOURCE_NOT_FOUND("G002", "요청한 리소스를 찾을 수 없습니다"),
    COMMON_METHOD_NOT_ALLOWED("G003", "허용되지 않은 HTTP 메서드입니다"),
    COMMON_RATE_LIMIT_EXCEEDED("G004", "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요"),
    COMMON_INTERNAL_ERROR("G999", "서버 내부 오류가 발생했습니다");

    private final String code;
    private final String message;
}
