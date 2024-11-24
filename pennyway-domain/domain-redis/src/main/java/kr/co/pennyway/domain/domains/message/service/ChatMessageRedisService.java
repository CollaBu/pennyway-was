package kr.co.pennyway.domain.domains.message.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import kr.co.pennyway.domain.domains.message.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;

import java.util.List;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageRedisService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage create(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> readRecentMessages(Long roomId, int limit) {
        return chatMessageRepository.findRecentMessages(roomId, limit);
    }

    public Slice<ChatMessage> readMessagesBefore(Long roomId, Long lastMessageId, int size) {
        return chatMessageRepository.findMessagesBefore(roomId, lastMessageId, size);
    }
    
    public Long countUnreadMessages(Long roomId, Long lastReadMessageId) {
        return chatMessageRepository.countUnreadMessages(roomId, lastReadMessageId);
    }
}
