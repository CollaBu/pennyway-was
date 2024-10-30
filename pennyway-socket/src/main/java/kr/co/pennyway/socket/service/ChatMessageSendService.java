package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter;
import kr.co.pennyway.infra.client.guid.IdGenerator;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
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
     * @param chatRoomId   long : 채팅방 ID
     * @param content      String : 메시지 내용
     * @param contentType  {@link MessageContentType} : 메시지 타입
     * @param categoryType {@link MessageCategoryType} : 메시지 카테고리
     * @param senderId     long : 발신자 ID
     */
    public void execute(final long chatRoomId, final String content, MessageContentType contentType, MessageCategoryType categoryType, final long senderId) {
        ChatMessage message = ChatMessageBuilder.builder()
                .chatRoomId(chatRoomId)
                .chatId(idGenerator.generate())
                .content(content)
                .contentType(contentType)
                .categoryType(categoryType)
                .sender(senderId)
                .build();

        chatMessageService.save(message);

        messageBrokerAdapter.convertAndSend(
                chatExchangeProperties.getExchange(),
                "chat.room." + chatRoomId,
                message
        );
    }
}
