package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.common.redis.session.UserSession;
import kr.co.pennyway.domain.common.redis.session.UserSessionService;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import kr.co.pennyway.socket.common.dto.StatusMessage;
import kr.co.pennyway.socket.common.event.ReceiptEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {
    private final UserSessionService userSessionService;
    private final ApplicationEventPublisher publisher;

    public void updateStatus(Long userId, String deviceId, StatusMessage message, StompHeaderAccessor accessor) {
        if (message.isChatRoomStatus()) {
            UserSession session = userSessionService.updateUserStatus(userId, deviceId, message.chatRoomId());
            log.debug("사용자 상태 변경: {}", session);
        } else {
            UserSession session = userSessionService.updateUserStatus(userId, deviceId, message.status());
            log.debug("사용자 상태 변경: {}", session);
        }

        ServerSideMessage payload = ServerSideMessage.of("2000", "OK");
        Message<ServerSideMessage> response = MessageBuilder.createMessage(payload, accessor.getMessageHeaders());

        publisher.publishEvent(ReceiptEvent.of(response)); // @FIXME: Refresh Event와 달리 Receipt가 성공적으로 처리되지 않음.
    }
}
