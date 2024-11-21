package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.services.chat.context.ChatPushNotificationContext;
import kr.co.pennyway.domain.services.chat.service.ChatNotificationCoordinatorService;
import kr.co.pennyway.infra.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageRelayService {
    private final ApplicationEventPublisher eventPublisher;

    private final ChatNotificationCoordinatorService chatNotificationCoordinatorService;

    /**
     * 채팅방, 채팅 리스트 뷰를 보고 있지 않은 사용자들에게만 푸시 알림을 전송합니다.
     *
     * @param senderId   Long 전송자 아이디
     * @param chatRoomId Long 채팅방 아이디
     * @param content    String 채팅 내용
     * @apiNote push notification 전송 실패에 대한 재시도를 수행하고 있지 않습니다.
     */
    @Transactional(readOnly = true)
    public void execute(Long senderId, Long chatRoomId, String content) {
        ChatPushNotificationContext context = executeInTransaction(() -> chatNotificationCoordinatorService.determineRecipients(senderId, chatRoomId));
        log.info("채팅 메시지 알림 전송 컨텍스트: {}", context);

        NotificationEvent notificationEvent = NotificationEvent.of(
                context.senderName(),
                content,
                context.deviceTokens(),
                context.senderImageUrl(),
                Map.of("chatRoomId", chatRoomId.toString())
        );

        eventPublisher.publishEvent(notificationEvent);
    }

    @Transactional
    public <T> T executeInTransaction(Supplier<T> operation) {
        return operation.get();
    }
}
