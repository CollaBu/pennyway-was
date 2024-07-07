package kr.co.pennyway.batch.writer;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.domain.domains.notification.repository.NotificationRepository;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.infra.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationWriter implements ItemWriter<DeviceTokenOwner> {
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public void write(Chunk<? extends DeviceTokenOwner> deviceTokenOwners) throws Exception {
        Announcement announcement = Announcement.DAILY_SPENDING;
        LocalDateTime publishedAt = LocalDateTime.now();

        List<Long> userIds = new ArrayList<>();
        List<String> deviceTokens = new ArrayList<>();

        for (DeviceTokenOwner deviceTokenOwner : deviceTokenOwners) {
            userIds.add(deviceTokenOwner.userId());
            deviceTokens.addAll(deviceTokenOwner.deviceTokens());
        }

        notificationRepository.saveDailySpendingAnnounceInBulk(userIds, publishedAt, announcement);

        // 3. 이벤트 리스너 호출
        publisher.publishEvent(
                new NotificationEvent(
                        Announcement.DAILY_SPENDING.getTitle(),
                        Announcement.DAILY_SPENDING.getContent(),
                        deviceTokens,
                        ""
                )
        );
    }
}
