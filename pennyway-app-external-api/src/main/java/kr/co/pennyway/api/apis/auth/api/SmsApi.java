package kr.co.pennyway.api.apis.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.common.query.VerificationType;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[인증코드 SMS 요청]", description = "SMS 인증 관련 API")
public interface SmsApi {
    @Operation(summary = "전화번호로 인증코드 전송", description = "전화번호로 인증번호를 전송합니다. 미인증 사용자만 가능합니다.")
    @Parameters({
            @Parameter(name = "type", description = "인증 타입", required = true, examples = {
                    @ExampleObject(name = "일반 회원가입", value = "general"), @ExampleObject(name = "소셜 회원가입", value = "oauth"), @ExampleObject(name = "아이디 찾기", value = "username"), @ExampleObject(name = "비밀번호 찾기", value = "password"), @ExampleObject(name = "휴대폰 번호 변경", value = "phone")
            }, in = ParameterIn.QUERY),
            @Parameter(name = "provider", description = "소셜 로그인 제공자. type이 oauth인 경우 반드시 포함되어야 한다.", required = false, examples = {
                    @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
            }, in = ParameterIn.QUERY)
    })
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "발신 성공", value = """
                    {
                        "code": "2000",
                        "data": {
                            "sms": {
                                "to": "010-2629-4624",
                                "sendAt": "2024-04-04 00:31:57",
                                "expiresAt": "2024-04-04 00:36:57"
                            }
                        }
                    }
                    """)
    }))
    ResponseEntity<?> sendCode(@RequestParam(value = "type") VerificationType type, @RequestParam(name = "provider", required = false) Provider provider, @RequestBody @Validated PhoneVerificationDto.PushCodeReq request);
}
