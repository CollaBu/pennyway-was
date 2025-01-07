package kr.co.pennyway.socket.controller;

import kr.co.pennyway.socket.command.SendMessageCommand
import kr.co.pennyway.socket.common.annotation.PreAuthorize
import kr.co.pennyway.socket.common.dto.ChatMessageDto
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import kr.co.pennyway.socket.service.ChatMessageSendService
import kr.co.pennyway.socket.service.LastMessageIdSaveService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Controller
class ChatMessageController(
    private val chatMessageSendService: ChatMessageSendService,
    private val lastMessageIdSaveService: LastMessageIdSaveService
) {
    companion object {
        private const val CHAT_MESSAGE_PATH = "chat.message.{chatRoomId}"
        private const val READ_MESSAGE_PATH = "chat.message.{chatRoomId}.read.{lastReadMessageId}"
    }

    @MessageMapping(CHAT_MESSAGE_PATH)
    @PreAuthorize("#isAuthenticated(#principal) and @chatRoomAccessChecker.hasPermission(#chatRoomId, #principal)")
    fun sendMessage(
        @DestinationVariable chatRoomId: Long,
        @Validated payload: ChatMessageDto.Request,
        principal: UserPrincipal,
        @Header("x-message-id") messageId: StompHeaderAccessor?
    ) {
        chatMessageSendService.execute(
            SendMessageCommand.createUserMessage(
                chatRoomId,
                payload.content(),
                payload.contentType(),
                principal.userId,
                principal.name,
                messageId?.let { mapOf("x-message-id" to it) }
            )
        )
    }

    @MessageMapping(READ_MESSAGE_PATH)
    @PreAuthorize("#isAuthenticated(#principal) and @chatRoomAccessChecker.hasPermission(#chatRoomId, #principal)")
    fun readMessage(
        @DestinationVariable("chatRoomId") @Validated chatRoomId: Long,
        @DestinationVariable("lastReadMessageId") @Validated lastReadMessageId: Long,
        principal: UserPrincipal
    ) {
        lastMessageIdSaveService.execute(principal.userId, chatRoomId, lastReadMessageId)
    }
}
