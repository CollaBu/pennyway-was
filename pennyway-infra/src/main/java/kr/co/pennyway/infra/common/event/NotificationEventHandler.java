package kr.co.pennyway.infra.common.event;

import com.google.api.core.ApiFuture;
import kr.co.pennyway.infra.client.google.fcm.FcmManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Executors;

/**
 * FCM 푸시 알림{을 처리하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {
    private final FcmManager fcmManager;

    @TransactionalEventListener
    public void handleEvent(NotificationEvent event) {
        log.debug("handleEvent: {}", event);
        ApiFuture<?> response = fcmManager.sendMessage(event);

        if (response == null) {
            return;
        }

        response.addListener(() -> {
            try {
                log.info("Successfully sent message: " + response.get());
            } catch (Exception e) {
                log.error("Failed to send message: " + e.getMessage());
            }
        }, Executors.newCachedThreadPool()); // FIXME: 알림이 매우 많은 경우 out of memory 발생 가능성 있음 (Thread pool size 제한 필요)
    }
}
