package com.todolist.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Todo 엔티티
 */
@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "priority", nullable = false, columnDefinition = "priority_level")
    private TodoPriority priority = TodoPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "todo_status")
    private TodoStatus status = TodoStatus.TODO;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "is_important", nullable = false)
    private Boolean isImportant = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Todo(Long userId, Long categoryId, String title, String description,
                TodoPriority priority, TodoStatus status, LocalDateTime dueDate, Boolean isImportant) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        if (priority != null) {
            this.priority = priority;
        }
        if (status != null) {
            this.status = status;
        }
        this.dueDate = dueDate;
        if (isImportant != null) {
            this.isImportant = isImportant;
        }
    }

    /**
     * Todo 정보 업데이트
     */
    public void update(String title, String description, TodoPriority priority,
                       TodoStatus status, LocalDateTime dueDate, Long categoryId, Boolean isImportant) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (priority != null) {
            this.priority = priority;
        }
        if (status != null) {
            this.status = status;
            // COMPLETED 상태로 변경 시 completedAt 설정
            if (status == TodoStatus.COMPLETED && this.completedAt == null) {
                this.completedAt = LocalDateTime.now();
            }
            // COMPLETED가 아닌 상태로 변경 시 completedAt 초기화
            if (status != TodoStatus.COMPLETED) {
                this.completedAt = null;
            }
        }
        if (dueDate != null) {
            this.dueDate = dueDate;
        }
        if (categoryId != null) {
            this.categoryId = categoryId;
        }
        if (isImportant != null) {
            this.isImportant = isImportant;
        }
    }

    /**
     * 완료 상태 토글
     */
    public void toggleComplete() {
        if (this.status == TodoStatus.COMPLETED) {
            this.status = TodoStatus.TODO;
            this.completedAt = null;
        } else {
            this.status = TodoStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * 소프트 삭제
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 복구 (휴지통에서 복원)
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
}
