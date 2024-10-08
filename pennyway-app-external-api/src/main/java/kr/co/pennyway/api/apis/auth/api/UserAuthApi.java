package kr.co.pennyway.api.apis.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.AuthStateDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.common.annotation.ApiExceptionExplanation;
import kr.co.pennyway.api.common.annotation.ApiResponseExplanations;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[사용자 인증 관리 API]", description = "사용자의 인증과 관련된 UseCase(로그아웃, 소셜 계정 연동/해지 등)를 제공하는 API")
public interface UserAuthApi {
    @Operation(summary = "로그인 상태 확인", description = "사용자의 로그인 상태를 확인하고 토큰에 등록된 사용자 pk값을 확인한다. 만약, 토큰이 만료되었거나 유효하지 않은 경우에는 401 에러를 반환한다.")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, hidden = true)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schemaProperties = @SchemaProperty(name = "user", schema = @Schema(implementation = AuthStateDto.class))))
    ResponseEntity<?> getAuthState(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authHeader);

    @Operation(summary = "로그아웃", description = """
            사용자의 로그아웃을 수행한다. Access Token과 Refresh Token을 받아서 Access Token을 만료시키고, Refresh Token을 삭제한다. <br>
            Refresh Token이 없는 경우에는 Access Token만 만료시킨다. (만료된 refesh token이면 access token 만료만 수행) <br>
            만약, Refresh Token이 유효하지 않거나 소유권이 없는 경우에는 401 에러를 반환한다. <br>
            access token이 인가 과정 중에 성공했어도, 삭제 등록 시 만료되면 refresh token만 제거하고 401_EXPIRED_TOKEN 응답을 반환한다.
            """)
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

    @Operation(summary = "소셜 계정 연동", description = "인증된 사용자의 소셜 계정을 연동한다. 이미 연동된 계정이 있는 경우에는 409 에러를 반환한다. 미인증 사용자는 해당 API를 사용할 수 없다.")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = OauthErrorCode.class, constant = "ALREADY_USED_OAUTH", name = "다른 사용자가 사용 중"),
                    @ApiExceptionExplanation(value = OauthErrorCode.class, constant = "ALREADY_SIGNUP_OAUTH", name = "이미 연동된 계정")
            }
    )
    ResponseEntity<?> linkOauth(@RequestParam Provider provider, @RequestBody @Validated SignInReq.Oauth request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "소셜 계정 연동 해제", description = "인증된 사용자의 소셜 계정 연동을 해제한다. 연동되지 않은 계정을 해제하려고 하는 경우에는 404 에러를 반환한다. 미인증 사용자는 해당 API를 사용할 수 없다.")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "해당 provider로 로그인한 이력이 없음", value = """
                            {
                                "code": "4040",
                                "message": "해당 제공자로 가입된 이력을 찾을 수 없습니다."
                            }
                            """)
            })),
            @ApiResponse(responseCode = "409", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "연결 해제 요청 실패", value = """
                            {
                                "code": "4090",
                                "message": "해당 제공자로만 가입된 사용자는 연동을 해제할 수 없습니다."
                            }
                            """, description = "일반 회원 가입 이력이 없고, 연동된 소셜 계정이 해지를 요청하는 제공자 하나 뿐인 경우 -> 계정 삭제 API를 호출해야 한다.")
            }))
    })
    ResponseEntity<?> unlinkOauth(@RequestParam Provider provider, @AuthenticationPrincipal SecurityUserDetails user);
}
