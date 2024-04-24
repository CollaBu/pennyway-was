package kr.co.pennyway.api.apis.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[인증 API]")
public interface AuthApi {
    @Operation(summary = "[2] 일반 회원가입 인증번호 검증", description = "인증번호를 검증합니다. 미인증 사용자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "검증 성공 - 기존에 등록한 소셜 계정 없음 - [3-1]로 진행", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "sms": {
                                        "code": true,
                                        "oauth": false
                                    }
                                }
                            }
                            """),
                    @ExampleObject(name = "검증 성공 - 기존에 등록한 소셜 계정 있음 - [3-2]로 진행", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "sms": {
                                        "code": true,
                                        "oauth": true,
                                        "username": "pennyway"
                                    }
                                }
                            }
                            """)
            })),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "일반 회원가입 계정이 이미 존재함", value = """
                            {
                                "code": "4004",
                                "message": "이미 회원가입한 유저입니다."
                            }
                            """)
            })),
            @ApiResponse(responseCode = "401", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "검증 실패", value = """
                            {
                                "code": "4010",
                                "message": "인증번호가 일치하지 않습니다."
                            }
                            """)
            })),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "검증 실패 - 인증번호 만료", value = """
                            {
                                "code": "4042",
                                "message": "만료되었거나 등록되지 않은 휴대폰 정보입니다."
                            }
                            """)
            }))
    })
    ResponseEntity<?> verifyCode(@RequestBody @Validated PhoneVerificationDto.VerifyCodeReq request);

    @Operation(summary = "[3-1] 일반 회원가입", description = "일반 회원가입을 진행합니다. 미인증 사용자만 가능합니다.")
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
    ResponseEntity<?> signUp(@RequestBody @Validated SignUpReq.General request);

    @Operation(summary = "[3-2] 기존 소셜 계정에 일반 계정을 연동하는 회원가입", description = "소셜 계정과 연동할 일반 계정 정보를 입력합니다. 미인증 사용자만 가능합니다.")
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
    ResponseEntity<?> linkOauth(@RequestBody @Validated SignUpReq.SyncWithOauth request);

    @Operation(summary = "[4] 일반 로그인", description = "아이디와 비밀번호로 로그인합니다. 미인증 사용자만 가능합니다.")
    @ApiResponses({
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
                    })),
            @ApiResponse(responseCode = "401", description = "로그인 실패", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "실패 - 유효하지 않은 아이디/비밀번호", value = """
                            {
                                "code": "4010",
                                "message": "유효하지 않은 아이디 또는 비밀번호입니다."
                            }
                            """)
            }))
    })
    ResponseEntity<?> signIn(@RequestBody @Validated SignInReq.General request);

    @Operation(summary = "[5] 토큰 갱신", description = "리프레시 토큰을 이용해 액세스 토큰과 리프레시 토큰을 갱신합니다.")
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
    ResponseEntity<?> refresh(@CookieValue("refreshToken") @Valid String refreshToken);
}
