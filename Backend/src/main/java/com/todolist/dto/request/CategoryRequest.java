package com.todolist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 생성/수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이하여야 합니다")
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "올바른 HEX 색상 코드 형식이 아닙니다")
    private String colorCode = "#3B82F6";  // 기본값: 파란색

    private String icon;
}
