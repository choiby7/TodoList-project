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
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "terms_agreed_at")
    private LocalDateTime termsAgreedAt;

    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private OAuth2Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "profile_image")
    private String profileImage;

    @Builder
    public User(String email, String passwordHash, String username,
                OAuth2Provider provider, String providerId, String profileImage) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = profileImage;
        // OAuth2로 가입한 경우 이메일 자동 인증
        if (provider != null) {
            this.emailVerified = true;
        }
    }

    /**
     * 계정 잠금 여부 확인
     */
    public boolean isAccountLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockedUntil);
    }

    /**
     * 로그인 실패 시 호출
     * 5회 실패 시 15분 잠금
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(15);
        }
    }

    /**
     * 로그인 성공 시 호출
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /**
     * 프로필 업데이트
     */
    public void updateProfile(String username, String profileImageUrl) {
        if (username != null) {
            this.username = username;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * OAuth2 프로바이더 연결 (계정 병합)
     * 기존 일반 계정에 OAuth2 로그인 추가
     */
    public void linkOAuthProvider(OAuth2Provider provider, String providerId, String profileImage) {
        this.provider = provider;
        this.providerId = providerId;
        if (profileImage != null && !profileImage.isEmpty()) {
            this.profileImage = profileImage;
        }
        this.emailVerified = true; // OAuth2는 이메일 자동 인증
    }

    /**
     * OAuth2 전용 계정 여부 (비밀번호 없음)
     * OAuth2로만 가입한 경우 true 반환
     */
    public boolean isOAuth2Only() {
        return provider != null && (passwordHash == null || passwordHash.isEmpty());
    }

    /**
     * 약관 동의 처리
     */
    public void agreeToTerms() {
        this.termsAgreedAt = LocalDateTime.now();
        this.privacyAgreedAt = LocalDateTime.now();
    }

    /**
     * 약관 동의 여부 확인
     */
    public boolean hasAgreedToTerms() {
        return termsAgreedAt != null && privacyAgreedAt != null;
    }
}
