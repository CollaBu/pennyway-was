package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public record AuthStateDto(
        @Schema(description = "사용자 pk. isSignIn이 false인 경우에는 존재하지 않는 속성", example = "1")
        Long id
) {
    public AuthStateDto {
        Objects.requireNonNull(id);
    }

    public static AuthStateDto of(Long userId) {
        return new AuthStateDto(userId);
    }
}
