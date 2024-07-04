package kr.co.pennyway.infra.client.google.fcm;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import kr.co.pennyway.infra.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmManager {
    private final FirebaseMessaging firebaseMessaging;

    /**
     * {@link NotificationEvent}를 받아서 메시지를 전송한다.
     * <p>
     * 디바이스 토큰이 1개인 경우에는 단일 메시지를, 2개 이상인 경우에는 다중 메시지를 전송한다.
     * 만약 디바이스 토큰이 존재하지 않는 경우에는 메시지 전송을 하지 않는다.
     * </p>
     */
    public void sendMessage(NotificationEvent event) {
        if (event.deviceTokensSize() == 0) {
            log.info("메시지 전송을 위한 디바이스 토큰이 존재하지 않습니다.");
            return;
        }

        if (event.deviceTokensSize() == 1) {
            sendSingleMessage(event);
        } else {
            sendMulticastMessage(event);
        }
    }

    private void sendSingleMessage(NotificationEvent event) {
        Message message = event.buildSingleMessage().setApnsConfig(getApnsConfig(event)).build();

        ApiFuture<String> syncRes = firebaseMessaging.sendAsync(message);
        log.info("Successfully sync sent message: " + syncRes);
    }

    private void sendMulticastMessage(NotificationEvent event) {
        MulticastMessage messages = event.buildMulticastMessage().setApnsConfig(getApnsConfig(event)).build();

        ApiFuture<BatchResponse> response = firebaseMessaging.sendEachForMulticastAsync(messages);
        log.info("Successfully sent message: " + response);
    }

    private ApnsConfig getApnsConfig(NotificationEvent event) {
        ApsAlert alert = ApsAlert.builder()
                .setTitle(event.getTitle())
                .setBody(event.getContent())
                .setLaunchImage("")
                .build();

        Aps aps = Aps.builder()
                .setAlert(alert)
                .setSound("default")
                .build();

        return ApnsConfig.builder()
                .setAps(aps)
                .build();
    }
}
