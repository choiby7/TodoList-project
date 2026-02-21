package com.todolist.dto.response;

import com.todolist.domain.Todo;
import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Todo 응답 DTO
 */
@Getter
@Builder
public class TodoResponse {

    private final Long todoId;
    private final Long userId;
    private final Long categoryId;
    private final String categoryName;
    private final String title;
    private final String description;
    private final TodoPriority priority;
    private final TodoStatus status;
    private final LocalDateTime dueDate;
    private final Boolean isImportant;
    private final Boolean isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime deletedAt;

    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .todoId(todo.getTodoId())
                .userId(todo.getUserId())
                .categoryId(todo.getCategoryId())
                .categoryName(todo.getCategory() != null ? todo.getCategory().getName() : null)
                .title(todo.getTitle())
                .description(todo.getDescription())
                .priority(todo.getPriority())
                .status(todo.getStatus())
                .dueDate(todo.getDueDate())
                .isImportant(todo.getIsImportant())
                .isDeleted(todo.getIsDeleted())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .completedAt(todo.getCompletedAt())
                .deletedAt(todo.getDeletedAt())
                .build();
    }
}
