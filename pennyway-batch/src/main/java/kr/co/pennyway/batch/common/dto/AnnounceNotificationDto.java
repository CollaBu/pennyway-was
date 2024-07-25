package kr.co.pennyway.batch.common.dto;

import kr.co.pennyway.domain.domains.notification.type.Announcement;
import lombok.Builder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Builder
public record AnnounceNotificationDto(
        Long userId,
        String title,
        String content,
        Set<String> deviceTokens
) {
    public AnnounceNotificationDto {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(deviceTokens, "deviceTokens must not be null");
    }

    public static AnnounceNotificationDto from(DeviceTokenOwner owner, Announcement announcement) {
        Set<String> deviceTokens = new HashSet<>();
        deviceTokens.add(owner.deviceToken());

        return AnnounceNotificationDto.builder()
                .userId(owner.userId())
                .title(announcement.createFormattedTitle(owner.name()))
                .content(announcement.createFormattedContent(owner.name()))
                .deviceTokens(deviceTokens)
                .build();
    }

    public void addDeviceToken(String deviceToken) {
        deviceTokens.add(deviceToken);
    }

    /**
     * DeviceToken을 List로 변환하여 View를 반환한다.
     */
    public List<String> deviceTokensForList() {
        return List.copyOf(deviceTokens);
    }
}
