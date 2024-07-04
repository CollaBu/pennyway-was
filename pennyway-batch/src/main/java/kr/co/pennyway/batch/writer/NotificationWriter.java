package kr.co.pennyway.batch.writer;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.infra.client.google.fcm.FcmManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationWriter implements ItemWriter<DeviceToken> {
    private final FcmManager fcmManager;
    private final NotificationRepository notificationRepository;

    @Override
    public void write(Chunk<? extends DeviceToken> deviceTokens) throws Exception {
        // 전송 알림 DB 쓰기

        // 이벤트 리스너 호출
        
    }
}
