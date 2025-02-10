package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.context.chat.service.ChatMessageService
import kr.co.pennyway.domain.domains.message.domain.ChatMessageBuilder
import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter
import kr.co.pennyway.infra.client.guid.IdGenerator
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties
import kr.co.pennyway.socket.command.SendMessageCommand
import kr.co.pennyway.socket.common.dto.ChatMessageDto
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(ChatExchangeProperties::class)
class ChatMessageSendService(
    private val chatMessageService: ChatMessageService,
    private val messageBrokerAdapter: MessageBrokerAdapter,
    private val idGenerator: IdGenerator<Long>,
    private val chatExchangeProperties: ChatExchangeProperties,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    /**
     * 채팅 메시지를 전송한다.
     *
     * @param command SendMessageCommand : 채팅 메시지 전송을 위한 Command
     */
    fun execute(command: SendMessageCommand) {
        val message = command.toChatMessage(command)
            .let { chatMessageService.create(it) }

        with(chatExchangeProperties) {
            messageBrokerAdapter.convertAndSend(
                exchange,
                "chat.room.${command.chatRoomId}",
                ChatMessageDto.Response.from(message),
                command.headers
            )
        }

        command.senderName?.takeIf { it.isNotBlank() }?.let { senderName ->
            simpMessagingTemplate.convertAndSendToUser(
                senderName,
                "/queue/success",
                Unit,
                command.messageIdHeader()
            )
        }
    }

    private fun SendMessageCommand.toChatMessage(command: SendMessageCommand) = ChatMessageBuilder.builder()
        .chatRoomId(command.chatRoomId())
        .chatId(idGenerator.generate())
        .content(command.content())
        .contentType(command.contentType())
        .categoryType(command.categoryType())
        .sender(command.senderId())
        .build()
}