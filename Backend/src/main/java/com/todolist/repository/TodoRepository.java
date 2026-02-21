package com.todolist.repository;

import com.todolist.domain.Todo;
import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Todo Repository
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {

    /**
     * 사용자의 삭제되지 않은 Todo 목록 조회 (페이징)
     */
    Page<Todo> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    /**
     * 사용자의 휴지통 목록 조회 (페이징)
     */
    Page<Todo> findByUserIdAndIsDeletedTrue(Long userId, Pageable pageable);

    /**
     * 사용자의 특정 Todo 조회 (삭제 여부 무관)
     */
    Optional<Todo> findByTodoIdAndUserId(Long todoId, Long userId);

    /**
     * 동적 필터링을 위한 쿼리
     * status, priority, categoryId, keyword, dueFrom, dueTo 조건 적용
     *
     * 주석 처리 이유: PostgreSQL에서 (:param IS NULL OR field = :param) 패턴 사용 시
     * 파라미터 타입 추론 실패 (ERROR: could not determine data type of parameter $2)
     * JpaSpecificationExecutor를 통한 Specification 방식으로 대체
     */
    /*
    @Query("SELECT t FROM Todo t WHERE t.userId = :userId " +
            "AND t.isDeleted = false " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:categoryId IS NULL OR t.categoryId = :categoryId) " +
            "AND (:keyword IS NULL OR t.title LIKE %:keyword% OR t.description LIKE %:keyword%) " +
            "AND (:dueFrom IS NULL OR t.dueDate >= :dueFrom) " +
            "AND (:dueTo IS NULL OR t.dueDate <= :dueTo)")
    Page<Todo> findTodosWithFilters(
            @Param("userId") Long userId,
            @Param("status") TodoStatus status,
            @Param("priority") TodoPriority priority,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("dueFrom") LocalDateTime dueFrom,
            @Param("dueTo") LocalDateTime dueTo,
            Pageable pageable
    );
    */

    /**
     * 카테고리별 Todo 개수 조회 (삭제되지 않은 것만)
     */
    long countByCategoryIdAndIsDeletedFalse(Long categoryId);

    /**
     * 사용자의 완료된 Todo 개수
     */
    long countByUserIdAndStatusAndIsDeletedFalse(Long userId, TodoStatus status);

    /**
     * 사용자의 기한이 지난 Todo 목록
     */
    List<Todo> findByUserIdAndIsDeletedFalseAndDueDateBefore(Long userId, LocalDateTime dateTime);
}
