package kr.co.pennyway.infra.client.google.fcm;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import kr.co.pennyway.infra.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    public ApiFuture<?> sendMessage(NotificationEvent event) {
        if (event.deviceTokensSize() == 0) {
            log.info("메시지 전송을 위한 디바이스 토큰이 존재하지 않습니다.");
            return null;
        }

        if (event.deviceTokensSize() == 1) {
            return sendSingleMessage(event);
        } else {
            return sendMulticastMessage(event);
        }
    }

    private ApiFuture<String> sendSingleMessage(NotificationEvent event) {
        log.info("단일 메시지 전송 : {}", event);
        Message message = event.buildSingleMessage().setApnsConfig(getApnsConfig(event)).build();

        return firebaseMessaging.sendAsync(message);
    }

    private ApiFuture<BatchResponse> sendMulticastMessage(NotificationEvent event) {
        log.info("다중 메시지 전송 : {}", event);
        MulticastMessage messages = event.buildMulticastMessage().setApnsConfig(getApnsConfig(event)).build();

        return firebaseMessaging.sendEachForMulticastAsync(messages);
    }

    private ApnsConfig getApnsConfig(NotificationEvent event) {
        ApsAlert alert = ApsAlert.builder()
                .setTitle(event.title())
                .setBody(event.content())
                .setLaunchImage(event.imageUrl())
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
