package kr.co.pennyway.api.apis.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

public record AuthStateDto(
        @Schema(description = "로그인 여부", example = "true")
        boolean isSignIn,
        @Schema(description = "사용자 pk. isSignIn이 false인 경우에는 존재하지 않는 속성", example = "1")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long userId
) {
    public static AuthStateDto of(boolean isSignIn) {
        return new AuthStateDto(isSignIn, null);
    }

    public static AuthStateDto of(boolean isSignIn, Long userId) {
        assert userId != null;
        return new AuthStateDto(isSignIn, userId);
    }
}
