package com.todolist.repository;

import com.todolist.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 사용자의 카테고리 목록 조회 (displayOrder 순)
     */
    List<Category> findByUserIdOrderByDisplayOrder(Long userId);

    /**
     * 사용자의 특정 카테고리 조회
     */
    Optional<Category> findByCategoryIdAndUserId(Long categoryId, Long userId);

    /**
     * 사용자의 카테고리명 중복 확인
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * 사용자의 카테고리명 중복 확인 (자신 제외)
     */
    boolean existsByUserIdAndNameAndCategoryIdNot(Long userId, String name, Long categoryId);
}
