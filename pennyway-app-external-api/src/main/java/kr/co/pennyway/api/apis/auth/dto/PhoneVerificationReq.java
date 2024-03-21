package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PhoneVerificationReq {
    @Schema(title = "인증번호 요청 DTO", description = "전화번호로 인증번호 송신 요청을 위한 DTO")
    public record PushCodeReq(
            @Schema(description = "전화번호", example = "01012345678")
            @NotNull(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$\n", message = "전화번호 형식이 올바르지 않습니다.")
            String phone
    ) {
    }

    @Schema(title = "인증번호 검증 DTO", description = "전화번호로 인증번호 검증 요청을 위한 DTO")
    public record VerifyCodeReq(
            @Schema(description = "전화번호", example = "01012345678")
            @NotNull(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$\n", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code
    ) {
    }
}
