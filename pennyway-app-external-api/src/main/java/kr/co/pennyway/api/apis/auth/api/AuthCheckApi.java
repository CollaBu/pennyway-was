package kr.co.pennyway.api.apis.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[계정 검사 API]")
public interface AuthCheckApi {
    @Operation(summary = "닉네임 중복 검사")
    ResponseEntity<?> checkUsername(@RequestParam @Validated String username);

    @Operation(summary = "일반 회원 아이디 찾기")
    @Parameter(name = "phone", description = "휴대폰 번호", required = true, in = ParameterIn.QUERY, example = "010-1234-5678")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "일반 회원으로 등록된 휴대폰 번호일 경우", value = """
                                {
                                    "code": "2000",
                                    "data": {
                                        "user": {
                                            "username": "pennyway"
                                        }
                                    }
                                }
                            """)
            })),
            @ApiResponse(responseCode = "404", description = "일반 회원으로 등록되지 않은 휴대폰 번호일 경우", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "일반 회원으로 등록되지 않은 휴대폰 번호일 경우", value = """
                                {
                                    "code": "4040",
                                    "message": "일반 회원으로 등록되지 않은 휴대폰 번호입니다."
                                }
                            """)
            })),
            @ApiResponse(responseCode = "404", description = "인증번호 만료 또는 유효하지 않은 경우", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "인증번호 만료 또는 유효하지 않은 경우", value = """
                                {
                                    "code": "4042",
                                    "message": "인증번호가 만료되었거나 유효하지 않습니다."
                                }
                            """)
            })),
    })
    ResponseEntity<?> findUsername(@Validated PhoneVerificationDto.VerifyCodeReq request);

    @Operation(summary = "일반 회원 비밀번호 찾기에 사용되는 비밀번호 변경")
    ResponseEntity<?> findPassword(@Validated PhoneVerificationDto.VerifyCodeReq request, @Validated AuthFindDto.PasswordReq passwordReq);
}
