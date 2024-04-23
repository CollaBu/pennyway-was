package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public record AuthStateDto(
        @Schema(description = "로그인한 사용자의 pk", example = "1")
        Long id
) {
    public AuthStateDto {
        Objects.requireNonNull(id);
    }

    public static AuthStateDto of(Long userId) {
        return new AuthStateDto(userId);
    }
}
