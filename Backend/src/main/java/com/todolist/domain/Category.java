package com.todolist.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 카테고리 엔티티
 */
@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color_code", nullable = false, length = 7)
    private String colorCode = "#3B82F6";

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Category(Long userId, String name, String colorCode, String icon, Integer displayOrder) {
        this.userId = userId;
        this.name = name;
        if (colorCode != null) {
            this.colorCode = colorCode;
        }
        this.icon = icon;
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    /**
     * 카테고리 정보 업데이트
     */
    public void update(String name, String colorCode, String icon) {
        if (name != null) {
            this.name = name;
        }
        if (colorCode != null) {
            this.colorCode = colorCode;
        }
        if (icon != null) {
            this.icon = icon;
        }
    }

    /**
     * 표시 순서 변경
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
