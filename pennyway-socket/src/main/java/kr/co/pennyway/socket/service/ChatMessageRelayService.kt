package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.context.chat.service.ChatNotificationCoordinatorService
import kr.co.pennyway.infra.common.event.NotificationEvent
import kr.co.pennyway.socket.common.util.logger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageRelayService(
    private val eventPublisher: ApplicationEventPublisher,
    private val chatNotificationCoordinatorService: ChatNotificationCoordinatorService
) {
    private val log = logger()

    /**
     * 채팅방, 채팅 리스트 뷰를 보고 있지 않은 사용자들에게만 푸시 알림을 전송합니다.
     *
     * @param senderId   Long 전송자 아이디
     * @param chatRoomId Long 채팅방 아이디
     * @param content    String 채팅 내용
     * @apiNote push notification 전송 실패에 대한 재시도를 수행하고 있지 않습니다.
     */
    @Transactional
    fun execute(senderId: Long, chatRoomId: Long, content: String) {
        chatNotificationCoordinatorService.determineRecipients(senderId, chatRoomId)
            .also { log.info("채팅 메시지 알림 전송 컨텍스트: {}", it) }
            .let { context ->
                NotificationEvent.of(
                    context.senderName(),
                    content,
                    context.deviceTokens(),
                    context.senderImageUrl(),
                    mapOf("chatRoomId" to chatRoomId.toString())
                )
            }
            .let { eventPublisher.publishEvent(it) }
    }
}