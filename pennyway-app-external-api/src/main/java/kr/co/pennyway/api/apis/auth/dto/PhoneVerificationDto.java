package kr.co.pennyway.api.apis.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.domain.common.redis.phone.Code;

import java.time.LocalDateTime;

public class PhoneVerificationDto {
    @Schema(title = "인증번호 요청 DTO", description = "전화번호로 인증번호 송신 요청을 위한 DTO")
    public record PushCodeReq(
            @Schema(description = "전화번호", example = "01012345678")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "전화번호 인증 종류", example = "SIGN_UP")
            @NotBlank(message = "인증 종류는 필수입니다.")
            Code codeType
    ) {
    }

    @Schema(title = "인증번호 발송 응답 DTO", description = "전화번호로 인증번호 송신 응답을 위한 DTO")
    public record PushCodeRes(
            @Schema(description = "수신자 번호")
            String to,
            @Schema(description = "발송 시간")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime sendTime,
            @Schema(description = "만료 시간 (default: 3분)", example = "2021-08-01T00:00:00")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime expireTime
    ) {
        /**
         * 인증번호 발송 응답 객체 생성
         *
         * @param to         String : 수신자 번호
         * @param sendTime   LocalDateTime : 발송 시간
         * @param expireTime LocalDateTime : 만료 시간 (default: 5분)
         */
        public static PushCodeRes of(String to, LocalDateTime sendTime, LocalDateTime expireTime) {
            return new PushCodeRes(to, sendTime, expireTime);
        }
    }

    @Schema(title = "인증번호 검증 DTO", description = "전화번호로 인증번호 검증 요청을 위한 DTO")
    public record VerifyCodeReq(
            @Schema(description = "전화번호", example = "01012345678")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "전화번호 인증 종류", example = "SIGN_UP")
            @NotBlank(message = "인증 종류는 필수입니다.")
            Code codeType,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code
    ) {
    }
}
