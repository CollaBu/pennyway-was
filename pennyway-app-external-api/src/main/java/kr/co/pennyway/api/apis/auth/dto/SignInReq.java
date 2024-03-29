package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignInReq {
    @Schema(title = "로그인 요청")
    public record General(
            @Schema(description = "아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            String username,
            @Schema(description = "비밀번호", example = "pennyway1234")
            @NotBlank(message = "비밀번호를 입력해주세요")
            String password
    ) {
    }
}
