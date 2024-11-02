package kr.co.pennyway.domain.common.redis.message.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.repository.ChatMessageRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepositoryImpl chatMessageRepositoryImpl;

    public ChatMessage create(ChatMessage chatMessage) {
        return chatMessageRepositoryImpl.save(chatMessage);
    }
}
