package com.todolist.config;

import com.todolist.security.JwtAuthenticationFilter;
import com.todolist.security.OAuth2AuthenticationFailureHandler;
import com.todolist.security.OAuth2AuthenticationSuccessHandler;
import com.todolist.security.RateLimitFilter;
import com.todolist.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2FailureHandler;

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 정책 (완전 STATELESS - JWT 기반 인증)
                // OAuth2는 Redis 세션으로 별도 관리하므로 Spring Security 세션 불필요
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 실패 시 JSON 에러 반환 (Google 리다이렉트 방지)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"success\":false,\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다\"}");
                        })
                )

                // 요청 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 공개 엔드포인트
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/oauth2/exchange",      // OAuth2 세션 교환
                                "/api/v1/auth/oauth2/agree-terms",   // OAuth2 약관 동의
                                "/api/v1/auth/oauth2/agree-merge",   // OAuth2 계정 병합 동의
                                "/login/oauth2/code/**",             // OAuth2 콜백
                                "/oauth2/authorization/**",          // OAuth2 시작
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 Login 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                )

                // H2 Console 사용을 위한 설정
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()))

                // Rate Limit 필터 추가 (JWT 필터보다 먼저 실행)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정 (환경 변수 기반)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경 변수에서 Frontend URL 읽기
        configuration.setAllowedOrigins(List.of(
                frontendUrl,
                "http://localhost:8080"  // Swagger/Actuator 로컬 테스트용
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * BCrypt 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // 강도 12
    }
}
