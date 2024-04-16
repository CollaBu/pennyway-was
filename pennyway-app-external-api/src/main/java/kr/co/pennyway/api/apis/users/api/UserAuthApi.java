package kr.co.pennyway.api.apis.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "[사용자 인증 관리 API]", description = "사용자의 인증과 관련된 UseCase(로그아웃, 소셜 계정 연동/해지 등)를 제공하는 API")
public interface UserAuthApi {
    @Operation(summary = "로그아웃", description = "사용자의 로그아웃을 수행한다. Access Token과 Refresh Token을 받아서 Access Token을 만료시키고, Refresh Token을 삭제한다.")
    @Parameters({
            @Parameter(name = "Authorization", description = "Access Token", required = true),
            @Parameter(name = "refreshToken", description = "Refresh Token", required = false)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "401", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "유효하지 않은 refresh token", value = """
                            {
                                "code": "4013",
                                "message": "비정상적인 토큰입니다"
                            }
                            """),
                    @ExampleObject(name = "소유권이 없는 refresh token 삭제 요청", value = """
                            {
                                "code": "4014",
                                "message": "소유권이 없는 리프레시 토큰입니다"
                            }
                            """)
            }))
    })
    ResponseEntity<?> signOut(
            @RequestHeader("Authorization") String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @AuthenticationPrincipal SecurityUserDetails user
    );
}
