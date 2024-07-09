package kr.co.pennyway.batch.dto;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import lombok.Builder;

import java.util.Objects;

@Builder
public record DailySpendingNotification(
        Long userId,
        String title,
        String content,
        String deviceToken
) {
    public DailySpendingNotification {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(deviceToken, "deviceTokens must not be null");
    }

    public static DailySpendingNotification from(DeviceTokenOwner owner) {
        Announcement announcement = Announcement.DAILY_SPENDING;

        return DailySpendingNotification.builder()
                .userId(owner.userId())
                .title(announcement.createFormattedTitle(owner.name()))
                .content(announcement.getContent())
                .deviceToken(owner.deviceToken())
                .build();
    }
}
