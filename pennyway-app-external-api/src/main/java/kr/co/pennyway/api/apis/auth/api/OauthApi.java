package kr.co.pennyway.api.apis.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[소셜 인증 API]")
public interface OauthApi {
    @Operation(summary = "[1] 소셜 로그인", description = "기존에 Provider로 가입한 사용자는 로그인, 가입하지 않은 사용자는 전화번호 인증으로 이동")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공 - 기존 계정 있음", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "user": {
                                        "id": 1
                                    }
                                }
                            }
                            """),
                    @ExampleObject(name = "성공 - 기존 계정 없음 (id -1인 경우) - [2]로 진행", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "user": {
                                        "id": -1
                                    }
                                }
                            }
                            """)
            })),
            @ApiResponse(responseCode = "401", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "실패 - 유효하지 않은 idToken", value = """
                            {
                                "code": "4013",
                                "message": "비정상적인 토큰입니다"
                            }
                            """)
            }))
    })
    ResponseEntity<?> signIn(@RequestParam Provider provider, @RequestBody @Validated SignInReq.Oauth request);

    @Operation(summary = "[2] 인증번호 발송", description = "전화번호 입력 후 인증번호 발송")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "발신 성공", value = """
                    {
                        "code": "2000",
                        "data": {
                            "sms": {
                                "to": "010-1234-5678",
                                "sendAt": "2024-04-04 00:31:57",
                                "expiresAt": "2024-04-04 00:36:57"
                            }
                        }
                    }
                    """)
    }))
    ResponseEntity<?> sendCode(@RequestParam Provider provider, @RequestBody @Validated PhoneVerificationDto.PushCodeReq request);

    @Operation(summary = "[3] 전화번호 인증", description = "전화번호 인증 후 이미 계정이 존재하면 연동, 없으면 회원가입")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공 - 기존 계정 있음 - [4-1]로 진행", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "sms": {
                                        "code": true,
                                        "existsUser": true,
                                        "username": "pennyway"
                                    }
                                }
                            }
                            """),
                    @ExampleObject(name = "성공 - 기존 계정 없음 - [4-2]로 진행", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "sms": {
                                        "code": true,
                                        "existsUser": false
                                    }
                                }
                            }
                            """)
            })),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "해당 provider로 로그인한 이력이 이미 존재함", value = """
                            {
                                "code": "4004",
                                "message": "이미 해당 제공자로 가입된 사용자입니다."
                            }
                            """)
            })),
            @ApiResponse(responseCode = "401", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "인증코드 불일치", value = """
                            {
                                "code": "4010",
                                "message": "인증코드가 일치하지 않습니다."
                            }
                            """)
            })),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "만료 혹은 등록되지 않은 휴대폰", value = """
                            {
                                "code": "4042",
                                "message": "만료되었거나 등록되지 않은 휴대폰 정보입니다."
                            }
                            """),
            }))
    })
    ResponseEntity<?> verifyCode(@RequestParam Provider provider, @RequestBody @Validated PhoneVerificationDto.VerifyCodeReq request);

    @Operation(summary = "[4-1] 계정 연동", description = "일반 혹은 소셜 계정이 존재하는 경우 연동")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            headers = {
                    @Header(name = "Set-Cookie", description = "리프레시 토큰", schema = @Schema(type = "string"), required = true),
                    @Header(name = "Authorization", description = "액세스 토큰", schema = @Schema(type = "string", format = "jwt"), required = true)
            },
            content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "user": {
                                        "id": 1
                                    }
                                }
                            }
                            """)
            }))
    ResponseEntity<?> linkAuth(@RequestParam Provider provider, @RequestBody @Validated SignUpReq.SyncWithAuth request);

    @Operation(summary = "[4-2] 소셜 회원가입", description = "회원 정보 입력 후 회원가입")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            headers = {
                    @Header(name = "Set-Cookie", description = "리프레시 토큰", schema = @Schema(type = "string"), required = true),
                    @Header(name = "Authorization", description = "액세스 토큰", schema = @Schema(type = "string", format = "jwt"), required = true)
            },
            content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "user": {
                                        "id": 1
                                    }
                                }
                            }
                            """)
            }))
    ResponseEntity<?> signUp(@RequestParam Provider provider, @RequestBody @Validated SignUpReq.Oauth request);
}
