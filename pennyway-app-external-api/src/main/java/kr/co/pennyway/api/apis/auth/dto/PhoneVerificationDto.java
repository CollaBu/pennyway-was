package kr.co.pennyway.api.apis.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class PhoneVerificationDto {
    @Schema(title = "인증번호 요청 DTO", description = "전화번호로 인증번호 송신 요청을 위한 DTO")
    public record PushCodeReq(
            @Schema(description = "전화번호", example = "01012345678")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone
    ) {
    }

    @Schema(title = "인증번호 발송 응답 DTO", description = "전화번호로 인증번호 송신 응답을 위한 DTO")
    public record PushCodeRes(
            @Schema(description = "수신자 번호")
            String to,
            @Schema(description = "발송 시간")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime sendAt,
            @Schema(description = "만료 시간 (default: 3분)", example = "2021-08-01T00:00:00")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime expiresAt
    ) {
        /**
         * 인증번호 발송 응답 객체 생성
         *
         * @param to        String : 수신자 번호
         * @param sendAt    LocalDateTime : 발송 시간
         * @param expiresAt LocalDateTime : 만료 시간 (default: 5분)
         */
        public static PushCodeRes of(String to, LocalDateTime sendAt, LocalDateTime expiresAt) {
            return new PushCodeRes(to, sendAt, expiresAt);
        }
    }

    @Schema(title = "인증번호 검증 DTO", description = "전화번호로 인증번호 검증 요청을 위한 DTO")
    public record VerifyCodeReq(
            @Schema(description = "전화번호", example = "01012345678")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code
    ) {
        public static VerifyCodeReq from(SignUpReq.Info request) {
            return new VerifyCodeReq(request.phone(), request.code());
        }
    }

    @Schema(title = "인증번호 검증 응답 DTO")
    public record VerifyCodeRes(
            @Schema(description = "코드 일치 여부 : 일치하지 않으면 예외이므로 성공하면 언제나 true", example = "true")
            Boolean code,
            @Schema(description = "oauth 사용자 여부. true면 sync, false면 회원가입으로 진행 (일반 회원가입 시 필수값)", example = "true")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean oauth,
            @Schema(description = "기존 계정 존재 여부. true면 sync, false면 회원가입 (oauth 회원가입 시 필수값)", example = "true")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean existsUser,
            @Schema(description = "기존 사용자 아이디", example = "pennyway")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String username
    ) {
        /**
         * 일반 회원가입 시 인증 코드 응답 객체 생성
         *
         * @param isOauthUser Boolean : oauth 사용자 여부. true면 sync, false면 회원가입으로 진행
         */
        public static VerifyCodeRes valueOfGeneral(Boolean isValidCode, Boolean isOauthUser, String username) {
            return new VerifyCodeRes(isValidCode, isOauthUser, null, username);
        }

        /**
         * oauth 회원가입 시 인증 코드 응답 객체 생성
         *
         * @param existsUser Boolean : 기존 계정 존재 여부. true면 sync, false면 회원가입으로 진행
         */
        public static VerifyCodeRes valueOfOauth(Boolean isValidCode, Boolean existsUser, String username) {
            return new VerifyCodeRes(isValidCode, null, existsUser, username);
        }
    }
}
