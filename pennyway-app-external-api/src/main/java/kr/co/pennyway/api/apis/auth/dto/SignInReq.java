package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class SignInReq {
    @Schema(name = "signInReqGeneral", title = "로그인 요청")
    public record General(
            @Schema(description = "아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            String username,
            @Schema(description = "비밀번호", example = "pennyway1234")
            @NotBlank(message = "비밀번호를 입력해주세요")
            String password
    ) {
    }

    @Schema(name = "signInReqOauth", title = "소셜 로그인 요청")
    public record Oauth(
            @Schema(description = "OAuth id")
            @NotBlank(message = "OAuth id는 필수 입력값입니다.")
            String oauthId,
            @Schema(description = "OIDC 토큰")
            @NotBlank(message = "OIDC 토큰은 필수 입력값입니다.")
            String idToken,
            @Schema(description = "OIDC nonce")
            @NotBlank(message = "OIDC nonce는 필수 입력값입니다.")
            String nonce
    ) {
    }
}
