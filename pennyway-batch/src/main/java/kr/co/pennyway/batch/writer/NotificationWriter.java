package kr.co.pennyway.batch.writer;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.infra.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationWriter implements ItemWriter<DeviceTokenOwner> {
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public void write(Chunk<? extends DeviceTokenOwner> deviceTokenOwners) throws Exception {
        // 1. 토큰 주인 userId 리스트 중복 없이 추출
        Set<Long> userIds = deviceTokenOwners.getItems().stream()
                .map(DeviceTokenOwner::userId)
                .collect(Collectors.toSet());


        // 2. 전송 알림 DB 쓰기
        notificationRepository.saveAll(notifications);

        // 3. 이벤트 리스너 호출
        String title = "", content = "", imageUrl = "";
        publisher.publishEvent(NotificationEvent.of(title, content, , imageUrl));
    }
}
