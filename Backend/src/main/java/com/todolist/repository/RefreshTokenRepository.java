package com.todolist.repository;

import com.todolist.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token Repository
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰으로 조회 (폐기되지 않은 것만)
     */
    Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);

    /**
     * 사용자의 모든 토큰 폐기
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.userId = :userId")
    void revokeAllByUserId(Long userId);

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
