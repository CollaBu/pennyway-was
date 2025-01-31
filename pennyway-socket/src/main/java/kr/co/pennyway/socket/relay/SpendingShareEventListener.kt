package kr.co.pennyway.socket.relay;

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.pennyway.domain.domains.message.type.MessageCategoryType
import kr.co.pennyway.domain.domains.message.type.MessageContentType
import kr.co.pennyway.infra.common.event.SpendingChatShareEvent
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties
import kr.co.pennyway.socket.command.SendMessageCommand
import kr.co.pennyway.socket.common.util.logger
import kr.co.pennyway.socket.service.ChatMessageSendService
import lombok.extern.slf4j.Slf4j
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Slf4j
@Component
@EnableConfigurationProperties(ChatExchangeProperties::class)
class SpendingShareEventListener(
    private val chatMessageSendService: ChatMessageSendService,
    private val objectMapper: ObjectMapper
) {
    private companion object {
        private val log = logger()
    }

    @RabbitListener(
        containerFactory = "simpleRabbitListenerContainerFactory",
        bindings = [QueueBinding(
            value = Queue("\${pennyway.rabbitmq.spending-chat-share.queue}"),
            exchange = Exchange(value = "\${pennyway.rabbitmq.chat.exchange}", type = "topic"),
            key = ["\${pennyway.rabbitmq.spending-chat-share.routing-key}"]
        )]
    )
    fun handle(event: SpendingChatShareEvent) {
        log.debug("handle: {}", event)

        convertToJson(event.spendingOnDates())
            .getOrNull()
            ?.let { payload ->
                chatMessageSendService.execute(
                    SendMessageCommand.createMessage(
                        event.chatRoomId(),
                        payload,
                        MessageContentType.TEXT,
                        MessageCategoryType.SHARE,
                        event.senderId(),
                        event.name(),
                        null,
                        mapOf("Content-Type" to "application/json", "date" to event.date())
                    )
                )
            }
    }

    private fun convertToJson(spendingOnDates: List<SpendingChatShareEvent.SpendingOnDate>): Result<String> =
        runCatching {
            objectMapper.writeValueAsString(spendingOnDates)
        }.onFailure {
            log.error("Failed to serialize spendingOnDates", it)
        }
}