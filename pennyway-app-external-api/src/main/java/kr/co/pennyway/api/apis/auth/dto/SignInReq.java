package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.api.common.validator.Password;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignInReq {
    public record General(
            @Schema(description = "아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            @Pattern(regexp = "^[a-z-_.]{5,20}$", message = "5~20자의 영문 소문자, -, _, . 만 사용 가능합니다.")
            String username,
            @Schema(description = "비밀번호", example = "pennyway1234")
            @NotBlank(message = "비밀번호를 입력해주세요")
            @Password(message = "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)")
            String password
    ) {
    }
}
