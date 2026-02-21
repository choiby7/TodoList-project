package com.todolist.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 DTO
 * Spring Data의 Page 객체를 클라이언트 친화적인 형태로 변환
 */
@Getter
@Builder
public class PageResponse<T> {

    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;
    private final boolean hasNext;
    private final boolean hasPrevious;

    /**
     * Spring Data Page를 PageResponse로 변환
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
