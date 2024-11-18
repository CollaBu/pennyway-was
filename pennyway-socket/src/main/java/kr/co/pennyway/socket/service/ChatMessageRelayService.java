package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.services.chat.context.ChatPushNotificationContext;
import kr.co.pennyway.domain.services.chat.service.ChatMemberForPushRelaySearchService;
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

    private final ChatMemberForPushRelaySearchService chatMemberForPushRelaySearchService;

    public void execute(Long senderId, Long chatRoomId, String content) {
        ChatPushNotificationContext context = executeInTransaction(() -> chatMemberForPushRelaySearchService.determineRecipients(senderId, chatRoomId));

        // 4. 해당 채팅방에 있는 사용자에게만 푸시 알림 전송 (sender 이름, 프로필 사진 url, 채팅 내용, 전송 시간, DeepLink 정보 포함)
        // 필요한 정보.. 채팅 내용, 채팅방 아이디(그대로 사용), sender(이름, 프로필 사진 url), deviceTokens(푸시 알림 대상자들의 deviceToken)
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
