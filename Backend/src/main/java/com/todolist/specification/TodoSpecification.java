package com.todolist.specification;

import com.todolist.domain.Todo;
import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Todo 동적 쿼리 Specification
 */
public class TodoSpecification {

    /**
     * 동적 필터 조건을 적용한 Specification 생성
     */
    public static Specification<Todo> withFilters(
            Long userId,
            TodoStatus status,
            TodoPriority priority,
            Long categoryId,
            String keyword,
            LocalDateTime dueFrom,
            LocalDateTime dueTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 필수 조건: userId
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

            // 필수 조건: 삭제되지 않은 항목
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // 선택 조건: status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 선택 조건: priority
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }

            // 선택 조건: categoryId
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }

            // 선택 조건: keyword (title 또는 description에 포함)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Predicate titleLike = criteriaBuilder.like(
                        root.get("title"),
                        "%" + keyword + "%"
                );
                Predicate descriptionLike = criteriaBuilder.like(
                        root.get("description"),
                        "%" + keyword + "%"
                );
                predicates.add(criteriaBuilder.or(titleLike, descriptionLike));
            }

            // 선택 조건: dueFrom (마감일이 dueFrom 이후)
            if (dueFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dueDate"),
                        dueFrom
                ));
            }

            // 선택 조건: dueTo (마감일이 dueTo 이전)
            if (dueTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dueDate"),
                        dueTo
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
