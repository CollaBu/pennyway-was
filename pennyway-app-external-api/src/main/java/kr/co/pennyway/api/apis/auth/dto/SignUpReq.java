package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.api.common.validator.Password;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;

/**
 * 회원가입 요청 Dto
 * <br/>
 * 일반 회원가입 시엔 General, 소셜 회원가입 시엔 Oauth를 사용합니다.
 */
public class SignUpReq {
    @Schema(title = "일반 회원가입 요청 DTO")
    public record General(
            @Schema(description = "아이디", example = "pennyway")
            @NotEmpty(message = "아이디를 입력해주세요")
            @Pattern(regexp = "^[a-z-_.]{5,20}$", message = "5~20자의 영문 소문자, -, _, . 만 사용 가능합니다.")
            String username,
            @Schema(description = "이름", example = "페니웨이")
            @NotEmpty(message = "이름을 입력해주세요")
            @Pattern(regexp = "^[가-힣a-zA-Z]{2,20}$", message = "2~20자의 한글, 영문 대/소문자만 사용 가능합니다.")
            String name,
            @Schema(description = "비밀번호", example = "pennyway1234")
            @NotEmpty(message = "비밀번호를 입력해주세요")
            @Password(message = "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)")
            String password,
            @Schema(description = "전화번호", example = "01012345678")
            @NotEmpty(message = "전화번호를 입력해주세요")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$\n", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code
    ) {
        public User toEntity() {
            return User.builder()
                    .username(username)
                    .name(name)
                    .password(password)
                    .phone(phone)
                    .role(Role.USER)
                    .profileVisibility(ProfileVisibility.PUBLIC)
                    .build();
        }
        
    }

    @Schema(title = "소셜 회원가입 요청 DTO")
    public record Oauth(

    ) {

    }
}
