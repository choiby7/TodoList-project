package com.todolist.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String password;

    @NotBlank(message = "사용자 이름은 필수입니다")
    @Size(min = 2, max = 20, message = "사용자 이름은 2자 이상 20자 이하여야 합니다")
    private String username;

    @NotNull(message = "서비스 이용약관 동의는 필수입니다")
    @AssertTrue(message = "서비스 이용약관에 동의해주세요")
    private Boolean termsAgreed;

    @NotNull(message = "개인정보 처리방침 동의는 필수입니다")
    @AssertTrue(message = "개인정보 처리방침에 동의해주세요")
    private Boolean privacyAgreed;
}
