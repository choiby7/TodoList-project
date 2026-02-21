package com.todolist.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공통 API 응답 DTO
 * 모든 성공 응답에 사용되는 표준 형식
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success = true;
    private final T data;
    private final LocalDateTime timestamp;

    private ApiResponse(T data) {
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 데이터와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    /**
     * 데이터 없이 성공 응답 생성 (예: 삭제 성공)
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(null);
    }
}
