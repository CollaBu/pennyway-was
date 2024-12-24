package kr.co.pennyway.socket.controller;

import kr.co.pennyway.socket.command.SendMessageCommand
import kr.co.pennyway.socket.common.annotation.PreAuthorize
import kr.co.pennyway.socket.common.dto.ChatMessageDto
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import kr.co.pennyway.socket.service.ChatMessageSendService
import kr.co.pennyway.socket.service.LastMessageIdSaveService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
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
        principal: UserPrincipal
    ) {
        chatMessageSendService.execute(
            SendMessageCommand.createUserMessage(
                chatRoomId,
                payload.content(),
                payload.contentType(),
                principal.userId
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
