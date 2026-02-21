package com.todolist.service;

import com.todolist.domain.Category;
import com.todolist.dto.request.CategoryRequest;
import com.todolist.dto.response.CategoryResponse;
import com.todolist.exception.ConflictException;
import com.todolist.exception.ErrorCode;
import com.todolist.exception.ForbiddenException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.CategoryRepository;
import com.todolist.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Long userId = getCurrentUserId();

        // 중복 이름 확인
        if (categoryRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new ConflictException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        // displayOrder는 현재 카테고리 개수 + 1
        int displayOrder = categoryRepository.findByUserIdOrderByDisplayOrder(userId).size();

        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .colorCode(request.getColorCode())
                .icon(request.getIcon())
                .displayOrder(displayOrder)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 생성 완료: categoryId={}, userId={}, name={}", savedCategory.getCategoryId(), userId, savedCategory.getName());

        return CategoryResponse.from(savedCategory);
    }

    /**
     * 카테고리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        Long userId = getCurrentUserId();

        List<Category> categories = categoryRepository.findByUserIdOrderByDisplayOrder(userId);

        return categories.stream()
                .map(category -> {
                    Long todoCount = todoRepository.countByCategoryIdAndIsDeletedFalse(category.getCategoryId());
                    return CategoryResponse.from(category, todoCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Long userId = getCurrentUserId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        // 소유권 검증
        if (!category.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.CATEGORY_FORBIDDEN);
        }

        // 이름 변경 시 중복 확인
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByUserIdAndNameAndCategoryIdNot(userId, request.getName(), categoryId)) {
                throw new ConflictException(ErrorCode.CATEGORY_NAME_DUPLICATE);
            }
        }

        category.update(request.getName(), request.getColorCode(), request.getIcon());
        Category updatedCategory = categoryRepository.save(category);
        log.info("카테고리 수정 완료: categoryId={}, userId={}", categoryId, userId);

        return CategoryResponse.from(updatedCategory);
    }

    /**
     * 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Long userId = getCurrentUserId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        // 소유권 검증
        if (!category.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.CATEGORY_FORBIDDEN);
        }

        // 카테고리 삭제 (소속 Todo의 categoryId는 NULL로 자동 설정됨)
        categoryRepository.delete(category);
        log.info("카테고리 삭제 완료: categoryId={}, userId={}", categoryId, userId);
    }

    /**
     * SecurityContext에서 현재 사용자 ID 추출
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }
}
