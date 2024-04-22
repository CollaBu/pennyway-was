package kr.co.pennyway.api.apis.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
            String idToken
    ) {
    }

    @Schema(title = "로그인 상태 확인")
    public record State(
            @Schema(description = "로그인 여부", example = "true")
            boolean isSignIn,
            @Schema(description = "사용자 pk", example = "1")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Long userId
    ) {
        public static State of(boolean isSignIn) {
            return new State(isSignIn, null);
        }

        public static State of(boolean isSignIn, Long userId) {
            assert userId != null;
            return new State(isSignIn, userId);
        }
    }
}
