package com.todolist.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 인증 사용자 정보 (userId 포함)
 * Spring Security의 OAuth2User를 확장하여 우리 DB의 userId를 포함
 *
 * @since 2026-02-18
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final Long userId;
    private final boolean isNewUser;
    private final boolean needsAccountMerge;
    private final String existingEmail;

    public CustomOAuth2User(OAuth2User oauth2User, Long userId) {
        this(oauth2User, userId, false, false, null);
    }

    public CustomOAuth2User(OAuth2User oauth2User, Long userId, boolean isNewUser,
                            boolean needsAccountMerge, String existingEmail) {
        this.oauth2User = oauth2User;
        this.userId = userId;
        this.isNewUser = isNewUser;
        this.needsAccountMerge = needsAccountMerge;
        this.existingEmail = existingEmail;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getName();
    }
}
