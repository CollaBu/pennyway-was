package kr.co.pennyway.api.apis.users.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@Schema(title = "사용자 프로필 정보 응답")
public record UserProfileDto(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        @Schema(description = "사용자 아이디", example = "user1")
        String username,
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,
        @Schema(description = "일반 회원가입 이력. 일반 회원가입 계정이 있으면 true, 없으면 false", example = "false")
        boolean isGeneralSignUp,
        @Schema(description = "비밀번호 변경 일시. isOauthAccount가 true면 존재하지 않는 필드", nullable = true, type = "string", example = "yyyy-MM-dd HH:mm:ss")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime passwordUpdatedAt,
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,
        @Schema(description = "프로필 공개 여부", example = "PUBLIC")
        ProfileVisibility profileVisibility,
        @Schema(description = "계정 잠금 여부", example = "false")
        Boolean locked,
        @Schema(description = "알림 설정 정보")
        NotifySetting notifySetting,
        @Schema(description = "계정 생성 일시", type = "string", example = "yyyy-MM-dd HH:mm:ss")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @Schema(description = "Oauth 계정 정보")
        OauthAccountDto oauthAccount
) {
    public UserProfileDto {
        Objects.requireNonNull(id);
        Objects.requireNonNull(username);
        Objects.requireNonNull(name);
        Objects.requireNonNull(profileImageUrl);
        Objects.requireNonNull(phone);
        Objects.requireNonNull(profileVisibility);
        Objects.requireNonNull(locked);
        Objects.requireNonNull(notifySetting);
        Objects.requireNonNull(createdAt);
        Objects.requireNonNull(oauthAccount);
    }

    public static UserProfileDto from(User user, OauthAccountDto oauthAccount) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .passwordUpdatedAt(user.getPasswordUpdatedAt())
                .profileImageUrl(Objects.toString(user.getProfileImageUrl(), ""))
                .phone(user.getPhone())
                .profileVisibility(user.getProfileVisibility())
                .locked(user.isLocked())
                .notifySetting(user.getNotifySetting())
                .isGeneralSignUp(user.isGeneralSignedUpUser())
                .createdAt(user.getCreatedAt())
                .oauthAccount(oauthAccount)
                .build();
    }
}
