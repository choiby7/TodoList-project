package com.todolist.dto.request;

import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Todo 수정 요청 DTO
 * 모든 필드는 optional이며, null이 아닌 필드만 업데이트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoUpdateRequest {

    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
    private String description;

    private TodoPriority priority;

    private TodoStatus status;

    private LocalDateTime dueDate;

    private Long categoryId;

    private Boolean isImportant;
}
