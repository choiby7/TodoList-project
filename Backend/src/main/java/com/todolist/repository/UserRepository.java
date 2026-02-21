package com.todolist.repository;

import com.todolist.domain.OAuth2Provider;
import com.todolist.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);

    /**
     * OAuth2 제공자와 제공자 ID로 사용자 조회
     */
    Optional<User> findByProviderAndProviderId(OAuth2Provider provider, String providerId);
}
