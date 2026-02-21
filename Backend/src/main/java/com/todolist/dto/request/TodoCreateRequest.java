package com.todolist.dto.request;

import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Todo 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
    private String description;

    private TodoPriority priority = TodoPriority.MEDIUM;

    private TodoStatus status = TodoStatus.TODO;

    private LocalDateTime dueDate;

    private Long categoryId;

    private Boolean isImportant = false;
}
