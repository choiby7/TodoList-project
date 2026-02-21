package com.todolist.dto.response;

import com.todolist.domain.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 카테고리 응답 DTO
 */
@Getter
@Builder
public class CategoryResponse {

    private final Long categoryId;
    private final Long userId;
    private final String name;
    private final String colorCode;
    private final String icon;
    private final Integer displayOrder;
    private final Long todoCount;  // 해당 카테고리의 Todo 개수
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .userId(category.getUserId())
                .name(category.getName())
                .colorCode(category.getColorCode())
                .icon(category.getIcon())
                .displayOrder(category.getDisplayOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public static CategoryResponse from(Category category, Long todoCount) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .userId(category.getUserId())
                .name(category.getName())
                .colorCode(category.getColorCode())
                .icon(category.getIcon())
                .displayOrder(category.getDisplayOrder())
                .todoCount(todoCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
