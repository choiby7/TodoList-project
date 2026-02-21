package com.todolist.controller;

import com.todolist.dto.request.LoginRequest;
import com.todolist.dto.request.SignupRequest;
import com.todolist.dto.response.ApiResponse;
import com.todolist.dto.response.OAuth2ExchangeResponse;
import com.todolist.dto.response.TokenResponse;
import com.todolist.dto.response.UserResponse;
import com.todolist.exception.ErrorCode;
import com.todolist.exception.UnauthorizedException;
import com.todolist.security.JwtTokenProvider;
import com.todolist.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = "새 사용자를 등록합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        TokenResponse response = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 폐기하여 로그아웃합니다")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        // Bearer 토큰에서 실제 토큰 추출
        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        authService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "로그인한 사용자의 정보를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "OAuth2 세션 교환", description = "OAuth2 인증 후 토큰 또는 동의 필요 여부 확인")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "교환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "세션 만료 또는 무효")
    })
    @GetMapping("/oauth2/exchange")
    public ResponseEntity<ApiResponse<OAuth2ExchangeResponse>> exchangeOAuth2Session(
            @RequestParam(required = true) String session) {

        OAuth2ExchangeResponse response = authService.exchangeOAuth2Session(session);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "OAuth2 약관 동의", description = "신규 사용자의 약관 동의 처리 및 토큰 발급")
    @PostMapping("/oauth2/agree-terms")
    public ResponseEntity<ApiResponse<TokenResponse>> agreeToTerms(
            @RequestParam(required = true) String session) {

        TokenResponse tokenResponse = authService.agreeToTermsAndIssueToken(session);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    @Operation(summary = "OAuth2 계정 병합 동의", description = "기존 계정과 OAuth2 계정 병합 동의")
    @PostMapping("/oauth2/agree-merge")
    public ResponseEntity<ApiResponse<TokenResponse>> agreeToMerge(
            @RequestParam(required = true) String session) {

        TokenResponse tokenResponse = authService.agreeToMergeAndIssueToken(session);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }
}
