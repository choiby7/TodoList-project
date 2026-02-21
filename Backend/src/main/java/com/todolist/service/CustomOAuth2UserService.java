package com.todolist.service;

import com.todolist.domain.OAuth2Provider;
import com.todolist.domain.User;
import com.todolist.exception.BadRequestException;
import com.todolist.exception.ConflictException;
import com.todolist.exception.ErrorCode;
import com.todolist.repository.UserRepository;
import com.todolist.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 사용자 정보 조회 및 계정 생성/병합 서비스
 *
 * @since 2026-02-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 인증 후 사용자 정보 로드
     * 1. Google에서 사용자 정보 추출 (이메일, 이름, 프로필 사진)
     * 2. provider + providerId로 기존 사용자 조회
     * 3. 없으면 이메일로 조회 → 있으면 계정 병합
     * 4. 없으면 신규 생성
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Google로부터 사용자 정보 가져오기
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 2. Provider 타입 결정
        OAuth2Provider provider = OAuth2Provider.valueOf(registrationId.toUpperCase());

        // 3. 사용자 정보 추출
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = extractEmail(attributes, provider);
        String name = extractName(attributes, provider);
        String providerId = extractProviderId(attributes, provider);
        String profileImage = extractProfileImage(attributes, provider);

        // 4. 이메일 필수 검증
        if (email == null || email.isEmpty()) {
            log.error("[OAuth2] 이메일 정보 없음 - provider: {}, attributes: {}", provider, attributes);
            throw new BadRequestException(ErrorCode.AUTH_OAUTH2_EMAIL_NOT_PROVIDED);
        }

        // 5. 사용자 조회 또는 생성
        UserCreationResult result = findOrCreateUser(provider, providerId, email, name, profileImage);

        // 6. CustomOAuth2User 반환 (추가 정보 포함)
        return new CustomOAuth2User(
                oauth2User,
                result.getUser().getUserId(),
                result.isNewUser(),
                result.isNeedsAccountMerge(),
                result.getExistingEmail()
        );
    }

    /**
     * 사용자 조회 또는 생성 결과
     */
    private static class UserCreationResult {
        private final User user;
        private final boolean isNewUser;
        private final boolean needsAccountMerge;
        private final String existingEmail;

        public UserCreationResult(User user, boolean isNewUser, boolean needsAccountMerge, String existingEmail) {
            this.user = user;
            this.isNewUser = isNewUser;
            this.needsAccountMerge = needsAccountMerge;
            this.existingEmail = existingEmail;
        }

        public User getUser() {
            return user;
        }

        public boolean isNewUser() {
            return isNewUser;
        }

        public boolean isNeedsAccountMerge() {
            return needsAccountMerge;
        }

        public String getExistingEmail() {
            return existingEmail;
        }
    }

    /**
     * 사용자 조회 또는 생성 (계정 병합 로직 포함)
     */
    private UserCreationResult findOrCreateUser(OAuth2Provider provider, String providerId,
                                                 String email, String name, String profileImage) {
        // 1. provider + providerId로 조회 (이미 OAuth2로 가입한 경우)
        Optional<User> existingOAuth2User = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existingOAuth2User.isPresent()) {
            log.info("[OAuth2] 기존 OAuth2 사용자 로그인 - email: {}, provider: {}", email, provider);
            return new UserCreationResult(existingOAuth2User.get(), false, false, null);
        }

        // 2. 이메일로 조회 (일반 가입 또는 다른 provider로 가입한 경우)
        Optional<User> existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();

            // 2-1. 이미 다른 provider와 연결된 경우 → 에러
            if (user.getProvider() != null && user.getProvider() != provider) {
                log.error("[OAuth2] 계정 병합 충돌 - email: {}, existing provider: {}, new provider: {}",
                        email, user.getProvider(), provider);
                throw new ConflictException(ErrorCode.AUTH_OAUTH2_ACCOUNT_MERGE_CONFLICT);
            }

            // 2-2. 일반 계정 → OAuth2 연동 필요 (사용자 동의 필요)
            if (user.getProvider() == null) {
                log.info("[OAuth2] 계정 병합 동의 필요 - email: {}, provider: {}", email, provider);
                // 임시 OAuth2 사용자 생성 (병합 대기, 유효한 이메일 형식 사용)
                String tempEmail = "pending." + System.currentTimeMillis() + "@oauth2.temp";
                User pendingUser = User.builder()
                        .email(tempEmail)
                        .username(name)
                        .passwordHash(null)
                        .provider(provider)
                        .providerId(providerId)
                        .profileImage(profileImage)
                        .build();
                User savedPendingUser = userRepository.save(pendingUser);
                return new UserCreationResult(savedPendingUser, false, true, email);
            }

            // 2-3. 동일 provider (이론적으로 발생하지 않지만 안전장치)
            return new UserCreationResult(user, false, false, null);
        }

        // 3. 신규 사용자 생성 (OAuth2 전용, 비밀번호 없음, 약관 미동의)
        log.info("[OAuth2] 신규 사용자 생성 (약관 동의 필요) - email: {}, provider: {}", email, provider);
        User newUser = User.builder()
                .email(email)
                .username(name)
                .passwordHash(null) // OAuth2 전용 계정
                .provider(provider)
                .providerId(providerId)
                .profileImage(profileImage)
                .build();
        // termsAgreedAt은 null로 유지 (약관 미동의 상태)

        User savedUser = userRepository.save(newUser);
        return new UserCreationResult(savedUser, true, false, null);
    }

    /**
     * 이메일 추출
     */
    private String extractEmail(Map<String, Object> attributes, OAuth2Provider provider) {
        return (String) attributes.get("email");
    }

    /**
     * 이름 추출
     */
    private String extractName(Map<String, Object> attributes, OAuth2Provider provider) {
        return (String) attributes.get("name");
    }

    /**
     * Provider ID 추출 (Google: sub, GitHub: id, Kakao: id)
     */
    private String extractProviderId(Map<String, Object> attributes, OAuth2Provider provider) {
        if (provider == OAuth2Provider.GOOGLE) {
            return (String) attributes.get("sub");
        } else if (provider == OAuth2Provider.GITHUB) {
            Object id = attributes.get("id");
            return id != null ? id.toString() : null;
        } else if (provider == OAuth2Provider.KAKAO) {
            Object id = attributes.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    /**
     * 프로필 이미지 URL 추출
     */
    private String extractProfileImage(Map<String, Object> attributes, OAuth2Provider provider) {
        return (String) attributes.get("picture");
    }
}
