package com.todolist.security;

import com.todolist.domain.User;
import com.todolist.exception.ErrorCode;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Spring Security UserDetailsService 구현
 * userId로 사용자 정보를 로드합니다.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getUserId()),
                user.getPasswordHash(),
                new ArrayList<>()  // 권한은 빈 리스트
        );
    }
}
