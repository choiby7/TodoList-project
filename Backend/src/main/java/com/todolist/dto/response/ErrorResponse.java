package com.todolist.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 DTO
 * success는 항상 false이며, 에러 코드, 메시지, 타임스탬프, 요청 경로를 포함합니다.
 * 유효성 검증 에러의 경우 errors 필드에 필드별 에러 목록이 포함됩니다.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success = false;
    private final String errorCode;
    private final String message;
    private final List<FieldError> errors;  // 유효성 검증 에러 시에만 포함
    private final LocalDateTime timestamp;
    private final String path;

    /**
     * 필드 에러 정보
     */
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }

    /**
     * 단일 에러 응답 생성
     */
    public static ErrorResponse of(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    /**
     * 유효성 검증 에러 응답 생성
     */
    public static ErrorResponse of(String errorCode, String message, List<FieldError> errors, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}
