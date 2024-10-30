package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter;
import kr.co.pennyway.infra.client.guid.IdGenerator;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.socket.command.SendMessageCommand;
import kr.co.pennyway.socket.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({ChatExchangeProperties.class})
public class ChatMessageSendService {
    private final ChatMessageService chatMessageService;

    private final MessageBrokerAdapter messageBrokerAdapter;
    private final IdGenerator<Long> idGenerator;
    private final ChatExchangeProperties chatExchangeProperties;

    /**
     * 채팅 메시지를 전송한다.
     *
     * @param command SendMessageCommand : 채팅 메시지 전송을 위한 Command
     */
    public void execute(SendMessageCommand command) {
        ChatMessage message = ChatMessageBuilder.builder()
                .chatRoomId(command.chatRoomId())
                .chatId(idGenerator.generate())
                .content(command.content())
                .contentType(command.contentType())
                .categoryType(command.categoryType())
                .sender(command.senderId())
                .build();
        
        ChatMessageDto.Response response = ChatMessageDto.Response.from(chatMessageService.save(message));

        messageBrokerAdapter.convertAndSend(
                chatExchangeProperties.getExchange(),
                "chat.room." + command.chatRoomId(),
                response
        );
    }
}
