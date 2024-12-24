package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.context.chat.service.ChatMessageService
import kr.co.pennyway.domain.domains.message.domain.ChatMessageBuilder
import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter
import kr.co.pennyway.infra.client.guid.IdGenerator
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties
import kr.co.pennyway.socket.command.SendMessageCommand
import kr.co.pennyway.socket.common.dto.ChatMessageDto
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(ChatExchangeProperties::class)
class ChatMessageSendService(
    private val chatMessageService: ChatMessageService,
    private val messageBrokerAdapter: MessageBrokerAdapter,
    private val idGenerator: IdGenerator<Long>,
    private val chatExchangeProperties: ChatExchangeProperties
) {
    /**
     * 채팅 메시지를 전송한다.
     *
     * @param command SendMessageCommand : 채팅 메시지 전송을 위한 Command
     */
    fun execute(command: SendMessageCommand) {
        val message = ChatMessageBuilder.builder()
            .chatRoomId(command.chatRoomId())
            .chatId(idGenerator.generate())
            .content(command.content())
            .contentType(command.contentType())
            .categoryType(command.categoryType())
            .sender(command.senderId())
            .build()
            .let { chatMessageService.create(it) }

        messageBrokerAdapter.convertAndSend(
            chatExchangeProperties.exchange,
            "chat.room.${command.chatRoomId()}",
            ChatMessageDto.Response.from(message)
        )
    }
}