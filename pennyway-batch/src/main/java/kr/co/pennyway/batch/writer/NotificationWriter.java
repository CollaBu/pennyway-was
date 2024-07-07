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
        for (DeviceTokenOwner deviceTokenOwner : deviceTokenOwners) {
            userIds.add(deviceTokenOwner.userId());
        }

        notificationRepository.saveDailySpendingAnnounceInBulk(userIds, publishedAt, announcement);

        for (DeviceTokenOwner owner : deviceTokenOwners) {
            publisher.publishEvent(NotificationEvent.of(announcement.createFormattedTitle(owner.name()), announcement.getTitle(), owner.deviceTokens(), ""));
        }
    }
}
