package kr.co.pennyway.api.apis.users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.api.common.validator.Password;

public class UserProfileUpdateDto {
    @Schema(title = "이름 변경 요청 DTO")
    public record NameReq(
            @Schema(description = "이름", example = "페니웨이")
            @NotBlank(message = "이름을 입력해주세요")
            @Pattern(regexp = "^[가-힣a-z0-9]{2,8}$", message = "2~8자의 한글, 영문 소문자, 숫자만 사용 가능합니다.")
            String name
    ) {
    }

    @Schema(title = "아이디 변경 요청 DTO")
    public record UsernameReq(
            @Schema(description = "아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            @Pattern(regexp = "^[a-z-_.]{5,20}$", message = "5~20자의 영문 소문자, -, _, . 만 사용 가능합니다.")
            String username
    ) {
    }

    @Schema(title = "현재 비밀번호 검증 요청 DTO")
    public record PasswordVerificationReq(
            @Schema(description = "현재 비밀번호", example = "password")
            @NotBlank(message = "비밀번호를 입력해주세요")
            String password
    ) {
    }

    @Schema(title = "비밀번호 변경 요청 DTO")
    public record PasswordReq(
            @Schema(description = "현재 비밀번호. 공백 문자만 포함되어 있으면 안 됨", example = "password")
            @NotBlank(message = "현재 비밀번호를 입력해주세요")
            String oldPassword,
            @Schema(description = "새 비밀번호. 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)", example = "newPassword")
            @NotBlank(message = "새 비밀번호를 입력해주세요")
            @Password(message = "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)")
            String newPassword
    ) {
    }

    @Schema(title = "사용자 알림 설정 응답 DTO")
    public record NotifySettingUpdateRes(
            @Schema(description = "계좌 알림 설정", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean accountBookNotify,
            @Schema(description = "피드 알림 설정", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean feedNotify,
            @Schema(description = "채팅 알림 설정", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean chatNotify
    ) {
    }

    @Schema(title = "프로필 이미지 등록 요청 DTO")
    public record ProfileImageReq(
            @Schema(description = "프로필 이미지 URL", example = "delete/profile/1/154aa3bd-da02-4311-a735-3bf7e4bb68d2_1717446100295.jpeg")
            @Pattern(regexp = "^delete/.*$", message = "URL은 'delete/'로 시작해야 합니다.")
            @NotBlank(message = "프로필 이미지 URL을 입력해주세요")
            String profileImageUrl
    ) {
    }

    @Schema(title = "사용자 아이디, 전화번호 변경 DTO")
    public record UsernameAndPhoneReq(
            @Schema(description = "변경할 아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            @Pattern(regexp = "^[a-z-_.]{5,20}$", message = "5~20자의 영문 소문자, -, _, . 만 사용 가능합니다.")
            String username,
            @Schema(description = "전화번호", example = "010-2629-4624")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호. 만약 전화번호가 변경되지 않는다면, 6자리 정수 더미값 삽입", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
            String code
    ) {
    }
}
