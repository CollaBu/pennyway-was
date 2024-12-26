package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.context.chat.service.ChatMessageStatusService
import org.springframework.stereotype.Service

@Service
class LastMessageIdSaveService(private val chatMessageStatusService: ChatMessageStatusService) {
    fun execute(userId: Long, chatRoomId: Long, lastReadMessageId: Long) {
        chatMessageStatusService.saveLastReadMessageId(userId, chatRoomId, lastReadMessageId)
    }
}
