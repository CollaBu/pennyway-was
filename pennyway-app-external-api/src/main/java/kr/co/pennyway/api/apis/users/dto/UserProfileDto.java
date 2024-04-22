package kr.co.pennyway.api.apis.users.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "사용자 프로필 정보")
public record UserProfileDto(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        @Schema(description = "사용자 아이디", example = "user1")
        String username,
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,
        @Schema(description = "비밀번호 변경 일시", example = "2024-04-22 00:00:00")
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
        @Schema(description = "계정 생성 일시", example = "2024-04-22 00:00:00")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static UserProfileDto from(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .passwordUpdatedAt(user.getPasswordUpdatedAt())
                .profileImageUrl(user.getProfileImageUrl())
                .phone(user.getPhone())
                .profileVisibility(user.getProfileVisibility())
                .locked(user.getLocked())
                .notifySetting(user.getNotifySetting())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
