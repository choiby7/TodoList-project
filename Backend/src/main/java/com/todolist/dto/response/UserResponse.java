package com.todolist.dto.response;

import com.todolist.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 * 비밀번호 등 민감한 정보는 포함하지 않음
 */
@Getter
@Builder
public class UserResponse {

    private final Long userId;
    private final String email;
    private final String username;
    private final Boolean emailVerified;
    private final LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
