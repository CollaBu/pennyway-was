package kr.co.pennyway.batch.dto;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Builder
public record DailySpendingNotification(
        Long userId,
        String title,
        String content,
        Announcement announcement,
        LocalDateTime publishedAt,
        List<String> deviceTokens
) {
    public DailySpendingNotification {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(announcement, "announcement must not be null");
        Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        Objects.requireNonNull(deviceTokens, "deviceTokens must not be null");
    }

    public static DailySpendingNotification of(DeviceTokenOwner owner, LocalDateTime publishedAt) {
        Announcement announcement = Announcement.DAILY_SPENDING;

        return DailySpendingNotification.builder()
                .userId(owner.userId())
                .title(announcement.createFormattedTitle(owner.name()))
                .content(announcement.getTitle())
                .announcement(announcement)
                .publishedAt(publishedAt)
                .deviceTokens(owner.deviceTokens())
                .build();
    }
}
