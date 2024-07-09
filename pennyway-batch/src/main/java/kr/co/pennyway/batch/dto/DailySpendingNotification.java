package kr.co.pennyway.batch.dto;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record DailySpendingNotification(
        Long userId,
        String title,
        String content,
        List<String> deviceTokens
) {
    public DailySpendingNotification {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(deviceTokens, "deviceTokens must not be null");
    }

    /**
     * {@link DeviceTokenOwner}를 DailySpendingNotification DTO로 변환하는 정적 팩토리 메서드
     * <p>
     * DeviceToken은 List로 변환되어 멤버 변수로 관리하게 된다.
     */
    public static DailySpendingNotification from(DeviceTokenOwner owner) {
        Announcement announcement = Announcement.DAILY_SPENDING;
        List<String> deviceTokens = new ArrayList<>();
        deviceTokens.add(owner.deviceToken());

        return DailySpendingNotification.builder()
                .userId(owner.userId())
                .title(announcement.createFormattedTitle(owner.name()))
                .content(announcement.getContent())
                .deviceTokens(deviceTokens)
                .build();
    }

    public void addDeviceToken(String deviceToken) {
        deviceTokens.add(deviceToken);
    }
}
