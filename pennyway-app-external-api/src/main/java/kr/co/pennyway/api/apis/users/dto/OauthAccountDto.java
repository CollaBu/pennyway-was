package kr.co.pennyway.api.apis.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Oauth 계정 정보 응답")
public record OauthAccountDto(
        @Schema(description = "카카오 계정 연동 여부", example = "true")
        boolean kakao,
        @Schema(description = "구글 계정 연동 여부", example = "false")
        boolean google,
        @Schema(description = "애플 계정 연동 여부", example = "false")
        boolean apple
) {
    public static OauthAccountDto of(boolean kakao, boolean google, boolean apple) {
        return new OauthAccountDto(kakao, google, apple);
    }
}
