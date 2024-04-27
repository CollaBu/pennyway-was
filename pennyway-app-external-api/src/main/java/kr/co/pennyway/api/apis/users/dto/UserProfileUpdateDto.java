package kr.co.pennyway.api.apis.users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.api.common.validator.Password;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;

public class UserProfileUpdateDto {
    @Schema(title = "이름 변경 요청 DTO")
    public record NameReq(
            @Schema(description = "이름", example = "페니웨이")
            @NotBlank(message = "이름을 입력해주세요")
            @Pattern(regexp = "^[가-힣a-z]{2,8}$", message = "2~8자의 한글, 영문 소문자만 사용 가능합니다.")
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
    public record NotifySettingUpdateReq(
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
        public static NotifySettingUpdateReq of(NotifySetting.NotifyType type, Boolean flag) {
            return switch (type) {
                case ACCOUNT_BOOK -> new NotifySettingUpdateReq(flag, null, null);
                case FEED -> new NotifySettingUpdateReq(null, flag, null);
                case CHAT -> new NotifySettingUpdateReq(null, null, flag);
            };
        }
    }
}
