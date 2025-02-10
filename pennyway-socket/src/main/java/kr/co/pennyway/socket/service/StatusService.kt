package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.context.account.service.UserSessionService
import kr.co.pennyway.socket.common.dto.ServerSideMessage
import kr.co.pennyway.socket.common.dto.StatusMessage
import kr.co.pennyway.socket.common.event.ReceiptEvent
import kr.co.pennyway.socket.common.util.logger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class StatusService(
    private val userSessionService: UserSessionService,
    private val publisher: ApplicationEventPublisher
) {
    private val log = logger()

    fun updateStatus(
        userId: Long,
        deviceId: String,
        message: StatusMessage,
        accessor: StompHeaderAccessor
    ) = when (message.isChatRoomStatus) {
        true -> userSessionService.updateUserStatus(userId, deviceId, message.chatRoomId())
        false -> userSessionService.updateUserStatus(userId, deviceId, message.status())
    }
        .also { session -> log.info("사용자 상태 변경: {}", session) }
        .run {
            ServerSideMessage.of("2000", "OK")
                .let { MessageBuilder.createMessage(it, accessor.messageHeaders) }
                .let { ReceiptEvent.of(it) }
                .let { publisher.publishEvent(it) }
        }
}